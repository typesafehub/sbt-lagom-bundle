package com.typesafe.sbt.bundle

import sbt._
import com.typesafe.sbt.SbtNativePackager.Universal

object LagomBundleImport {

  // Configuration to produce a bundle configuration for cassandra
  val CassandraConfiguration = config("cassandra-configuration") extend Universal

  object LagomBundleKeys {

    val conductrBundleLibVersion = SettingKey[String](
      "lagom-bundle-conductr-bundle-lib-version",
      "The version of conductr-bundle-lib to depend on. Defaults to 1.4.2"
    )

    @deprecated("This setting is no longer used as endpoint port is not of Bundle Endpoint declaration using HTTP request ACL", since = "1.0.4")
    val endpointsPort = SettingKey[Int](
      "lagom-bundle-endpoints-port",
      "Declares the port for each service endpoint that gets exposed to the outside world, e.g. http://:9000/myservice. Defaults to 9000."
    )
  }
}
