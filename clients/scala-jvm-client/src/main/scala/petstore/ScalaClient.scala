package petstore

import cats.effect._
import com.github.pshirshov.izumi.idealingua.runtime.circe.IRTOpinionatedMarshalers
import com.github.pshirshov.izumi.idealingua.runtime.rpc.http4s.RuntimeHttp4s
import org.http4s.dsl.io._
import org.http4s.client.blaze.Http1Client
import petstore.api.HelloServiceWrapped
import cats.implicits._
import com.github.pshirshov.izumi.idealingua.runtime.cats.RuntimeCats._
import fs2.StreamApp.ExitCode

object ScalaClient {
  final val rt = new RuntimeHttp4s[IO]
  final val host = uri("http://localhost:8080")
  final val marsh = IRTOpinionatedMarshalers(List(HelloServiceWrapped))
}

object ScalaClientApp {
  import ScalaClient._

  def main(args: Array[String]): Unit =
    (for {
      httpClient <- Http1Client[IO]()

      clientDispatcher = rt.httpClient(httpClient, marsh)(rt.requestBuilder(host))

      helloworldClient = HelloServiceWrapped.clientUnsafe(clientDispatcher)

      helloMsg <- helloworldClient.hello()
      _ <- IO(println(helloMsg))
    } yield ()).unsafeRunSync

}
