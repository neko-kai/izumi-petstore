
resolvers += Opts.resolver.sonatypeReleases

val coursier = "1.1.0-M3"
addSbtPlugin("io.get-coursier" % "sbt-coursier" % coursier)

val izumi_version = "0.5.14"
addSbtPlugin("com.github.pshirshov.izumi.r2" % "sbt-idealingua" % izumi_version)
addSbtPlugin("com.github.pshirshov.izumi.r2" % "sbt-izumi" % izumi_version)
addSbtPlugin("com.github.pshirshov.izumi.r2" % "sbt-izumi-deps" % izumi_version)
