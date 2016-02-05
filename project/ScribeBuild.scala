import sbt._
import sbt.Keys._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

object ScribeBuild extends Build {
  val SharedSettings = Seq(
    name := s"${Details.name}-core",
    version := Details.version,
    organization := Details.organization,
    scalaVersion := Details.scalaVersion,
    sbtVersion := Details.sbtVersion,
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature",
      "-encoding", "utf8"),
    resolvers ++= Seq(
      Resolver.sonatypeRepo("snapshots"),
      Resolver.sonatypeRepo("releases"),
      Resolver.typesafeRepo("releases")
    ),
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full),
    publishTo <<= version {
      (v: String) =>
        val nexus = "https://oss.sonatype.org/"
        if (v.trim.endsWith("SNAPSHOT")) {
          Some("snapshots" at nexus + "content/repositories/snapshots")
        } else {
          Some("releases" at nexus + "service/local/staging/deploy/maven2")
        }
    },
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
    .aggregate(js, jvm)
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
        "io.github.widok" %%% "scala-js-momentjs" % "0.1.4",
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
}

object Details {
  val organization = "com.outr.scribe"
  val name = "scribe"
  val version = "1.1.0-SNAPSHOT"
  val url = "http://outr.com"
  val licenseType = "MIT"
  val licenseURL = "http://opensource.org/licenses/MIT"
  val projectURL = "https://github.com/outr/scribe"
  val repoURL = "https://github.com/outr/scribe.git"
  val developerId = "darkfrog"
  val developerName = "Matt Hicks"
  val developerURL = "http://matthicks.com"

  val sbtVersion = "0.13.9"
  val scalaVersion = "2.11.7"
}

object Dependencies {
  val ScalaTest = "3.0.0-M15"
}
