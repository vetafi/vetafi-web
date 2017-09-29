package services.submission

import java.time.Instant
import java.util.{Date, UUID}

import models.{ClaimSubmission, TwilioFax, TwilioUser}
import org.mockito.{Matchers, Mockito}
import play.api.test.{PlaySpecification, WithApplication}
import reactivemongo.api.commands.UpdateWriteResult

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class TwilioFaxSubmissionServiceSpec extends PlaySpecification {

  "Twilio fax submission service" should {
    "do something" in new TwilioFaxSubmissionServiceTestContext {
      val twilioUser = TwilioUser(UUID.randomUUID(), "password")
      val claimSubmission = ClaimSubmission(
        claimSubmissionID = UUID.randomUUID(),
        to = "xxx",
        from = "xxx",
        method = "xxx",
        dateSubmitted = Date.from(Instant.now()),
        success = true
      )

      val fakeTwilioFax = TwilioFax(
        claimID = UUID.randomUUID(),
        claimSubmissionID = UUID.randomUUID(),
        dateCreated = Date.from(Instant.now()),
        dateUpdated = Date.from(Instant.now()),
        to = "toNumber",
        from = "fromNumber",
        twilioFaxId = "twilioFaxId",
        status = "status"
      )

      Mockito.when(mockSecretsManager.getSecretUtf8(Matchers.any()))
        .thenReturn("SECRET")

      Mockito.when(mockTwilioUserService.save(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(twilioUser))

      Mockito.when(mockTwilioFaxDAO.save(Matchers.any()))
        .thenReturn(Future.successful(UpdateWriteResult(ok = true, 1, 1, Seq(), Seq(), None, None, None)))

      Mockito.when(mockSecureRandomIDGenerator.generate)
        .thenReturn(Future.successful("password"))

      Mockito.when(mockClaimDAO.save(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(UpdateWriteResult(ok = true, 1, 1, Seq(), Seq(), None, None, None)))

      Mockito.when(mockFaxApi.sendFax(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(fakeTwilioFax)

      new WithApplication(application) {
        val twilioFaxSubmissionService: TwilioFaxSubmissionService =
          app.injector.instanceOf(classOf[TwilioFaxSubmissionService])

        val result: Future[ClaimSubmission] = twilioFaxSubmissionService.submit(testClaim)
        Await.result(result, Duration.Inf) must be equalTo claimSubmission
      }
    }
  }
}
