import scalariform.formatter.preferences._
import bintray.Keys._

organization := "com.typesafe.sbt"
name := "sbt-lagom-bundle"

scalaVersion := "2.10.4"
scalacOptions ++= List(
  "-unchecked",
  "-deprecation",
  "-language:_",
  "-target:jvm-1.7",
  "-encoding", "UTF-8"
)

lazy val sbtLagomBundle = project.in(file("."))

addSbtPlugin(Library.sbtBundle)
addSbtPlugin(Library.sbtLagom)

libraryDependencies += Library.playJson

scalariformSettings
ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(PreserveDanglingCloseParenthesis, true)

releaseSettings
ReleaseKeys.versionBump := sbtrelease.Version.Bump.Minor

sbtPlugin := true

licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))
publishMavenStyle := false
bintrayPublishSettings
repository in bintray := "sbt-plugins"
bintrayOrganization in bintray := Some("sbt-lagom-bundle")

scriptedSettings
scriptedLaunchOpts <+= version apply { v => s"-Dproject.version=$v" }
