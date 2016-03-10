import sbt._
import sbt.Resolver.bintrayRepo

object Version {
  val lagom             = "1.0.0-M1"
  val sbtBundle         = "1.3.1"
  val play              = "2.3.10" // Using Play 2.3 to ensure compatibility with sbt-conductr
  val scala             = "2.10.6"
}

object Library {
  val sbtLagom               = "com.lightbend.lagom"   %  "lagom-sbt-plugin"            % Version.lagom
  val sbtBundle              = "com.typesafe.sbt"      %  "sbt-bundle"                  % Version.sbtBundle
  val playJson               = "com.typesafe.play"     %% "play-json"                   % Version.play
}

object Resolver {
  val typesafeReleases        = "typesafe-releases" at "http://repo.typesafe.com/typesafe/maven-releases"
  val typesafeBintrayReleases = bintrayRepo("typesafe", "maven-releases")
}
