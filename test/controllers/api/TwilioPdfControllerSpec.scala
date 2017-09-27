package controllers.api

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import controllers.CSRFTest
import models.{Claim, ClaimForm}
import org.mockito.{Matchers, Mockito}
import play.api.libs.json.JsResult
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.{FakeRequest, PlaySpecification, WithApplication}

import scala.concurrent.Future

class TwilioPdfControllerSpec extends PlaySpecification with CSRFTest {
  sequential

  "the getPdf action should" should {
    "return 401 with www-authenticate set if no Digest Auth credentials are provided" in new TwilioPdfControllerTestContext {
      Mockito.when(mockDigestAuthProvider.authenticate(Matchers.any()))
        .thenReturn(Future.successful(None))

      new WithApplication(application) {
        val getRequest: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(controllers.api.routes.TwilioPdfController.getPdf(UUID.randomUUID(), UUID.randomUUID()))
        val csrfReq: FakeRequest[AnyContentAsEmpty.type] = addToken(getRequest)
        val getResult: Future[Result] = route(app, csrfReq).get

        status(getResult) must be equalTo UNAUTHORIZED
        headers(getResult).get("WWW-Authenticate").get must be equalTo "Digest realm=twilio"
      }
    }

    "return 200 with pdf if digest credentials are authenticated" in new TwilioPdfControllerTestContext {

      val userUUID = UUID.randomUUID()
      val claimUUID = UUID.randomUUID()
      Mockito.when(mockDigestAuthProvider.authenticate(Matchers.any()))
        .thenReturn(Future.successful(Some(LoginInfo("digest-auth", "user"))))

      Mockito.when(mockFormDao.find(Matchers.eq(userUUID), Matchers.eq(claimUUID)))
        .thenReturn(Future.successful(Seq(testForm)))

      Mockito.when(mockDocumentService.render(Matchers.eq(testForm)))
        .thenReturn(Future.successful(Array.empty[Byte]))

      new WithApplication(application) {
        val getRequest: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(controllers.api.routes.TwilioPdfController.getPdf(UUID.randomUUID(), UUID.randomUUID()))
        val csrfReq: FakeRequest[AnyContentAsEmpty.type] = addToken(getRequest)
        val getResult: Future[Result] = route(app, csrfReq).get

        status(getResult) must be equalTo UNAUTHORIZED
        headers(getResult).get("WWW-Authenticate").get must be equalTo "Digest realm=twilio"
      }
    }


  }
}
