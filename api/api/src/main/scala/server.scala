import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import scala.io.StdIn


object Server {
  def main(args: Array[String]) {

    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    val route =
      path(Segment / "view") { (link) =>
        post {
          formFields("user") { user =>
            complete("Usuario: " + user + " visualizou: " + link + "\n")
          }
        }
      } ~
      path(Segment / "similar") { (link) =>
        get {
            complete("Similares a: " + link + " : " + "\n")
        }
      } ~
      path("") {
        delete {
          complete("Deletado!\n")
        }
      }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}
