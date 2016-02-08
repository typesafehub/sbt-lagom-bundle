import sbt._
import sbt.Resolver.bintrayRepo

object Version {
  // TODO: Use non SNAPSHOT version once Lagom published
  val lagom             = "0.1.0-SNAPSHOT"
  val sbtBundle         = "1.3.1"
  val play              = "2.4.6"
  val scala             = "2.10.6"
}

object Library {
  val sbtLagom               = "com.newco.lagom"       %  "lagom-sbt-plugin"            % Version.lagom
  val sbtBundle              = "com.typesafe.sbt"      %  "sbt-bundle"                  % Version.sbtBundle
  val playJson               = "com.typesafe.play"     %% "play-json"                   % Version.play
}

object Resolver {
  val typesafeReleases        = "typesafe-releases" at "http://repo.typesafe.com/typesafe/maven-releases"
  val typesafeBintrayReleases = bintrayRepo("typesafe", "maven-releases")
}
