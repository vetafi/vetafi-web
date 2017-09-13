package utils.auth

import java.util

import com.mohiva.play.silhouette.api.crypto.Base64
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{Credentials, PasswordHasherRegistry}
import com.mohiva.play.silhouette.api.{Logger, LoginInfo, RequestProvider}
import com.mohiva.play.silhouette.impl.providers.PasswordProvider
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.lang3.StringUtils
import play.api.http.HeaderNames
import play.api.mvc.{Request, RequestHeader}
import play.mvc.Http
import com.mohiva.play.silhouette.api.util.Credentials

import scala.concurrent.{ExecutionContext, Future}

case class DigestParameters(username: String, realm: String, uri: String, nonce: String, response: String) {

}


object DigestAuthProvider {
  def isAuthorized(request: Http.Request): Boolean = {
    val req = new DigestAuthProvider(request)
    req.isValid && req.isAuthorized
  }

  val ID = "digest-auth"
}

class DigestAuthProvider(
                          protected val authInfoRepository: AuthInfoRepository,
                          protected val passwordHasherRegistry: PasswordHasherRegistry)(implicit val executionContext: ExecutionContext)
  extends RequestProvider with PasswordProvider with Logger {

  private val params = new util.HashMap[String, String]


  def getCredentials(request: RequestHeader): Option[DigestParameters] = {
    if (!request.headers.toMap.contains("authorization")) return None
    val authStringOpt = request.headers.get("authorization")

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

    if (params.contains("username") &&
      params.contains("realm") &&
      params.contains("uri") &&
      params.contains("nonce") &&
      params.contains("response")) {
      Some(DigestParameters(params("username"),
        params("realm"),
        params("uri"),
        params("nonce"),
        params("response")))
    } else {
      None
    }
  }


    def isAuthorized(digestParameters: DigestParameters): Boolean = {
      val user: Future[Option[Nothing]] = authInfoRepository.find(LoginInfo(id, digestParameters.username))
      if (user == null) throw new Nothing(params.get("realm"))
      val digest = createDigest(user.apiPassword)
      digest == params.get("response")
    }

    private def createDigest(pass: String) = {
      val username = params.get("username")
      val realm = params.get("realm")
      val digest1 = DigestUtils.md5Hex(username + ":" + realm + ":" + pass)
      val digest2 = DigestUtils.md5Hex(request.method + ":" + params.get("uri"))
      DigestUtils.md5Hex(digest1 + ":" + params.get("nonce") + ":" + digest2)
    }*/
  override def authenticate[B](request: Request[B]): Future[Option[LoginInfo]] = {

  }

  def getCredentials(request: RequestHeader): Option[Credentials] = {
    request.headers.get(HeaderNames.AUTHORIZATION) match {
      case Some(header) if header.startsWith("Basic ") =>
        Base64.decode(header.replace("Basic ", "")).split(":", 2) match {
          case credentials if credentials.length == 2 => Some(Credentials(credentials(0), credentials(1)))
          case _ => None
        }
      case _ => None
    }
  }

  private def createDigest(request: RequestHeader, pass: String) = {
    val username = params.get("username")
    val realm = params.get("realm")
    val digest1 = DigestUtils.md5Hex(username + ":" + realm + ":" + pass)
    val digest2 = DigestUtils.md5Hex(request.method + ":" + params.get("uri"))
    DigestUtils.md5Hex(digest1 + ":" + params.get("nonce") + ":" + digest2)
  }

  override def id = DigestAuthProvider.ID
}
