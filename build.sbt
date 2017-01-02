name := "scribe"
organization in ThisBuild := "com.outr"
version in ThisBuild := "1.3.0-SNAPSHOT"
scalaVersion in ThisBuild := "2.12.1"
crossScalaVersions in ThisBuild := List("2.12.1", "2.11.8")
sbtVersion in ThisBuild := "0.13.13"

lazy val root = project.in(file("."))
  .aggregate(js, jvm, slf4j)
  .settings(
    publish := {},
    publishLocal := {}
  )

lazy val scribe = crossProject.in(file("."))
    .settings(
      libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-reflect" % _),
      libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.1" % "test"
    )

lazy val js = scribe.js
lazy val jvm = scribe.jvm

lazy val slf4j = project.in(file("slf4j"))
  .dependsOn(jvm)
  .settings(
    name := "scribe-slf4j",
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-api" % "1.7.22",
      "org.scalatest" %% "scalatest" % "3.0.1" % "test"
    )
  )