package petstore

import cats.effect._
import com.github.pshirshov.izumi.idealingua.runtime.circe.IRTOpinionatedMarshalers
import com.github.pshirshov.izumi.idealingua.runtime.rpc.http4s.RuntimeHttp4s
import org.http4s.dsl.io._
import org.http4s.client.blaze.Http1Client
import petstore.api.HelloServiceWrapped
import cats.implicits._
import com.github.pshirshov.izumi.idealingua.runtime.cats.RuntimeCats._
import com.github.pshirshov.izumi.idealingua.runtime.rpc.{IRTServiceResult, IRTWrappedServiceDefinition, IRTWrappedUnsafeServiceDefinition}
import shapeless._
import shapeless.ops.hlist._

import scala.language.higherKinds

case class Marshallers[L <: HList](hlist: L) {
  import ScalaClient._

  def client[F[_]: Effect: IRTServiceResult, S <: IRTWrappedServiceDefinition with IRTWrappedUnsafeServiceDefinition](implicit select: Selector[L, S]): F[S#ServiceClient[F]] =
    for {
      httpClient <- Http1Client[F]()

      rt = new RuntimeHttp4s[F]

      clientDispatcher = rt.httpClient(httpClient, marsh)(rt.requestBuilder(host))

      client = (select(hlist): S).clientUnsafe(clientDispatcher)
    } yield
      client
}

object ScalaClient {
  final val rt = new RuntimeHttp4s[IO]
  final val host = uri("http://localhost:8080")
  final val marsh = IRTOpinionatedMarshalers(List(HelloServiceWrapped))
}

object ScalaClientApp {
  import ScalaClient._

  def main(args: Array[String]): Unit =
    (for {
      client <- Marshallers(HList(HelloServiceWrapped)).client[IO, HelloServiceWrapped.type]
      helloMsg <- client.hello()
      _ <- IO(println(helloMsg))
    } yield ()).unsafeRunSync

}
