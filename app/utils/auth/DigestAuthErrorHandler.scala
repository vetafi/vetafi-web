package utils.auth

import com.mohiva.play.silhouette.api.actions.SecuredErrorHandler
import play.api.mvc.{ RequestHeader, Result }
import play.api.mvc.Results.{ Unauthorized, Forbidden }
import scala.concurrent.Future

/**
 * When twilio requests our PDF document, it will initially do so with no auth.
 * This response will indicate to twilio to try again with digest auth.
 */
class DigestAuthErrorHandler extends SecuredErrorHandler {

  override def onNotAuthorized(implicit request: RequestHeader): Future[Result] = {
    Future.successful(Unauthorized.withHeaders("WWW-Authenticate" -> "Digest realm=twilio"))
  }

  override def onNotAuthenticated(implicit request: RequestHeader): Future[Result] = {
    Future.successful(Forbidden)
  }
}
