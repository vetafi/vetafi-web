package services.submission

import models.{ Claim, ClaimSubmission }

import scala.concurrent.Future

/**
 * Service for the physical submission of a claim object.
 */
trait FaxSubmissionService {

  def submit(claim: Claim): Future[ClaimSubmission]
}
