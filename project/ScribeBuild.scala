import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._

object ScribeBuild extends Build {
  val SharedSettings = Seq(
    name := Details.name,
    version := Details.version,
    organization := Details.organization,
    scalaVersion := Details.scalaVersion,
    crossScalaVersions := Details.scalaVersions,
    sbtVersion := Details.sbtVersion,
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8"),
    resolvers ++= Seq(
      Resolver.sonatypeRepo("snapshots"),
      Resolver.sonatypeRepo("releases"),
      Resolver.typesafeRepo("releases")
    ),
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
    publishArtifact in Test := false,
    pomExtra :=
      <url>${Details.url}</url>
      <licenses>
        <license>
          <name>{Details.licenseType}</name>
          <url>{Details.licenseURL}</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <developerConnection>scm:{Details.repoURL}</developerConnection>
        <connection>scm:{Details.repoURL}</connection>
        <url>{Details.projectURL}</url>
      </scm>
      <developers>
        <developer>
          <id>{Details.developerId}</id>
          <name>{Details.developerName}</name>
          <url>{Details.developerURL}</url>
        </developer>
      </developers>
  )

  lazy val root = project.in(file("."))
    .aggregate(js, jvm, slf4j)
    .settings(SharedSettings: _*)
    .settings(publishArtifact := false)

  lazy val scribe = crossProject.in(file("."))
    .settings(SharedSettings: _*)
    .settings(
      libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-reflect" % _),
      autoAPIMappings := true,
      apiMappings += (scalaInstance.value.libraryJar -> url(s"http://www.scala-lang.org/api/${scalaVersion.value}/"))
    )
    .jsSettings(
      libraryDependencies ++= Seq(
        "org.scalatest" %%% "scalatest" % Dependencies.ScalaTest % "test"
      ),

      scalaJSStage in Global := FastOptStage
    )
    .jvmSettings(
      libraryDependencies ++= Seq(
        "org.scalatest" %% "scalatest" % Dependencies.ScalaTest % "test"
      )
    )

  lazy val js = scribe.js
  lazy val jvm = scribe.jvm

  lazy val slf4j = project.in(file("slf4j"))
    .dependsOn(jvm)
    .settings(SharedSettings: _*)
    .settings(
      name := "scribe-slf4j",
      libraryDependencies ++= Seq(
        "org.slf4j" % "slf4j-api" % Dependencies.SLF4J,
        "org.scalatest" %% "scalatest" % Dependencies.ScalaTest % "test"
      )
    )
}

object Details {
  val organization = "com.outr.scribe"
  val name = "scribe"
  val version = "1.2.3"
  val url = "http://outr.com"
  val licenseType = "MIT"
  val licenseURL = "http://opensource.org/licenses/MIT"
  val projectURL = "https://github.com/outr/scribe"
  val repoURL = "https://github.com/outr/scribe.git"
  val developerId = "darkfrog"
  val developerName = "Matt Hicks"
  val developerURL = "http://matthicks.com"

  val sbtVersion = "0.13.11"
  val scalaVersions = List("2.12.0-M4", "2.11.8")
  val scalaVersion = scalaVersions.head
}

object Dependencies {
  val SLF4J = "1.7.21"
  val ScalaTest = "3.0.0-M16-SNAP4"
}