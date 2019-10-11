package logic

import akka.actor.ActorSystem
import akka.http.scaladsl.server.RouteResult
import akka.http.scaladsl.server.{RequestContext, Route}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.HttpCookie
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.io.StdIn

object Boot extends App {

  implicit val system: ActorSystem = ActorSystem("my-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  type Bet = Int
  type UserId = String

  val users: Map[UserId, Bet] = Map.empty

  def d(rc: RequestContext): Future[RouteResult] = rc.complete(Future("_"))

  val route: Route =
    path("put1" / IntNumber) { id =>
      optionalCookie("userName") {
        case Some(v) =>
          get { ctx =>
            d(ctx)
          }
        case None =>
          get { ctx =>
            d(ctx)
          }
      }
    } ~
      path("put2") {
        get {
          optionalCookie("userName") { nameCookie =>
            complete(s"The logged in user is '${nameCookie.get.value}'")
          }
        }
      } ~
      path("put3") {
        get { ctx =>
          ctx.complete(s"The HTTP method is '${ctx.request.method}'")
        }
      } ~
      path("put0") {
        get {
          setCookie(HttpCookie("userName", value = "paul")) {
            complete("The user was logged in")
          }
          //          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>OK, no cookie</h1>"))
        }
      }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done
}