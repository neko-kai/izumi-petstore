import com.github.pshirshov.izumi.sbt.deps.IzumiDeps.{R, V}
import com.github.pshirshov.izumi.sbt.plugins.IzumiConvenienceTasksPlugin.Keys.defaultStubPackage

enablePlugins(IzumiGitEnvironmentPlugin)

name := "izumi-petstore"

defaultStubPackage in Global := Some("petstore")
fork in Global := false

val GlobalSettings: DefaultGlobalSettingsGroup = new DefaultGlobalSettingsGroup {
  override val settings: Seq[sbt.Setting[_]] = Seq(
    scalaVersion := "2.12.6"
    , crossScalaVersions := Seq(
      V.scala_212
    )
    , addCompilerPlugin(R.kind_projector)

    , scalacOptions += "-Ypartial-unification"

    , libraryDependencies ++= Seq(
      Izumi.R.idealingua_model
      , Izumi.R.idealingua_runtime_rpc
      , Izumi.R.idealingua_runtime_rpc_http4s
      , Izumi.R.idealingua_runtime_rpc_cats
      , Izumi.R.idealingua_extension_rpc_format_circe
    )
    , updateOptions ~= {
      _.withCachedResolution(true)
        .withLatestSnapshots(true)
        .withInterProjectFirst(true)
    }
  )
}

val ApiSettings: SettingsGroup = new SettingsGroup {
  override val plugins = Set(IdealinguaPlugin) // enable Izumi-IDL compiler
}

lazy val inRoot = In(".")
  .settings(GlobalSettings)

lazy val inBackend = In("backend")
  .settings(GlobalSettings)

lazy val inApi = In("api")
  .settings(GlobalSettings)
  .settings(ApiSettings)

// API
lazy val petstoreApi = inApi.as.module

// Servers
lazy val scalaJvmServer = inBackend.as.module
  .dependsOn(petstoreApi)

// Clients


lazy val allProjects: Seq[ProjectReference] = Seq(scalaJvmServer)

lazy val `izumi-petstore` = inRoot.as.root.transitiveAggregateSeq(allProjects)
