logLevel := Level.Warn

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"


addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.4.6")
addSbtPlugin("com.typesafe.sbt" % "sbt-play-enhancer" % "1.1.0")
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "4.0.0")
