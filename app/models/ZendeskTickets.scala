package models

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads}

case class ZendeskTickets(id: Int,
                          description: String,
                          created_at: String,
                          updated_at: String,
                         ){
  override def toString: String =
    s"Ticket ID: $id, Description: $description, Created Date: $created_at, Update Date: $updated_at"
}

object ZendeskTickets {
  implicit  val formatZendeskTickets = Json.format[ZendeskTickets]
}
