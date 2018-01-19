import sbtcrossproject.{crossProject, CrossType}

name in ThisBuild := "scribe"
organization in ThisBuild := "com.outr"
version in ThisBuild := "1.4.6"
scalaVersion in ThisBuild := "2.12.4"
crossScalaVersions in ThisBuild := List("2.12.4", "2.11.12")

publishTo in ThisBuild := sonatypePublishTo.value
sonatypeProfileName in ThisBuild := "com.outr"
publishMavenStyle in ThisBuild := true
licenses in ThisBuild := Seq("MIT" -> url("https://github.com/outr/scribe/blob/master/LICENSE"))
sonatypeProjectHosting in ThisBuild := Some(xerial.sbt.Sonatype.GithubHosting("outr", "scribe", "matt@outr.com"))
homepage in ThisBuild := Some(url("https://github.com/outr/scribe"))
scmInfo in ThisBuild := Some(
  ScmInfo(
    url("https://github.com/outr/scribe"),
    "scm:git@github.com:outr/scribe.git"
  )
)
developers in ThisBuild := List(
  Developer(id="darkfrog", name="Matt Hicks", email="matt@matthicks.", url=url("http://matthicks.com"))
)

lazy val scribe = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Full)
  .in(file("."))
  .settings(
    name := "scribe",
    libraryDependencies += scalaVersion("org.scala-lang" % "scala-reflect" % _).value,
    publishArtifact in Test := false
  )
  .nativeSettings(
    scalaVersion := "2.11.12",
    crossScalaVersions := Seq("2.11.12")
  )

lazy val scribeJS = scribe.js
lazy val scribeJVM = scribe.jvm
lazy val scribeNative = scribe.native

lazy val slf4j = project.in(file("slf4j"))
  .dependsOn(scribeJVM)
  .settings(
    name := "scribe-slf4j",
    publishArtifact in Test := false,
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-api" % "1.7.25",
      "org.scalatest" %% "scalatest" % "3.0.4" % Test
    )

  )

lazy val slack = project.in(file("slack"))
  .dependsOn(scribeJVM)
  .settings(
    name := "scribe-slack",
    publishArtifact in Test := false,
    libraryDependencies ++= Seq(
      "com.eed3si9n" %% "gigahorse-asynchttpclient" % "0.3.1",
      "com.lihaoyi" %% "upickle" % "0.5.1"
    )
  )


lazy val tests = crossProject(JVMPlatform, JSPlatform).crossType(CrossType.Full)
  .dependsOn(scribe)
  .settings(
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % Test,
    publish := {},
    publishLocal := {},
    publishArtifact := false
  )

lazy val testsJVM = tests.jvm
lazy val testsJS = tests.js