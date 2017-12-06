package utils.auth

import javax.inject.Inject

import com.mohiva.play.silhouette.api.actions.SecuredErrorHandler
import com.mohiva.play.silhouette.impl.util.SecureRandomIDGenerator
import org.log4s.getLogger
import play.api.mvc.Results.{ Forbidden, Unauthorized }
import play.api.mvc.{ RequestHeader, Result }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * When twilio requests our PDF document, it will initially do so with no auth.
 * This response will indicate to twilio to try again with Basic auth.
 */
class BasicAuthErrorHandler @Inject() (secureRandomIDGenerator: SecureRandomIDGenerator)() extends SecuredErrorHandler {
  private[this] val logger = getLogger

  override def onNotAuthorized(implicit request: RequestHeader): Future[Result] = {
    logger.info("BasicAuthErrorHandler onNotAuthorized for: ")
    logger.info(request.headers.toSimpleMap.toString())
    logger.info(request.method)
    Future.successful(Forbidden)
  }

  /**
   * Issue the Basic Authentication challenge if they are not authenticated.
   */
  override def onNotAuthenticated(implicit request: RequestHeader): Future[Result] = {
    logger.info("BasicAuthErrorHandler onNotAuthenticated for: ")
    logger.info(request.headers.toSimpleMap.toString())
    logger.info(request.method)
    secureRandomIDGenerator.generate.map {
      nonce =>
        Unauthorized.withHeaders("WWW-Authenticate" ->
          s"""Basic realm="twilio"""")
    }
  }
}
