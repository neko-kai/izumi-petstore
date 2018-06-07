package petstore

import cats.effect.IO
import com.github.pshirshov.izumi.idealingua.runtime.circe.IRTOpinionatedMarshalers
import com.github.pshirshov.izumi.idealingua.runtime.rpc.http4s.RuntimeHttp4s
import org.http4s.Uri
import org.http4s.client.blaze.Http1Client
import petstore.api.HelloServiceWrapped

class ScalaClient {
  import com.github.pshirshov.izumi.idealingua.runtime.cats.RuntimeCats._
  private val rt = new RuntimeHttp4s[IO]()
  private val uri: Uri = Uri.fromString("http://localhost:8080").right.get
  private val marsh = IRTOpinionatedMarshalers(List(HelloServiceWrapped))
  private val clientDispatcher = rt.httpClient(Http1Client[IO]().unsafeRunSync, marsh)(rt.requestBuilder(uri))
  final val helloworldClient = HelloServiceWrapped.clientUnsafe(clientDispatcher)
}
