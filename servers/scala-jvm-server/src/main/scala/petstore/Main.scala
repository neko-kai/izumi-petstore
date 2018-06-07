package petstore

import cats.data.{Kleisli, OptionT}
import cats.effect.IO
import com.github.pshirshov.izumi.idealingua.runtime.circe.IRTOpinionatedMarshalers
import com.github.pshirshov.izumi.idealingua.runtime.rpc.{IRTServerMultiplexor, IRTServiceResult, IRTWithResult}
import com.github.pshirshov.izumi.idealingua.runtime.rpc.http4s.RuntimeHttp4s
import org.http4s.dsl.io
import org.http4s.server.AuthMiddleware
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.{Request, Uri}
import petstore.api.{HelloServiceServer, HelloServiceWrapped, HelloWorld}

import scala.language.higherKinds

final case class DummyContext(ip: String)


class HelloImpl[R[_] : IRTServiceResult, C] extends HelloServiceServer[R, C] with IRTWithResult[R] {
  override protected def _ServiceResult: IRTServiceResult[R] = implicitly

  override def hello(ctx: C): Result[HelloWorld] = _Result {
    HelloWorld(s"Hi!")
  }
}

object Main {


  import com.github.pshirshov.izumi.idealingua.runtime.cats.RuntimeCats._

  private val helloDispatcher = HelloServiceWrapped.serverUnsafe(new HelloImpl[IO, DummyContext]())
  private val dispatchers = List(helloDispatcher)

  private final val codecs = List(HelloServiceWrapped)
  private final val marsh = IRTOpinionatedMarshalers(codecs)

  final val serverMuxer = new IRTServerMultiplexor(dispatchers)

  final val authUser: Kleisli[OptionT[IO, ?], Request[IO], DummyContext] =
    Kleisli {
      request: Request[IO] =>
        val context = DummyContext(request.remoteAddr.getOrElse("0.0.0.0"))

        OptionT.liftF(IO(context))
    }

  final val port = 8080
  final val host = "localhost"
  final val baseUri = Uri(Some(Uri.Scheme.http), Some(Uri.Authority(host = Uri.RegName(host), port = Some(port))))
  final val rt = new RuntimeHttp4s[IO]
  final val ioService = rt.httpService(serverMuxer, AuthMiddleware(authUser), marsh, io)

  def main(args: Array[String]): Unit = {

    val builder = BlazeBuilder[IO]
      .bindHttp(port, host)
      .mountService(ioService, "/")
      .start

    builder.unsafeRunAsync {
      case Right(server) =>
        Thread.currentThread().join()

      case Left(error) =>
        throw error
    }
  }
}
