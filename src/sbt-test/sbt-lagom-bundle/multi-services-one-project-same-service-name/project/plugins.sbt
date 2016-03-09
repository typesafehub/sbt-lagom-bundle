addSbtPlugin("com.lightbend.lagom" % "lagom-sbt-plugin" % "0.1.0-SNAPSHOT")
addSbtPlugin("com.typesafe.sbt" % "sbt-lagom-bundle" % sys.props("project.version"))

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.6"
