name := "scribe"
organization in ThisBuild := "com.outr"
version in ThisBuild := "2.0.0-SNAPSHOT"
scalaVersion in ThisBuild := "2.12.4"
crossScalaVersions in ThisBuild := List("2.12.4", "2.11.12")

lazy val root = project.in(file("."))
  .aggregate(js, jvm, slf4j, slack, benchmarks)
  .settings(
    publish := {},
    publishLocal := {}
  )

lazy val scribe = crossProject.in(file("."))
    .settings(
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-reflect" % scalaVersion.value,
        "org.scalatest" %%% "scalatest" % "3.0.3" % "test"
      )
    )

lazy val js = scribe.js
lazy val jvm = scribe.jvm

lazy val core = crossProject.in(file("core"))
    .settings(
      name := "scribe-core",
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-reflect" % scalaVersion.value,
        "com.typesafe.akka" %% "akka-actor" % "2.5.9",
        "org.scalatest" %%% "scalatest" % "3.0.3" % "test"
      )
    )

lazy val coreJs = core.js
lazy val coreJvm = core.jvm

lazy val slf4j = project.in(file("slf4j"))
  .dependsOn(jvm)
  .settings(
    name := "scribe-slf4j",
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-api" % "1.7.25",
      "org.scalatest" %% "scalatest" % "3.0.3" % "test"
    )
  )

lazy val slack = project.in(file("slack"))
  .dependsOn(jvm)
  .settings(
    name := "scribe-slack",
    libraryDependencies ++= Seq(
      "com.eed3si9n" %% "gigahorse-asynchttpclient" % "0.3.1",
      "com.lihaoyi" %% "upickle" % "0.4.4"
    )
  )

lazy val benchmarks = project.in(file("benchmarks"))
  .dependsOn(jvm, coreJvm)
  .enablePlugins(JmhPlugin)
  .settings(
    publishArtifact := false,
    libraryDependencies ++= Seq(
      "org.apache.logging.log4j" % "log4j-api" % "2.10.0",
      "org.apache.logging.log4j" % "log4j-core" % "2.10.0",
      "com.lmax" % "disruptor" % "3.3.7"
    )
  )