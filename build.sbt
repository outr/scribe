// Scala versions
val scala213 = "2.13.14"

val scala212 = "2.12.19"

val scala3 = "3.3.3"

val allScalaVersions = List(scala213, scala212, scala3)

name := "scribe"
ThisBuild / organization := "com.outr"
ThisBuild / version := "3.15.0"
ThisBuild / scalaVersion := scala213
ThisBuild / scalacOptions ++= Seq("-unchecked", "-deprecation")
ThisBuild / javacOptions ++= Seq("-source", "1.8", "-target", "1.8")
ThisBuild / resolvers ++= Resolver.sonatypeOssRepos("releases")
ThisBuild / resolvers += Resolver.JCenterRepository
//javaOptions in run += "-agentpath:/opt/YourKit-JavaProfiler-2020.9/bin/linux-x86-64/libyjpagent.so=delay=10000,listen=all"

ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"
ThisBuild / sonatypeRepository := "https://s01.oss.sonatype.org/service/local"
ThisBuild / publishTo := sonatypePublishToBundle.value
ThisBuild / sonatypeProfileName := "com.outr"
ThisBuild / licenses := Seq("MIT" -> url("https://github.com/outr/scribe/blob/master/LICENSE"))
ThisBuild / sonatypeProjectHosting := Some(xerial.sbt.Sonatype.GitHubHosting("outr", "scribe", "matt@outr.com"))
ThisBuild / homepage := Some(url("https://github.com/outr/scribe"))
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/outr/scribe"),
    "scm:git@github.com:outr/scribe.git"
  )
)
ThisBuild / developers := List(
  Developer(id="darkfrog", name="Matt Hicks", email="matt@matthicks.com", url=url("https://matthicks.com"))
)
ThisBuild / parallelExecution := false

ThisBuild / outputStrategy := Some(StdoutOutput)

// Core
val perfolationVersion: String = "1.2.11"

val sourcecodeVersion: String = "0.4.2"

val collectionCompatVersion: String = "2.12.0"

val moduloadVersion: String = "1.1.7"

val catsEffectVersion: String = "3.5.4"

val catsEffectTestingVersion: String = "1.5.0"

// JSON
val fabricVersion: String = "1.15.1"

val circeVersion = "0.14.9"

// Testing
val scalaTestVersion: String = "3.2.18"

// SLF4J
val slf4jVersion: String = "1.7.36"

val slf4j2Version: String = "2.0.13"

// Config Dependencies
val profigVersion: String = "3.4.1"

// Slack and Logstash Dependencies
val spiceVersion: String = "0.5.10"

// Benchmarking Dependencies
val log4jVersion: String = "2.23.1"

val disruptorVersion: String = "3.4.4"

val logbackVersion: String = "1.2.11"

val typesafeConfigVersion: String = "1.4.2"

val scalaLoggingVersion: String = "3.9.5"

val tinyLogVersion: String = "1.3.6"

val log4sVersion: String = "1.10.0"

val log4catsVersion: String = "2.3.2"

val fs2Version: String = "3.2.9"

// set source map paths from local directories to github path
val sourceMapSettings = List(
  scalacOptions ++= git.gitHeadCommit.value.map { headCommit =>
    val compilerJsSourceMapFlag = if (scalaVersion.value.startsWith("2.")) "-P:scalajs:mapSourceURI" else "-scalajs-mapSourceURI"    
    val local = baseDirectory.value.toURI
    val remote = s"https://raw.githubusercontent.com/outr/scribe/$headCommit/"
    s"$compilerJsSourceMapFlag:$local->$remote"
  }
)

lazy val root = project.in(file("."))
  .aggregate(
    core.js, core.jvm, core.native,
    // TODO: Re-enable cats.native when cats-effect supports ScalaNative 0.5
    cats.js, cats.jvm, //cats.native,
    fileModule.jvm, fileModule.native,
    json.js, json.jvm, jsonFabric.js, jsonFabric.jvm, jsonCirce.js, jsonCirce.jvm,
    slf4j, slf4j2, log4j, migration, config, slack, logstash
  )
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
      "org.scalatest" %%% "scalatest" % scalaTestVersion % Test
    ),
    libraryDependencies ++= (
      if (scalaVersion.value.startsWith("2.12.")) {
        List("org.scala-lang.modules" %%% "scala-collection-compat" % collectionCompatVersion)
      } else {
        Nil
      }
    ),
    Test / publishArtifact := false,
    crossScalaVersions := allScalaVersions
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "com.outr" %%% "moduload" % moduloadVersion
    )
  )
  .jsSettings(
    libraryDependencies ++= Seq(
      "io.github.cquiroz" %%% "scala-java-time" % "2.6.0" % Test,
      "io.github.cquiroz" %%% "scala-java-time-tzdb" % "2.6.0" % Test
    )
  )
  .nativeSettings(
    coverageEnabled := false
  )

lazy val cats = crossProject(JVMPlatform, JSPlatform) //, NativePlatform)
  .crossType(CrossType.Full)
  .settings(
    name := "scribe-cats",
    crossScalaVersions := allScalaVersions,
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-effect" % catsEffectVersion,
      "org.scalatest" %%% "scalatest" % scalaTestVersion % Test,
      "org.typelevel" %%% "cats-effect-testing-scalatest" % catsEffectTestingVersion % "test"
    ),
    libraryDependencies ++= (
      if (scalaVersion.value.startsWith("2.12.")) {
        List("org.scala-lang.modules" %%% "scala-collection-compat" % collectionCompatVersion)
      } else {
        Nil
      }
    ),
    Test / publishArtifact := false
  )
  .dependsOn(core)

lazy val fileModule = crossProject(JVMPlatform, NativePlatform)
  .crossType(CrossType.Full)
  .settings(
    name := "scribe-file",
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest" % scalaTestVersion % Test
    ),
    crossScalaVersions := allScalaVersions
  )
  .nativeSettings(
    nativeConfig ~= {
      _.withLinkStubs(true)
    },
    test := {}
  )
  .dependsOn(core)

lazy val json = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .settings(
    name := "scribe-json",
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest" % scalaTestVersion % Test
    ),
    crossScalaVersions := allScalaVersions
  )
  .dependsOn(core)

lazy val jsonFabric = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .settings(
    name := "scribe-json-fabric",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "fabric-io" % fabricVersion,
      "org.scalatest" %%% "scalatest" % scalaTestVersion % Test
    ),
    crossScalaVersions := allScalaVersions
  )
  .dependsOn(json)

lazy val jsonCirce = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .settings(
    name := "scribe-json-circe",
    libraryDependencies ++= Seq(
      "io.circe" %%% "circe-core" % circeVersion,
      "io.circe" %%% "circe-parser" % circeVersion,
      "io.circe" %%% "circe-generic" % circeVersion,
      "org.scalatest" %%% "scalatest" % scalaTestVersion % Test
    ),
    crossScalaVersions := allScalaVersions
  )
  .dependsOn(json)

lazy val slf4j = project.in(file("slf4j"))
  .dependsOn(core.jvm)
  .settings(
    name := "scribe-slf4j",
    Test / publishArtifact := false,
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-api" % slf4jVersion,
      "org.scalatest" %%% "scalatest" % scalaTestVersion % Test
    ),
    crossScalaVersions := allScalaVersions
  )

lazy val slf4j2 = project.in(file("slf4j2"))
  .dependsOn(core.jvm)
  .settings(
    name := "scribe-slf4j2",
    Test / publishArtifact := false,
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-api" % slf4j2Version,
      "org.scalatest" %%% "scalatest" % scalaTestVersion % Test
    ),
    crossScalaVersions := allScalaVersions
  )

lazy val log4j = project.in(file("log4j"))
  .dependsOn(core.jvm)
  .settings(
    name := "scribe-log4j",
    Test / publishArtifact := false,
    libraryDependencies ++= Seq(
      "org.apache.logging.log4j" % "log4j-api" % log4jVersion,
      "org.apache.logging.log4j" % "log4j-core" % log4jVersion,
      "org.scalatest" %%% "scalatest" % scalaTestVersion % Test
    ),
    crossScalaVersions := allScalaVersions
  )

lazy val migration = project.in(file("migration"))
  .dependsOn(core.jvm)
  .settings(
    name := "scribe-migration",
    Test / publishArtifact := false,
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest" % scalaTestVersion % Test
    ),
    crossScalaVersions := allScalaVersions
  )

lazy val config = project.in(file("config"))
  .dependsOn(migration)
  .settings(
    name := "scribe-config",
    Test / publishArtifact := false,
    libraryDependencies ++= Seq(
      "com.outr" %%% "profig" % profigVersion,
      "org.scalatest" %%% "scalatest" % scalaTestVersion % Test
    ),
    crossScalaVersions := allScalaVersions
  )

lazy val slack = project.in(file("slack"))
  .settings(
    name := "scribe-slack",
    crossScalaVersions := List(scala213, scala3),
    libraryDependencies ++= Seq(
      "com.outr" %% "spice-client-okhttp" % spiceVersion
    )
  )
  .dependsOn(core.jvm)

lazy val logstash = project.in(file("logstash"))
  .settings(
    name := "scribe-logstash",
    crossScalaVersions := List(scala213, scala3),
    libraryDependencies ++= Seq(
      "com.outr" %% "spice-client-okhttp" % spiceVersion
    )
  )
  .dependsOn(core.jvm)

lazy val benchmarks = project.in(file("benchmarks"))
  .dependsOn(fileModule.jvm, cats.jvm)
  .enablePlugins(JmhPlugin)
  .settings(
    fork := true,
    publishArtifact := false,
    libraryDependencies ++= Seq(
      "org.apache.logging.log4j" % "log4j-api" % log4jVersion,
      "org.apache.logging.log4j" % "log4j-core" % log4jVersion,
      "com.lmax" % "disruptor" % disruptorVersion,
      "ch.qos.logback" % "logback-classic" % logbackVersion,
      "com.typesafe" % "config" % typesafeConfigVersion,
      "com.typesafe.scala-logging" %%% "scala-logging" % scalaLoggingVersion,
      "org.tinylog" % "tinylog" % tinyLogVersion,
      "org.log4s" %%% "log4s" % log4sVersion,
      "org.typelevel" %%% "log4cats-slf4j" % log4catsVersion,
      "co.fs2" %%% "fs2-core" % fs2Version
    )
  )

lazy val docs = project
  .in(file("documentation"))
  .dependsOn(core.jvm)
  .enablePlugins(MdocPlugin)
  .settings(
    mdocVariables := Map(
      "VERSION" -> version.value
    ),
    mdocOut := file(".")
  )
