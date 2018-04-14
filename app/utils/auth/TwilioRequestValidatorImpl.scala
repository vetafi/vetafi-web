package utils.auth

import javax.inject.Inject

import com.twilio.security.RequestValidator
import org.log4s.getLogger
import play.api.Configuration
import play.api.mvc.{ AnyContent, Request }
import utils.secrets.SecretsManager

class TwilioRequestValidatorImpl @Inject() (
  configuration: Configuration,
  secretsManager: SecretsManager) extends TwilioRequestValidator {

  private[this] val logger = getLogger

  lazy val authToken: String = secretsManager.getSecretUtf8(
    configuration.get[String]("twilio.authTokenSecretName"))

  def validateBodyParams(params: Map[String, Seq[String]], signature: String, uri: String): Boolean = {
    import scala.collection.JavaConversions._

    val requestValidator: RequestValidator = new RequestValidator(authToken)

    val paramsMap: Map[String, String] = params.toSeq.map {
      case (k, v) =>
        (k,
          v.reduceOption(_ + _) match {
            case None => ""
            case Some(value) => value
          })
    }.toMap

    if (requestValidator.validate(
      uri,
      paramsMap,
      signature)) {
      logger.info("Validation succeeded.")
      true
    } else {
      logger.info("Validation failed.")
      false
    }
  }

  /**
   * Reconstruct the originally requested URL based on the headers.
   * This is specific to being behind a AWS ELB.
   *
   * @param request
   * @return
   */
  def getOriginalUrlOfRequest(request: Request[AnyContent]): String = {

    val xProtoOpt = request.headers.get("x-forwarded-proto")
    val hostOpt = request.headers.get("host")

    s"${xProtoOpt.getOrElse("")}://${hostOpt.getOrElse("")}${request.uri}"
  }

  def maybeValidateSignature(signatue: String, request: Request[AnyContent]): Boolean = {
    val bodyParamsOpt: Option[Map[String, Seq[String]]] = request.body.asFormUrlEncoded

    bodyParamsOpt match {
      case None =>
        logger.info("Request did not contain form url encoded data.")
        false
      case Some(bodyParams) =>
        validateBodyParams(bodyParams, signatue, getOriginalUrlOfRequest(request))
    }
  }

  override def authenticate[B <: AnyContent](request: Request[B]): Boolean = {
    logger.info("Validating request: " + request.toString() + " " +
      request.uri + " " +
      request.body + " " +
      request.headers.toSimpleMap.toString())

    val signatureOpt = request.headers.get("x-twilio-signature")

    signatureOpt match {
      case Some(signature) => maybeValidateSignature(signature, request)
      case None =>
        logger.info("Request did not set x-twilio-signature")
        false
    }
  }
}
