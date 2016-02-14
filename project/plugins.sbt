addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.4.6")
addSbtPlugin("com.typesafe.sbt"  % "sbt-play-enhancer" % "1.1.0")
addSbtPlugin("com.typesafe.sbt"  % "sbt-coffeescript" % "1.0.0")

resolvers += Resolver.url("GitHub repository", url("http://shaggyyeti.github.io/releases"))(Resolver.ivyStylePatterns)

addSbtPlugin("default" % "sbt-sass" % "0.1.9")
