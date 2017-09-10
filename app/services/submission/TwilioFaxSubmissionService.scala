package services.submission

import java.time.Instant
import java.util.{ Date, UUID }

import models.{ Claim, ClaimSubmission }

import scala.concurrent.Future

/**
 * Submission service using twilio fax api.
 */
class TwilioFaxSubmissionService extends FaxSubmissionService {
  override def submit(claim: Claim): Future[ClaimSubmission] = {
    // TODO implement
    Future.successful(ClaimSubmission(
      UUID.randomUUID().toString,
      success = true,
      Option("Success."),
      Date.from(Instant.now())
    ))
  }
}
