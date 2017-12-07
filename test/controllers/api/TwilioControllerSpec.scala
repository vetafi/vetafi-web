package controllers.api

import java.time.Instant
import java.util.{Date, UUID}

import com.mohiva.play.silhouette.api.LoginInfo
import controllers.CSRFTest
import models.{Claim, ClaimForm, TwilioFax, TwilioUser}
import org.apache.commons.io.IOUtils
import org.mockito.{Matchers, Mockito}
import play.api.libs.json.JsResult
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, Result}
import play.api.test.{FakeRequest, PlaySpecification, WithApplication}
import reactivemongo.api.commands.UpdateWriteResult

import scala.concurrent.Future

class TwilioControllerSpec extends PlaySpecification with CSRFTest {

  "the twilio callback" should {
    "return 200 if the request can be validated" in new TwilioControllerTestContext {
      val twilioFaxId = "FXID"
      Mockito.when(mockClaimDao.findClaim(Matchers.eq(testClaim.userID), Matchers.eq(testClaim.claimID)))
        .thenReturn(Future.successful(Some(testClaim.copy())))

      Mockito.when(mockClaimDao.save(Matchers.eq(testClaim.userID), Matchers.eq(testClaim.claimID), Matchers.any()))
        .thenReturn(Future.successful(UpdateWriteResult(ok = true, 1, 1, Seq(), Seq(), None, None, None)))

      Mockito.when(mockTwilioFaxDao.find(Matchers.eq(twilioFaxId)))
        .thenReturn(Future.successful(Some(TwilioFax(
          userID = testClaim.userID,
          claimID = testClaim.claimID,
          claimSubmissionID = UUID.randomUUID(),
          dateCreated = Date.from(Instant.now()),
          dateUpdated = Date.from(Instant.now()),
          to = "to",
          from = "from",
          twilioFaxId = twilioFaxId,
          status = "status"
        ))))

      Mockito.when(mockTwilioFaxDao.save(Matchers.eq(twilioFaxId), Matchers.any()))
        .thenReturn(Future.successful(UpdateWriteResult(ok = true, 1, 1, Seq(), Seq(), None, None, None)))

      new WithApplication(application) {
        val request: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest(controllers.api.routes.TwilioController.faxCallback())
          .withHeaders(
            "X-Twilio-Signature" -> "jm9SluOI0TvBJ6CJqLEBqSSfpgs=",
            "X-Forwarded-Proto" -> "http",
            "Host" -> "www.vetafi.org"
          )
          .withFormUrlEncodedBody(
            "FaxSid" -> twilioFaxId,
            "FaxStatus" -> "delivered"
          )
        val csrfReq: FakeRequest[AnyContentAsFormUrlEncoded] = addToken(request)
        val getResult: Future[Result] = route(app, csrfReq).get
        status(getResult) must be equalTo OK
      }
    }
  }

  "the getPdf action" should {
    "return 401 with www-authenticate set if no Digest Auth credentials are provided" in new TwilioControllerTestContext {
      Mockito.when(mockBasicAuthProvider.authenticate(Matchers.any()))
        .thenReturn(Future.successful(None))

      Mockito.when(mockSecureRandomIdGenerator.generate)
        .thenReturn(Future.successful("nonce"))

      new WithApplication(application) {
        val getRequest: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(controllers.api.routes.TwilioController.getPdf(UUID.randomUUID(), UUID.randomUUID()))
        val csrfReq: FakeRequest[AnyContentAsEmpty.type] = addToken(getRequest)
        val getResult: Future[Result] = route(app, csrfReq).get

        status(getResult) must be equalTo UNAUTHORIZED
        headers(getResult).get("WWW-Authenticate").get must be equalTo """Basic realm="twilio""""
      }
    }

    "return 200 with pdf if digest credentials are authenticated" in new TwilioControllerTestContext {
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
          FakeRequest(controllers.api.routes.TwilioController.getPdf(userUUID, claimUUID))
        val csrfReq: FakeRequest[AnyContentAsEmpty.type] = addToken(getRequest)
        val getResult: Future[Result] = route(app, csrfReq).get

        status(getResult) must be equalTo OK
        contentAsBytes(getResult).toArray must be equalTo Array.empty[Byte]
      }
    }
  }
}
