import sbt._

object D {
  val circeVersion = "0.9.3"

  val circe: Seq[ModuleID] = Seq(
    "io.circe" %% "circe-core"
    , "io.circe" %% "circe-generic"
    , "io.circe" %% "circe-generic-extras"
    , "io.circe" %% "circe-parser"
    , "io.circe" %% "circe-java8"
  ).map(_ % circeVersion)

  val cats_version = "1.1.0"
  val cats_core = "org.typelevel" %% "cats-core" % cats_version

  val http4s_version = "0.18.0"
  val http4s_client = "org.http4s" %% "http4s-blaze-client" % http4s_version
  val http4s_all: Seq[ModuleID] = Seq(
    "org.http4s" %% "http4s-dsl"
    , "org.http4s" %% "http4s-circe"
    , "org.http4s" %% "http4s-blaze-server"
  ).map(_ % http4s_version) ++ Seq(http4s_client)
}
