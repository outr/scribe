// Scala versions
val scala213 = "2.13.3"
val scala212 = "2.12.12"
val scala211 = "2.11.12"
val scala3 = "0.27.0-RC1"
val allScalaVersions = List(scala213, scala212, scala211, scala3)
val scala2Versions = List(scala213, scala212, scala211)
val nativeScalaVersions = List(scala211)
val compatScalaVersions = List(scala213, scala212)

name := "scribe"
organization in ThisBuild := "com.outr"
version in ThisBuild := "3.0.4"
scalaVersion in ThisBuild := scala213
scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation")
javacOptions in ThisBuild ++= Seq("-source", "1.8", "-target", "1.8")
resolvers in ThisBuild += Resolver.sonatypeRepo("releases")
resolvers in ThisBuild += Resolver.sonatypeRepo("snapshots")

publishTo in ThisBuild := sonatypePublishTo.value
sonatypeProfileName in ThisBuild := "com.outr"
licenses in ThisBuild := Seq("MIT" -> url("https://github.com/outr/scribe/blob/master/LICENSE"))
sonatypeProjectHosting in ThisBuild := Some(xerial.sbt.Sonatype.GitHubHosting("outr", "scribe", "matt@outr.com"))
homepage in ThisBuild := Some(url("https://github.com/outr/scribe"))
scmInfo in ThisBuild := Some(
  ScmInfo(
    url("https://github.com/outr/scribe"),
    "scm:git@github.com:outr/scribe.git"
  )
)
developers in ThisBuild := List(
  Developer(id="darkfrog", name="Matt Hicks", email="matt@matthicks.com", url=url("http://matthicks.com"))
)
parallelExecution in ThisBuild := false

// Core
val perfolationVersion: String = "1.2.0"
val sourcecodeVersion: String = "0.2.1"
val collectionCompat: String = "2.2.0"

// Testing
val scalatestVersion: String = "3.2.2"

// SLF4J
val slf4jVersion: String = "1.7.30"
val slf4j18Version: String = "1.8.0-beta4"

// Slack and Logstash Dependencies
val youiVersion: String = "0.13.17"

// Benchmarking Dependencies
val log4jVersion: String = "2.13.3"
val disruptorVersion: String = "3.4.2"
val logbackVersion: String = "1.2.3"
val typesafeConfigVersion: String = "1.4.0"
val scalaLoggingVersion: String = "3.9.2"
val tinyLogVersion: String = "1.3.6"
val log4sVersion: String = "1.8.2"

// set source map paths from local directories to github path
val sourceMapSettings = List(
  scalacOptions ++= git.gitHeadCommit.value.map { headCommit =>
    val local = baseDirectory.value.toURI
    val remote = s"https://raw.githubusercontent.com/outr/scribe/$headCommit/"
    s"-P:scalajs:mapSourceURI:$local->$remote"
  }
)

val commonNativeSettings = Seq(
  scalaVersion := scala211,
  crossScalaVersions := nativeScalaVersions,
  nativeLinkStubs := true
)

lazy val root = project.in(file("."))
  .aggregate(
    coreJS, coreJVM, coreNative,
    slf4j, slf4j18, migration, slack, logstash)
  .settings(
    name := "scribe",
    publish := {},
    publishLocal := {}
  )

lazy val core = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Full)
  .settings(
    name := "scribe",
    libraryDependencies ++= Seq(
      "com.outr" %%% "perfolation" % perfolationVersion,
      "com.lihaoyi" %%% "sourcecode" % sourcecodeVersion,
      "org.scalatest" %%% "scalatest" % scalatestVersion % Test
    ),
    libraryDependencies ++= (
      if (isDotty.value) {
        Nil
      } else {
        List("org.scala-lang.modules" %% "scala-collection-compat" % collectionCompat)
      }
    ),
    publishArtifact in Test := false
  )
  .jsSettings(sourceMapSettings)
  .jsSettings(
    crossScalaVersions := scala2Versions
  )
  .jvmSettings(
    crossScalaVersions := allScalaVersions
  )
  .nativeSettings(
    commonNativeSettings
  )

lazy val coreJS = core.js
lazy val coreJVM = core.jvm
lazy val coreNative = core.native

lazy val slf4j = project.in(file("slf4j"))
  .dependsOn(coreJVM)
  .settings(
    name := "scribe-slf4j",
    publishArtifact in Test := false,
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-api" % slf4jVersion,
      "org.scalatest" %% "scalatest" % scalatestVersion % Test
    ),
    crossScalaVersions := scala2Versions
  )

lazy val slf4j18 = project.in(file("slf4j18"))
  .dependsOn(coreJVM)
  .settings(
    name := "scribe-slf4j18",
    publishArtifact in Test := false,
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-api" % slf4j18Version,
      "org.scalatest" %% "scalatest" % scalatestVersion % Test
    ),
    crossScalaVersions := scala2Versions
  )

lazy val migration = project.in(file("migration"))
  .dependsOn(coreJVM)
  .settings(
    name := "scribe.migration",
    publishArtifact in Test := false,
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % scalatestVersion % Test
    ),
    crossScalaVersions := allScalaVersions
  )

lazy val slack = project.in(file("slack"))
  .settings(
    name := "scribe-slack",
    crossScalaVersions := compatScalaVersions,
    libraryDependencies ++= Seq(
      "io.youi" %% "youi-client" % youiVersion
    )
  )
  .dependsOn(coreJVM)

lazy val logstash = project.in(file("logstash"))
  .settings(
    name := "scribe-logstash",
    crossScalaVersions := compatScalaVersions,
    libraryDependencies ++= Seq(
      "io.youi" %% "youi-client" % youiVersion
    )
  )
  .dependsOn(coreJVM)

lazy val benchmarks = project.in(file("benchmarks"))
  .dependsOn(coreJVM)
  .enablePlugins(JmhPlugin)
  .settings(
    publishArtifact := false,
    libraryDependencies ++= Seq(
      "org.apache.logging.log4j" % "log4j-api" % log4jVersion,
      "org.apache.logging.log4j" % "log4j-core" % log4jVersion,
      "com.lmax" % "disruptor" % disruptorVersion,
      "ch.qos.logback" % "logback-classic" % logbackVersion,
      "com.typesafe" % "config" % typesafeConfigVersion,
      "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
      "org.tinylog" % "tinylog" % tinyLogVersion,
      "org.log4s" %% "log4s" % log4sVersion
    )
  )