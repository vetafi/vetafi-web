package utils.auth

import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry
import com.mohiva.play.silhouette.api.{ AuthInfo, Logger, LoginInfo, RequestProvider }
import com.mohiva.play.silhouette.impl.providers.PasswordProvider
import models.TwilioUser
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.lang3.StringUtils
import org.log4s.getLogger
import play.api.http.HeaderNames
import play.api.mvc.{ Request, RequestHeader }

import scala.concurrent.{ ExecutionContext, Future }

case class DigestParameters(
  username: String,
  realm: String,
  uri: String,
  nonce: String,
  response: String,
  method: String
) {

}

class DigestAuthProvider(
  protected val authInfoRepository: AuthInfoRepository,
  protected val passwordHasherRegistry: PasswordHasherRegistry
)(implicit val executionContext: ExecutionContext)
  extends RequestProvider with PasswordProvider with Logger {

  private[this] val logger = getLogger

  override def id = "digest-auth"

  private val expectedHeaders: Set[String] = Set(
    "username", "realm", "uri", "nonce", "response"
  )

  def getDigestParameters(request: RequestHeader): Option[DigestParameters] = {
    if (!request.headers.toMap.contains(HeaderNames.AUTHORIZATION)) return None
    val authStringOpt = request.headers.get(HeaderNames.AUTHORIZATION)

    val authString: String = authStringOpt match {
      case Some(value) if value.startsWith("Digest ") => value
      case None => return None
      case _ => return None
    }

    val params: Map[String, String] = authString.replaceFirst("Digest ", "").split(",").map(
      (keyValuePair) => {
        val data = keyValuePair.trim.split("=", 2)
        val key = data(0)
        val value = data(1).replaceAll("\"", "")
        (key, value)
      }
    ).filter {
        case (key, value) =>
          StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(value)
      }.toMap

    if (expectedHeaders.subsetOf(params.keys.toSet)) {
      Some(DigestParameters(
        params("username"),
        params("realm"),
        params("uri"),
        params("nonce"),
        params("response"),
        request.method
      ))
    } else {
      val missingHeaders = expectedHeaders -- params.keys.toSet
      logger.warn(s"Digest auth request missing required headers: ${missingHeaders.toString()}")
      None
    }
  }

  def getAuthorizedUser(digestParameters: DigestParameters): Future[Option[LoginInfo]] = {
    authInfoRepository.find[TwilioUser](LoginInfo(id, digestParameters.username)).map {
      case Some(authInfo: TwilioUser) =>
        val digest: String = createDigest(digestParameters, authInfo.apiPassword)
        if (digest == digestParameters.response) {
          Some(LoginInfo(id, digestParameters.username))
        } else {
          logger.warn(s"Digest $digest did not match expected value ${digestParameters.response}")
          None
        }
      case None => None
    }
  }

  private def createDigest(digestParameters: DigestParameters, pass: String): String = {
    val username = digestParameters.username
    val realm = digestParameters.realm
    val digest1 = DigestUtils.md5Hex(username + ":" + realm + ":" + pass)
    val digest2 = DigestUtils.md5Hex(digestParameters.method + ":" + digestParameters.uri)
    DigestUtils.md5Hex(digest1 + ":" + digestParameters.nonce + ":" + digest2)
  }

  override def authenticate[B](request: Request[B]): Future[Option[LoginInfo]] = {
    getDigestParameters(request) match {
      case Some(params: DigestParameters) =>
        getAuthorizedUser(params)
      case None =>
        logger.warn("Failed to parse digest request.")
        Future.successful(None)
    }
  }
}
