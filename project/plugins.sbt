
resolvers += Resolver.defaultLocal
resolvers += Resolver.publishMavenLocal

resolvers += Opts.resolver.sonatypeReleases
resolvers += Opts.resolver.sonatypeSnapshots

resolvers += Resolver.typesafeRepo("releases")

addSbtPlugin("com.arpnetworking" % "sbt-typescript" % "0.4.2")

val izumi_version = "0.6.1-SNAPSHOT"
addSbtPlugin("com.github.pshirshov.izumi.r2" % "sbt-idealingua" % izumi_version)
addSbtPlugin("com.github.pshirshov.izumi.r2" % "sbt-izumi" % izumi_version)
addSbtPlugin("com.github.pshirshov.izumi.r2" % "sbt-izumi-deps" % izumi_version)

