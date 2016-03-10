package com.typesafe.sbt.bundle

import java.lang.reflect.InvocationTargetException
import scala.collection.JavaConverters._
import java.util.jar.{JarEntry, JarFile}
import java.io.InputStream
import com.lightbend.lagom.sbt.LagomJava
import com.typesafe.sbt.SbtNativePackager
import sbt._
import sbt.Keys._
import play.api.libs.json._
import scala.reflect.ClassTag
import scala.util.{Success, Failure, Try}
import sbt.Resolver.bintrayRepo

object LagomBundle extends AutoPlugin {

  import LagomBundleImport._
  import LagomBundleKeys._
  import SbtNativePackager.autoImport._
  import SbtBundle.autoImport._
  import ByteConversions._

  val autoImport = LagomBundleImport

  override def requires = LagomJava && SbtBundle
  override def trigger = allRequirements

  // Configuration to add api tools library dependencies
  private val apiToolsConfig = config("api-tools").hide

  override def projectSettings =
    bundleSettings(Bundle) ++ Seq(
      BundleKeys.nrOfCpus := 1.0,
      BundleKeys.memory := 128.MiB,
      BundleKeys.diskSpace := 200.MB,
      endpointsPort := 9000,
      ivyConfigurations += apiToolsConfig,
      // scalaBinaryVersion.value uses the binary compatible scala version from the Lagom project
      conductrBundleLibVersion := "1.4.0",
      libraryDependencies ++= Seq(
        "com.lightbend.lagom"   % s"api-tools_${scalaBinaryVersion.value}" % "0.1.0-SNAPSHOT" % apiToolsConfig,
        "com.typesafe.conductr" % s"lagom10-conductr-bundle-lib_${scalaBinaryVersion.value}" % conductrBundleLibVersion.value
      ),
      resolvers += bintrayRepo("typesafe", "maven-releases"),
      play.sbt.PlaySettings.manageClasspath(apiToolsConfig)
    )

  override def buildSettings =
    super.buildSettings ++ cassandraConfigurationSettings(CassandraConfiguration)

  /**
    * Override bundle settings from sbt-bundle with the collected Lagom endpoints
    */
  private def bundleSettings(config: Configuration): Seq[Setting[_]] =
    inConfig(config)(
      Seq(
        BundleKeys.overrideEndpoints := Some(collectEndpoints(config).value + ("akka-remote" -> Endpoint("tcp"))),
        BundleKeys.startCommand ++= {
          val bindings = (for {
            endpoints            <- BundleKeys.overrideEndpoints.value
            (serviceName, _)     <- endpoints.filterNot(_._1 == "akka-remote").headOption
            formattedServiceName =  envName(serviceName)
          } yield
            Seq(
              s"-Dhttp.address=$$${formattedServiceName}_BIND_IP",
              s"-Dhttp.port=$$${formattedServiceName}_BIND_PORT"
            )).toSeq.flatten
          // The application secret is not used by the Lagom project so the value doesn't really matter.
          // Therefore it is save to automatically generate one here. It is necessary though to set the key in prod mode.
          val applicationSecret = s"-Dplay.crypto.secret=${SbtBundle.hash(s"${name.value}-${version}")}"
          bindings :+ applicationSecret
        }
      )
    )

  /**
    * Bundle configuration for cassandra.
    * Only one bundle configuration for the entire project is created. This configuration should be used for a scenario
    * in which multiple Lagom services can use the same Cassandra database, e.g. on ConductR sandbox
    * Note that each Lagom services has a separate keyspace on ConductR and is therefore logically separated
    * even there are using the same DB.
    */
  private def cassandraConfigurationSettings(config: Configuration): Seq[Setting[_]] =
    inConfig(config)(
      Seq(
        BundleKeys.configurationName := "cassandra-configuraton",
        target := (baseDirectory in LocalRootProject).value / "target" / "bundle-configuration",
        NativePackagerKeys.stagingDirectory := (target in config).value / "stage",
        NativePackagerKeys.stage := cassandraStageConfiguration(config).value,
        NativePackagerKeys.dist := SbtBundle.createConfiguration(config, "Cassandra bundle configuration has been created").value
      ) ++ dontAggregate(NativePackagerKeys.stage, NativePackagerKeys.dist)
    )

  /**
    * Use this to not perform tasks for the aggregated projects, i.e sub projects.
    */
  private def dontAggregate(keys: Scoped*): Seq[Setting[_]] =
    keys.map(aggregate in _ := false)

  private def cassandraStageConfiguration(config: Configuration): Def.Initialize[Task[File]] = Def.task {
    val configurationTarget = (NativePackagerKeys.stagingDirectory in config).value / config.name
    val resourcePath = "bundle-configuration/cassandra"
    val jarFile = new File(this.getClass.getProtectionDomain.getCodeSource.getLocation.getPath)
    if(jarFile.isFile) {
      val jar = new JarFile(jarFile)
      IO.createDirectory(configurationTarget)
      copyDirectoryFromJar(jar, configurationTarget, s"$resourcePath/")
    }
    else {
      val urlOpt = Option(this.getClass.getResource(s"/$resourcePath"))
      val url = urlOpt.getOrElse {
        fail("The cassandra configuration from sbt-lagom-bundle couldn't be found both in the jar file and in the resources directory of the project.")
      }
      IO.createDirectory(configurationTarget)
      IO.copyDirectory(new File(url.toURI), configurationTarget, overwrite = true, preserveLastModified = false)
    }

    configurationTarget
  }

  /**
    * Scans a jar file and filters files based on the `dirPrefix` parameter.
    * These files are then copied to the `targetDir`.
    */
  private def copyDirectoryFromJar(fromJarFile: JarFile, targetDir: File, dirPrefix: String): Unit = {
    fromJarFile.entries.asScala.foreach { entry =>
      if(entry.getName.startsWith(dirPrefix) && !entry.isDirectory) {
        val name = entry.getName.drop(dirPrefix.size)
        val toFile = targetDir / name
        withJarInputStream(fromJarFile, entry) { in =>
          IO.transfer(in, toFile)
        }
      }
    }
  }

  private def withJarFile[T](path: String)(block: JarFile => T): T = {
    val jarFile = new JarFile(this.getClass.getProtectionDomain.getCodeSource.getLocation.getPath)
    try {
      block(jarFile)
    }
    finally {
      jarFile.close()
    }
  }

  private def withJarInputStream[T](jarFile: JarFile, jarEntry: JarEntry)(block: InputStream => T): T = {
    val in = jarFile.getInputStream(jarEntry)
    try {
      block(in)
    }
    finally {
      in.close()
    }
  }

  /**
    * Collect the endpoints either from `BundleKeys.endpoints` or from Lagom
    * Use the `BundleKeys.endpoints` if they are different as the default sbt-bundle endpoints.
    * Otherwise collect the endpoints from Lagom by accessing the Lagom api tools library
    */
  private def collectEndpoints(config: Configuration): Def.Initialize[Task[Map[String, Endpoint]]] = Def.taskDyn {
    Def.task {
      val manualEndpoints = (BundleKeys.endpoints in config).value
      if (manualEndpoints != DefaultEndpoints)
        manualEndpoints
      else {
        val classpath = toClasspathUrls(
          // managed classpath in api tools config contains the api tools library dependencies
          (managedClasspath in apiToolsConfig).value ++
          // full classpath containing the Lagom services, Lagom framework and all its dependencies
          (fullClasspath in Compile).value
        )
        // Create class loader based on a classpath that contains all project related + api tools library classes
        val classLoader = new java.net.URLClassLoader(classpath, scalaInstance.value.loader)
        // Lookup Lagom services
        val servicesAsString = ServiceDetector.services(classLoader)
        // Convert services string to `Map[String, Endpoint]`
        toConductrEndpoints(servicesAsString, (endpointsPort in config).value)
      }
    }
  }

  private def toClasspathUrls(attributedFiles: Seq[Attributed[File]]): Array[URL] =
    attributedFiles.files.map(_.toURI.toURL).toArray

  /**
    * Convert services string to `Map[String, Endpoint]` by using the Play json library
    */
  private def toConductrEndpoints(services: String, servicePort: Int): Map[String, Endpoint] = {
    val json = Json.parse(services)
    val serviceNames = (json \\ "name").flatMap(_.asOpt[String])
    val formattedServiceNames = serviceNames.map {
      case name if name.startsWith("/") => name.drop(1) // TODO: The drop will not be necessary anymore once Lagom verifies the service name
      case name => name
    }
    formattedServiceNames.map(name => name -> Endpoint("http", 0, Set(URI(s"http://:$servicePort/$name")))).toMap
  }

  private def envName(name: String) =
    name.replaceAll("\\W", "_").toUpperCase
}

/**
  * Mirrors the ServiceDetector class from the Lagom api tools library.
  * By declaring the public methods from the Lagom api tools library `ServiceDetector` it is possible to "safely"
  * call the class via reflection.
  */
private object ServiceDetector {

  // `ServiceDetector` mirror from the Lagom api tools library.
  // The method signature equals the signature from the api tools `ServiceDetector`
  type ServiceDetector = {
    def services(classLoader: ClassLoader): String
  }

  /**
    * Calls the Lagom api tools library `ServicesDetector.services` method by using reflection
    */
  def services(classLoader: ClassLoader): String =
    withContextClassloader(classLoader) { loader =>
      getSingletonObject[ServiceDetector](loader, "com.lightbend.lagom.internal.api.tools.ServiceDetector$") match {
        case Failure(t) => fail(s"Endpoints can not be resolved from Lagom project. Error: ${t.getMessage}")
        case Success(serviceDetector) => serviceDetector.services(loader)
      }
    }

  /**
    * Uses the given class loader for the given code block
    */
  private def withContextClassloader[T](loader: ClassLoader)(body: ClassLoader => T): T = {
    val current = Thread.currentThread().getContextClassLoader()
    try {
      Thread.currentThread().setContextClassLoader(loader)
      body(loader)
    } finally Thread.currentThread().setContextClassLoader(current)
  }

  /**
    * Resolves the singleton object instance via reflection.
    * The given `className` must end with "$", e.g. "com.lightbend.lagom.internal.api.tools.ServiceDetector$"
    */
  private def getSingletonObject[T: ClassTag](classLoader: ClassLoader, className: String): Try[T] =
    Try {
      val clazz = classLoader.loadClass(className)
      val t = implicitly[ClassTag[T]].runtimeClass
      clazz.getField("MODULE$").get(null) match {
        case null                  => throw new NullPointerException
        case c if !t.isInstance(c) => throw new ClassCastException(s"${clazz.getName} is not a subtype of $t")
        case c: T                  => c
      }
    } recover {
      case i: InvocationTargetException if i.getTargetException ne null => throw i.getTargetException
    }
}