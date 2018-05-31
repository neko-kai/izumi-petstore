import com.arpnetworking.sbt.typescript.Import.TypescriptKeys
import com.github.pshirshov.izumi.idealingua.translator.IDLLanguage
import com.github.pshirshov.izumi.idealingua.translator.TypespaceCompiler.CompilerOptions
import com.github.pshirshov.izumi.sbt.IdealinguaPlugin.Keys.{compilationTargets, idlDefaultExtensionsGolang, idlDefaultExtensionsScala, idlDefaultExtensionsTypescript}
import com.github.pshirshov.izumi.sbt.IdealinguaPlugin.{Invokation, Mode, Scope, compileSources}
import com.github.pshirshov.izumi.sbt.deps.IzumiDeps.{R, V}
import com.github.pshirshov.izumi.sbt.plugins.IzumiConvenienceTasksPlugin.Keys.defaultStubPackage
import com.typesafe.sbt.jse.JsEngineImport.JsEngineKeys.EngineType
import sbt.Keys.sourceDirectories
import sbt.io.{Path, PathFinder}

enablePlugins(IzumiGitEnvironmentPlugin)

name := "izumi-petstore"

resolvers += Opts.resolver.sonatypeReleases

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
      Invokation(CompilerOptions(IDLLanguage.Scala, idlDefaultExtensionsScala.value), Mode.Sources)

      , Invokation(CompilerOptions(IDLLanguage.Typescript, idlDefaultExtensionsTypescript.value), Mode.Sources)
    )
  )
}

val TypeScriptSettings: SettingsGroup = new SettingsGroup {
  override val plugins = Set(SbtWeb, SbtTypescript)

  override val settings = Seq(
    JsEngineKeys.engineType := EngineType.Node
    , sourceDirectory in TypescriptKeys.typescript := baseDirectory.value / "src" / "main" / "typescript"
    , sourceDirectories in TypescriptKeys.typescript := Seq((sourceDirectory in TypescriptKeys.typescript).value)

    , sourceGenerators in Compile += task {
      // i'm very sorry, but its the fault of ts's retarded `commonRoot` logic, not mine!
      val tsGeneratedSources = ((resourceManaged in petstoreApi).value / "main" / "typescript").asFile.getAbsoluteFile
      val tsSources = ((sourceDirectory in TypescriptKeys.typescript).value / "generated").asFile.getAbsoluteFile

      val sources = PathFinder(tsGeneratedSources).allPaths pair Path.rebase(tsGeneratedSources, tsSources)
      Seq()
    }
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
  .settings(
    TypescriptKeys.configFile in ThisProject := "clients/typescript-node-client/src/main/typescript/tsconfig.json"
    , TypescriptKeys.configFile in ThisBuild := "clients/typescript-node-client/src/main/typescript/tsconfig.json"
    , TypescriptKeys.configFile in Global := "clients/typescript-node-client/src/main/typescript/tsconfig.json"
    , TypescriptKeys.configFile in Compile := "clients/typescript-node-client/src/main/typescript/tsconfig.json"
    , TypescriptKeys.configFile in TypescriptKeys.typescript := "clients/typescript-node-client/src/main/typescript/tsconfig.json"
  )

lazy val allProjects: Seq[ProjectReference] = Seq(
  petstoreApi
  , scalaJvmServer
  , typescriptNodeClient
)

lazy val `izumi-petstore` = inRoot.as.root.transitiveAggregateSeq(allProjects)
