package models

import play.api.libs.json.{JsPath, Json, Reads}
import play.api.libs.functional.syntax._

case class ZendeskTicketsStream(tickets: Seq[ZendeskTickets],
                                after_url: String,
                                after_cursor: String,
                                end_of_stream: Boolean)
object ZendeskTicketsStream {
  implicit  val formatZendeskTicketsStream = Json.format[ZendeskTicketsStream]
}
