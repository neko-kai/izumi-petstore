import com.github.pshirshov.izumi.idealingua.translator.IDLLanguage
import com.github.pshirshov.izumi.idealingua.translator.TypespaceCompiler.CompilerOptions
import com.github.pshirshov.izumi.sbt.IdealinguaPlugin.Keys.{idlDefaultExtensionsScala, idlDefaultExtensionsTypescript}
import com.github.pshirshov.izumi.sbt.IdealinguaPlugin.{Invokation, Mode}
import com.github.pshirshov.izumi.sbt.deps.IzumiDeps.{R, V}
import com.github.pshirshov.izumi.sbt.plugins.IzumiConvenienceTasksPlugin.Keys.defaultStubPackage
import sbt.Keys.sourceDirectories

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

    , libraryDependencies ++= Seq(
      Izumi.R.idealingua_model
      , Izumi.R.idealingua_runtime_rpc
      , Izumi.R.idealingua_runtime_rpc_http4s
      , Izumi.R.idealingua_runtime_rpc_cats
      , Izumi.R.idealingua_extension_rpc_format_circe
    )
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
  // manual hack because sbt-typescript doesn't work for some reason
  override val settings = Seq(
    sourceDirectory in Compile := baseDirectory.value / "src" / "main" / "typescript"
    , sourceDirectories in Compile := Seq((sourceDirectory in Compile).value)

    , compile in Compile := Def.task {
      val _ = (managedSources in Compile in petstoreApi).value
      // i'm very sorry, but its the fault of typescript's retarded `common root` logic, not mine!

      val tsSourceRoot = (sourceDirectory in Compile).value
      val tsGeneratedSources = ((resourceManaged in petstoreApi).value / "main" / "typescript").asFile.getAbsoluteFile

      val tsTargetSourceRoot = (sourceManaged in Compile).value

      println(tsGeneratedSources)
      println(tsTargetSourceRoot)
      IO.copyDirectory(tsGeneratedSources, tsTargetSourceRoot, overwrite = true)
      IO.copyDirectory(tsSourceRoot, tsTargetSourceRoot, overwrite = true)

      val res = sys.process.stringSeqToProcess(Seq("tsc", "-p", tsTargetSourceRoot.getAbsolutePath)).!
      if (res != 0)
        throw new RuntimeException("fuck")

      sbt.internal.inc.Analysis.Empty
    }.value
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

lazy val scalaJvmClient = inClients.as.module
  .dependsOn(petstoreApi)

lazy val allProjects: Seq[ProjectReference] = Seq(
  petstoreApi
  , scalaJvmServer
  , scalaJvmClient
  , typescriptNodeClient
)

lazy val `izumi-petstore` = inRoot.as.root.transitiveAggregateSeq(allProjects)
