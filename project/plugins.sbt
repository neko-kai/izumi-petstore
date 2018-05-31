
resolvers += Resolver.defaultLocal
resolvers += Resolver.publishMavenLocal

resolvers += Opts.resolver.sonatypeReleases
resolvers += Opts.resolver.sonatypeSnapshots

val izumi_version = "0.6.1-SNAPSHOT"
addSbtPlugin("com.github.pshirshov.izumi.r2" % "sbt-idealingua" % izumi_version)
addSbtPlugin("com.github.pshirshov.izumi.r2" % "sbt-izumi" % izumi_version)
addSbtPlugin("com.github.pshirshov.izumi.r2" % "sbt-izumi-deps" % izumi_version)

