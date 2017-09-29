package utils.auth

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry
import models.TwilioUser
import org.mockito.Mockito
import play.api.http.HeaderNames
import play.api.test.{FakeRequest, PlaySpecification}

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

class DigestAuthProviderSpec  extends PlaySpecification {
  "the digest auth provider" should {

    "not authenticate if authorization header is missing" in {
      val mockAuthInfoRepository = Mockito.mock(classOf[AuthInfoRepository])
      val mockPasswordHasherRegistry = Mockito.mock(classOf[PasswordHasherRegistry])
      val digestAuthProvider = new DigestAuthProvider(mockAuthInfoRepository, mockPasswordHasherRegistry)
      val request = FakeRequest(play.api.http.HttpVerbs.GET, "/test")
      val authenticated: Future[Option[LoginInfo]] = digestAuthProvider.authenticate(request)

      Await.result(authenticated, Duration.Inf) must be equalTo None
    }

    "not authenticate if header fields are missing" in {
      val mockAuthInfoRepository = Mockito.mock(classOf[AuthInfoRepository])
      val mockPasswordHasherRegistry = Mockito.mock(classOf[PasswordHasherRegistry])
      val digestAuthProvider = new DigestAuthProvider(mockAuthInfoRepository, mockPasswordHasherRegistry)

      val request = FakeRequest(play.api.http.HttpVerbs.GET, "/test").withHeaders(
        HeaderNames.AUTHORIZATION -> s"Digest realm=realm")

      val authenticated: Future[Option[LoginInfo]] = digestAuthProvider.authenticate(request)

      Await.result(authenticated, Duration.Inf) must be equalTo None
    }

    "not authenticate if response is incorrect" in {
      val mockAuthInfoRepository = Mockito.mock(classOf[AuthInfoRepository])

      val userID = UUID.randomUUID()
      val loginInfo = LoginInfo("digest-auth", "username")

      Mockito.when(mockAuthInfoRepository.find[TwilioUser](loginInfo))
        .thenReturn(Future.successful(Some(TwilioUser(userID, "password"))))

      val mockPasswordHasherRegistry = Mockito.mock(classOf[PasswordHasherRegistry])
      val digestAuthProvider = new DigestAuthProvider(mockAuthInfoRepository, mockPasswordHasherRegistry)

      val response = DigestAuthProvider.createDigest(DigestParameters(
        username="username",
        realm="realm",
        uri="/test",
        nonce = "nonce",
        response="none",
        method="GET"), "different_password")

      val request = FakeRequest(play.api.http.HttpVerbs.GET, "/test").withHeaders(
        HeaderNames.AUTHORIZATION -> s"Digest realm=realm,uri=/test,username=username,auth=qop,nonce=nonce,response=$response")

      val authenticated: Future[Option[LoginInfo]] = digestAuthProvider.authenticate(request)

      Await.result(authenticated, Duration.Inf) must be equalTo None
    }

    "authenticate correct requests" in {
      val mockAuthInfoRepository = Mockito.mock(classOf[AuthInfoRepository])

      val userID = UUID.randomUUID()
      val loginInfo = LoginInfo("digest-auth", "username")

      Mockito.when(mockAuthInfoRepository.find[TwilioUser](loginInfo))
        .thenReturn(Future.successful(Some(TwilioUser(userID, "password"))))

      val mockPasswordHasherRegistry = Mockito.mock(classOf[PasswordHasherRegistry])
      val digestAuthProvider = new DigestAuthProvider(mockAuthInfoRepository, mockPasswordHasherRegistry)

      val response = DigestAuthProvider.createDigest(DigestParameters(
        username="username",
        realm="realm",
        uri="/test",
        nonce = "nonce",
        response="none",
        method="GET"), "password")

      val request = FakeRequest(play.api.http.HttpVerbs.GET, "/test").withHeaders(
        HeaderNames.AUTHORIZATION -> s"Digest realm=realm,uri=/test,username=username,auth=qop,nonce=nonce,response=$response")

      val authenticated: Future[Option[LoginInfo]] = digestAuthProvider.authenticate(request)

      Await.result(authenticated, Duration.Inf) must be equalTo Some(loginInfo)
    }
  }
}
