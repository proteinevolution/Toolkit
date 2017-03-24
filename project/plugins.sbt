addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.13")

addSbtPlugin("com.typesafe.sbt"  % "sbt-play-enhancer" % "1.1.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-rjs" % "1.0.8")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.0")

addSbtPlugin("org.ensime" % "ensime-sbt" % "0.2.3")

resolvers += Resolver.url("GitHub repository", url("http://shaggyyeti.github.io/releases"))(Resolver.ivyStylePatterns)

addSbtPlugin("default" % "sbt-sass" % "0.1.9")

addSbtPlugin("com.vmunier" % "sbt-web-scalajs" % "1.0.3")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.13")

addSbtPlugin("com.arpnetworking" % "sbt-typescript" % "0.3.3")