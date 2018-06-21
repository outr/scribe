import sbtcrossproject.{CrossType, crossProject}

name := "scribe"
organization in ThisBuild := "com.outr"
version in ThisBuild := "2.5.1-SNAPSHOT"
scalaVersion in ThisBuild := "2.12.6"
crossScalaVersions in ThisBuild := List("2.12.6", "2.11.12")
scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation")
resolvers in ThisBuild += Resolver.sonatypeRepo("releases")
resolvers in ThisBuild += Resolver.sonatypeRepo("snapshots")

publishTo in ThisBuild := sonatypePublishTo.value
sonatypeProfileName in ThisBuild := "com.outr"
publishMavenStyle in ThisBuild := true
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

// Core
val perfolationVersion: String = "1.0.2"
val scalatestVersion: String = "3.0.5"

// SLF4J
val slf4jVersion: String = "1.7.25"
val slf4j18Version: String = "1.8.0-beta2"

// Slack and Logstash Dependencies
val youiVersion: String = "0.9.0-M15"

// Benchmarking Dependencies
val log4jVersion: String = "2.11.0"
val disruptorVersion: String = "3.4.2"
val logbackVersion: String = "1.2.3"
val typesafeConfigVersion: String = "1.3.3"
val scalaLoggingVersion: String = "3.9.0"
val tinyLogVersion: String = "1.3.5"
val log4sVersion: String = "1.6.1"

// set source map paths from local directories to github path
val sourceMapSettings = List(
  scalacOptions ++= git.gitHeadCommit.value.map { headCommit =>
    val local = baseDirectory.value.toURI
    val remote = s"https://raw.githubusercontent.com/outr/scribe/$headCommit/"
    s"-P:scalajs:mapSourceURI:$local->$remote"
  }
)

lazy val root = project.in(file("."))
  .aggregate(
    macrosJS, macrosJVM, macrosNative,
    coreJS, coreJVM, coreNative,
    testsJS, testsJVM,
    slf4j, slf4j18, slack, logstash, benchmarks)
  .settings(
    name := "scribe",
    publish := {},
    publishLocal := {}
  )

lazy val macros = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Full)
  .in(file("macros"))
  .settings(
    name := "scribe-macros",
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    publishArtifact in Test := false
  )
  .nativeSettings(
    scalaVersion := "2.11.12",
  )

lazy val macrosJS = macros.js
lazy val macrosJVM = macros.jvm
lazy val macrosNative = macros.native

lazy val core = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Full)
  .in(file("core"))
  .dependsOn(macros)
  .settings(
    name := "scribe",
    libraryDependencies ++= Seq(
      "com.outr" %%% "perfolation" % perfolationVersion,
      "org.scala-lang" % "scala-reflect" % scalaVersion.value
    ),
    publishArtifact in Test := false
  )
  .jsSettings(sourceMapSettings)
  .nativeSettings(
    scalaVersion := "2.11.12",
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
    )
  )

lazy val slf4j18 = project.in(file("slf4j18"))
  .dependsOn(coreJVM)
  .settings(
    name := "scribe-swlf4j18",
    publishArtifact in Test := false,
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-api" % slf4j18Version,
      "org.scalatest" %% "scalatest" % scalatestVersion % Test
    )
  )

lazy val slack = project.in(file("slack"))
  .dependsOn(coreJVM)
  .settings(
    name := "scribe-slack",
    libraryDependencies ++= Seq(
      "io.youi" %% "youi-client" % youiVersion
    )
  )

lazy val logstash = project.in(file("logstash"))
  .dependsOn(coreJVM)
  .settings(
    name := "scribe-logstash",
    libraryDependencies ++= Seq(
      "io.youi" %% "youi-client" % youiVersion
    )
  )

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

lazy val tests = crossProject(JVMPlatform, JSPlatform).crossType(CrossType.Full)
  .dependsOn(core)
  .settings(
    libraryDependencies += "org.scalatest" %%% "scalatest" % scalatestVersion % Test,
    publish := {},
    publishLocal := {},
    publishArtifact := false
  )

lazy val testsJVM = tests.jvm
lazy val testsJS = tests.js
