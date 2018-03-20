resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"
resolvers += "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("org.scala-js"       % "sbt-scalajs"              % "0.6.22")
addSbtPlugin("org.portable-scala" % "sbt-crossproject"         % "0.3.1")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "0.3.1")
addSbtPlugin("org.scala-native"   % "sbt-scala-native"         % "0.3.6")

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.1")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.0")
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")
addSbtPlugin("pl.project13.scala" % "sbt-jmh" % "0.3.2")