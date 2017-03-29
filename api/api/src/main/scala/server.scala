import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import scala.io.StdIn

import org.apache.kafka.common.serialization.StringSerializer
import cakesolutions.kafka.KafkaProducer
import cakesolutions.kafka.KafkaProducerRecord
import cakesolutions.kafka.KafkaProducer.Conf

object Server {
  def main(args: Array[String]) {

    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    val producer = KafkaProducer(
      Conf(new StringSerializer(), new StringSerializer(), bootstrapServers = "localhost:9092")
    )

    val route =
      path(Segment / "view") { (link) =>
        post {
          formFields("user") { user =>
            val record = KafkaProducerRecord("test", Some(s"$user"), s"$link")
            producer.send(record)
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
