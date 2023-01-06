// Scala versions
val scala213 = "2.13.10"

val scala212 = "2.12.17"

val scala211 = "2.11.12"

val scala3 = "3.2.1"

val scala2 = List(scala213, scala212, scala211)
val allScalaVersions = scala3 :: scala2
val compatScalaVersions = List(scala213, scala212)
val scalaJVMVersions = allScalaVersions
val scalaJSVersions = allScalaVersions
val scalaNativeVersions = allScalaVersions
val scalaAllExcept211Versions = List(scala213, scala212, scala3)

name := "scribe"
ThisBuild / organization := "com.outr"
ThisBuild / version := "3.10.7-SNAPSHOT"
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
val perfolationVersion: String = "1.2.9"
val sourcecodeVersion: String = "0.3.0"
val collectionCompatVersion: String = "2.9.0"
val moduloadVersion: String = "1.1.6"

val catsEffectVersion: String = "3.4.4"
val catsEffectTestingVersion: String = "1.5.0"

// JSON
val fabricVersion: String = "1.2.5"

// Testing
val scalaTestVersion: String = "3.2.14"

// SLF4J
val slf4jVersion: String = "1.7.36"
val slf4j2Version: String = "2.0.6"

// Config Dependencies
val profigVersion: String = "3.4.1"

// Slack and Logstash Dependencies
val youiVersion: String = "0.14.4"

// Benchmarking Dependencies
val log4jVersion: String = "2.19.0"
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
    coreJS, coreJVM, coreNative,
    catsJS, catsJVM, catsNative,
    fileJVM, fileNative,
    jsonJS, jsonJVM,
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
      "org.scalatest" %% "scalatest" % scalaTestVersion % Test
    ),
    libraryDependencies ++= (
      if (scalaVersion.value.startsWith("3.0")) {
        Nil
      } else {
        List("org.scala-lang.modules" %% "scala-collection-compat" % collectionCompatVersion)
      }
    ),
    Test / publishArtifact := false
  )
  .jsSettings(
    crossScalaVersions := scalaJSVersions
  )
  .jvmSettings(
    crossScalaVersions := scalaJVMVersions,
    libraryDependencies ++= Seq(
      "com.outr" %% "moduload" % moduloadVersion
    )
  )
  .nativeSettings(
    crossScalaVersions := scalaNativeVersions
  )

lazy val coreJS = core.js
lazy val coreJVM = core.jvm
lazy val coreNative = core.native

lazy val cats = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Full)
  .settings(
    name := "scribe-cats",
    crossScalaVersions := scalaAllExcept211Versions,
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-effect" % catsEffectVersion,
      "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
      "org.typelevel" %% "cats-effect-testing-scalatest" % catsEffectTestingVersion % "test"
    ),
    libraryDependencies ++= (
      if (scalaVersion.value.startsWith("3.0")) {
        Nil
      } else {
        List("org.scala-lang.modules" %% "scala-collection-compat" % collectionCompatVersion)
      }
    ),
    Test / publishArtifact := false
  )
  .dependsOn(core)

lazy val catsJS = cats.js
lazy val catsJVM = cats.jvm
lazy val catsNative = cats.native

lazy val fileModule = crossProject(JVMPlatform, NativePlatform)
  .crossType(CrossType.Full)
  .settings(
    name := "scribe-file",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % scalaTestVersion % Test
    )
  )
  .jvmSettings(
    crossScalaVersions := scalaJVMVersions
  )
  .nativeSettings(
    crossScalaVersions := scalaNativeVersions,
    nativeLinkStubs := true,
    test := {}
  )
  .dependsOn(core)

lazy val fileJVM = fileModule.jvm
lazy val fileNative = fileModule.native

lazy val json = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .settings(
    name := "scribe-json",
    libraryDependencies ++= Seq(
      "com.outr" %%% "fabric-parse" % fabricVersion,
      "org.scalatest" %% "scalatest" % scalaTestVersion % Test
    ),
    crossScalaVersions := List(scala213, scala212)
  )
  .jsSettings(
    crossScalaVersions := scalaJSVersions
  )
  .jvmSettings(
    crossScalaVersions := scalaJVMVersions
  )
  .dependsOn(core)

lazy val jsonJS = json.js
lazy val jsonJVM = json.jvm

lazy val slf4j = project.in(file("slf4j"))
  .dependsOn(coreJVM)
  .settings(
    name := "scribe-slf4j",
    Test / publishArtifact := false,
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-api" % slf4jVersion,
      "org.scalatest" %% "scalatest" % scalaTestVersion % Test
    ),
    crossScalaVersions := scalaJVMVersions
  )

lazy val slf4j2 = project.in(file("slf4j2"))
  .dependsOn(coreJVM)
  .settings(
    name := "scribe-slf4j2",
    Test / publishArtifact := false,
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-api" % slf4j2Version,
      "org.scalatest" %% "scalatest" % scalaTestVersion % Test
    ),
    crossScalaVersions := scalaJVMVersions
  )

lazy val log4j = project.in(file("log4j"))
  .dependsOn(coreJVM)
  .settings(
    name := "scribe-log4j",
    Test / publishArtifact := false,
    libraryDependencies ++= Seq(
      "org.apache.logging.log4j" % "log4j-api" % log4jVersion,
      "org.apache.logging.log4j" % "log4j-core" % log4jVersion,
      "org.scalatest" %% "scalatest" % scalaTestVersion % Test
    ),
    crossScalaVersions := scalaJVMVersions
  )

lazy val migration = project.in(file("migration"))
  .dependsOn(coreJVM)
  .settings(
    name := "scribe-migration",
    Test / publishArtifact := false,
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % scalaTestVersion % Test
    ),
    crossScalaVersions := scalaJVMVersions
  )

lazy val config = project.in(file("config"))
  .dependsOn(migration)
  .settings(
    name := "scribe-config",
    Test / publishArtifact := false,
    libraryDependencies ++= Seq(
      "com.outr" %% "profig" % profigVersion,
      "org.scalatest" %% "scalatest" % scalaTestVersion % Test
    ),
    crossScalaVersions := compatScalaVersions
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
  .dependsOn(fileJVM, catsJVM)
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
      "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
      "org.tinylog" % "tinylog" % tinyLogVersion,
      "org.log4s" %% "log4s" % log4sVersion,
      "org.typelevel" %% "log4cats-slf4j" % log4catsVersion,
      "co.fs2" %% "fs2-core" % fs2Version
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
