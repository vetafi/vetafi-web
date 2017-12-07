package controllers.api

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import controllers.CSRFTest
import models.{Claim, ClaimForm, TwilioUser}
import org.apache.commons.io.IOUtils
import org.mockito.{Matchers, Mockito}
import play.api.libs.json.JsResult
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, Result}
import play.api.test.{FakeRequest, PlaySpecification, WithApplication}

import scala.concurrent.Future

class TwilioPdfControllerSpec extends PlaySpecification with CSRFTest {

  "the twilio callback" should {
    "return 200 if the request can be validated" in new TwilioPdfControllerTestContext {
      new WithApplication(application) {
        val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(controllers.api.routes.TwilioPdfController.callback())
          .withHeaders("X-Twilio-Signature" -> "yFkFXpUSG5aHdzUxL3PIliSbI1M=")
          .withFormUrlEncodedBody(
            "CallSid" -> "CA1234567890ABCDE",
            "Caller" -> "+14158675309",
            "Digits" -> "1234",
            "From" -> "+14158675309",
            "To" -> "+18005551212")
        val csrfReq: FakeRequest[AnyContentAsFormUrlEncoded] = addToken(request)
        val getResult: Future[Result] = route(app, csrfReq).get
        status(getResult) must be equalTo OK
      }
    }
  }

  "the getPdf action" should {
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
        headers(getResult).get("WWW-Authenticate").get must be equalTo """Basic realm="twilio""""
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
