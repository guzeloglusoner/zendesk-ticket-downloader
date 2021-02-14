/*@author: Soner Guzeloglu
*
* GeoRouter
*
* Routes the coming url
* */
package router

import javax.inject.Inject
import play.api.mvc._
import play.api.routing.SimpleRouter
import play.api.routing.sird._

class ZenDeskRouter @Inject()(controller: controllers.ZenDeskController)
  extends SimpleRouter {
  override def routes: PartialFunction[RequestHeader, Action[AnyContent]] = {
    case GET(p"/tickets" ? q"start_time=$start_time") => controller.startZendeskConsumer(start_time.toLong)
  }
}
