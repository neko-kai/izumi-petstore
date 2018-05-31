import com.arpnetworking.sbt.typescript.Import.TypescriptKeys
import com.github.pshirshov.izumi.idealingua.translator.IDLLanguage
import com.github.pshirshov.izumi.idealingua.translator.TypespaceCompiler.CompilerOptions
import com.github.pshirshov.izumi.sbt.IdealinguaPlugin.Keys.{idlDefaultExtensionsGolang, idlDefaultExtensionsScala, idlDefaultExtensionsTypescript}
import com.github.pshirshov.izumi.sbt.IdealinguaPlugin.{Invokation, Mode}
import com.github.pshirshov.izumi.sbt.deps.IzumiDeps.{R, V}
import com.github.pshirshov.izumi.sbt.plugins.IzumiConvenienceTasksPlugin.Keys.defaultStubPackage
import com.typesafe.sbt.jse.JsEngineImport.JsEngineKeys.EngineType

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

  override val settings = Seq(
    IdealinguaPlugin.Keys.compilationTargets := Seq(
//      Invokation(CompilerOptions(IDLLanguage.Scala, idlDefaultExtensionsScala.value), Mode.Sources)
//      ,
      Invokation(CompilerOptions(IDLLanguage.Typescript, idlDefaultExtensionsTypescript.value), Mode.Sources)
    )
  )
}

val TypeScriptSettings: SettingsGroup = new SettingsGroup {
  override val plugins = Set(SbtWeb, SbtTypescript)

  override val settings = Seq(
    JsEngineKeys.engineType := EngineType.Node
    , TypescriptKeys.configFile := "clients/typescript-node-client/src/main/typescript/tsconfig.json"
  )
}

lazy val inRoot = In(".")
  .settings(GlobalSettings)

lazy val inServers = In("servers")
  .settings(GlobalSettings)

lazy val inClients = In("clients")

lazy val inApi = In("api")
  .settings(GlobalSettings)
  .settings(ApiSettings)

// API
lazy val petstoreApi = inApi.as.module

// Servers
lazy val scalaJvmServer = inServers.as.module
  .dependsOn(petstoreApi)

// Clients
lazy val typescriptNodeClient = inClients.as.module
  .dependsOn(petstoreApi)
  .settings(TypeScriptSettings)

lazy val allProjects: Seq[ProjectReference] = Seq(
  petstoreApi
  , scalaJvmServer
  , typescriptNodeClient
)

lazy val `izumi-petstore` = inRoot.as.root.transitiveAggregateSeq(allProjects)
