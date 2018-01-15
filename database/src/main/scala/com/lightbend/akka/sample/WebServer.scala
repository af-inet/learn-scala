import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn
import scala.collection.mutable._

case class StorageValue(value: MutableList[Byte])

case class StoragePair (key: String,
                         value: StorageValue)

object WebServer {

  def storage(key: String): ToResponseMarshallable = {
    HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>stored: $key</h1>")
  }

  def main(args: Array[String]) {
    implicit val system: ActorSystem = ActorSystem("my-system")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher
    var state: Map[String, StorageValue] = Map()

    val route =
      pathPrefix("storage") {
        path(Segment) { key =>
          post {
            extractDataBytes { dataBytes =>
              val bucket: MutableList[Byte] = new MutableList[Byte]()
              dataBytes.runForeach { bytestring =>
                bytestring.foreach { b =>
                  bucket += b
                }
              }
              val value = StorageValue(bucket)
              state += (key -> value)
              complete(storage(key))
            }
          } ~ get {
            val data = state(key).value
            complete(HttpEntity(data.toArray))
          }
        }
      }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    println(s"listening on port *8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}