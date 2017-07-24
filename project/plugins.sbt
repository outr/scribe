resolvers += "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/"

val scalaJSVersion = Option(System.getenv("SCALAJS_VERSION")).getOrElse("0.6.18")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.8.0")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % scalaJSVersion)
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "1.1")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.1")

{
  if (scalaJSVersion.startsWith("0.6."))
    Seq(addSbtPlugin("org.scala-native" % "sbt-scalajs-crossproject" % "0.2.0"))
  else
    Nil
}