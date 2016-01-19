package com.typesafe.sbt.bundle

import sbt._

object LagomBundle extends AutoPlugin {

  import SbtBundle.autoImport._

  override def `requires` = SbtBundle

  override def trigger = allRequirements

  // TODO: The endpoints need to be retrieved from the Lagom API Tools library
  override def projectSettings = Seq(
    BundleKeys.endpoints := Map("helloservice" -> Endpoint("http", 0, Set(URI("http://:9000/helloservice"))))
  )

  private def collectLagomEndpoints: Def.Initialize[Task[Map[String, Endpoint]]] =
    Def.task {
      // TODO: Extract Lagom services from API Tools library
      ???
    }
}
