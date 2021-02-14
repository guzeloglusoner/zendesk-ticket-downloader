package controllers

import akka.{Done, NotUsed}
import akka.actor.{ActorSystem, Cancellable}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.ByteString
import models.{ZendeskTickets, ZendeskTicketsStream}
import play.api.Configuration
import play.api.cache.AsyncCacheApi
import play.api.http.HttpEntity
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import javax.inject.{Inject, _}
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success}

@Singleton
class ZenDeskController @Inject()(ws: WSClient,
                                  conf: Configuration,
                                  cc: ControllerComponents,
                                  cache: AsyncCacheApi)
                                 (implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  // The system coordinates actors and provides threads for them
  implicit val actorSystem = ActorSystem()
  // The materializer makes actors execute graphs
  implicit val materializer = ActorMaterializer()


  // Reading Config
  private val zendeskCursorBasedInitialURL = conf.get[String]("zendeskCursorBasedInitial.url")
  private val token = conf.get[String]("zendeskAccess.token")


  def startZendeskConsumer(timestamp: Long): Action[AnyContent] = Action {

    val initialResponse = ws
      .url(zendeskCursorBasedInitialURL.format(timestamp))
      .addHttpHeaders(("Authorization", s"Bearer $token"))
      .get()

    val cursorResponse: String => Future[WSResponse] = (cursor: String) => {
      ws
        .url(cursor)
        .addHttpHeaders(("Authorization", s"Bearer $token"))
        .get()
    }

    // get the latest cursor url
    val readCache = Flow[Unit].mapAsync(100)(_ => {
      val currentCache = cache.get[String]("cursorUrl")
      println(s"Read cache is: $currentCache")
      currentCache
    })

    // get zendesk tickets
    val getZendeskTickets = Flow[Option[String]].mapAsync(100) { lastCursorUrl =>
      println(s"Last URL is : ${lastCursorUrl.getOrElse("Soner")}")
      cursorResponse(lastCursorUrl.getOrElse("Soner"))
    }

    // De-serialization of ZendeskTickets
    val toZendeskTicketsStream = Flow[WSResponse].map(elem => (elem.json).as[ZendeskTicketsStream])

    val printTickets = Flow[ZendeskTicketsStream].map { elem =>
      elem.tickets.foreach(e => println(e.toString))
      elem
    }

    val setCache = Flow[ZendeskTicketsStream].map { elem =>
      println(s"Now caching : ${elem.after_url}")
      cache.set("cursorUrl", elem.after_url)
    }

    val lastZendeskTicketsStream: Future[ZendeskTicketsStream] =
      Source
        .fromFuture(initialResponse)
        .via(toZendeskTicketsStream)
        .runWith(Sink.last)

    lastZendeskTicketsStream.onComplete {
      case Failure(exception) => BadRequest
      case Success(value) =>
        println(s"First cache: ${value.after_url}")
        cache.set("cursorUrl", value.after_url)

        Source
          .tick(0 second, 15 second, ())
          .via(readCache)
          .via(getZendeskTickets)
          .via(toZendeskTicketsStream)
          .via(printTickets)
          .intersperse()
          .via(setCache)
          .runWith(Sink.ignore)
    }

    Ok("Selamun Aleykum")
  }
}
