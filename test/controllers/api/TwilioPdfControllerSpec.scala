package controllers.api

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import controllers.CSRFTest
import models.{ Claim, ClaimForm, TwilioUser }
import org.apache.commons.io.IOUtils
import org.mockito.{ Matchers, Mockito }
import play.api.libs.json.JsResult
import play.api.mvc.{ AnyContentAsEmpty, Result }
import play.api.test.{ FakeRequest, PlaySpecification, WithApplication }

import scala.concurrent.Future

class TwilioPdfControllerSpec extends PlaySpecification with CSRFTest {

  "the getPdf action should" should {
    "return 401 with www-authenticate set if no Digest Auth credentials are provided" in new TwilioPdfControllerTestContext {
      Mockito.when(mockBasicAuthProvider.authenticate(Matchers.any()))
        .thenReturn(Future.successful(None))

      Mockito.when(mockSecureRandomIdGenerator.generate)
        .thenReturn(Future.successful("nonce"))

      new WithApplication(application) {
        val getRequest: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(controllers.api.routes.TwilioPdfController.getPdf(UUID.randomUUID(), UUID.randomUUID()))
        val csrfReq: FakeRequest[AnyContentAsEmpty.type] = addToken(getRequest)
        val getResult: Future[Result] = route(app, csrfReq).get

        status(getResult) must be equalTo UNAUTHORIZED
        headers(getResult).get("WWW-Authenticate").get must be equalTo "Digest realm=twilio,nonce=nonce"
      }
    }

    "return 200 with pdf if digest credentials are authenticated" in new TwilioPdfControllerTestContext {
      val userUUID: UUID = UUID.randomUUID()
      val claimUUID: UUID = UUID.randomUUID()
      Mockito.when(mockBasicAuthProvider.authenticate(Matchers.any()))
        .thenReturn(Future.successful(Some(LoginInfo("basic-auth", "user"))))

      Mockito.when(mockFormDao.find(Matchers.eq(userUUID), Matchers.eq(claimUUID)))
        .thenReturn(Future.successful(Seq(testForm)))

      Mockito.when(mockDocumentService.render(Matchers.eq(testForm)))
        .thenReturn(Future.successful(Array.empty[Byte]))

      Mockito.when(mockTwilioUserDao.find(Matchers.any()))
        .thenReturn(Future.successful(Some(TwilioUser(userUUID, "password"))))

      Mockito.when(mockPdfConcatenator.concat(Matchers.any()))
        .thenReturn(Array.empty[Byte])

      new WithApplication(application) {
        val getRequest: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(controllers.api.routes.TwilioPdfController.getPdf(userUUID, claimUUID))
        val csrfReq: FakeRequest[AnyContentAsEmpty.type] = addToken(getRequest)
        val getResult: Future[Result] = route(app, csrfReq).get

        status(getResult) must be equalTo OK
        contentAsBytes(getResult).toArray must be equalTo Array.empty[Byte]
      }
    }
  }
}
