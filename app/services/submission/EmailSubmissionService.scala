package services.submission

import models.{ Claim, ClaimSubmission }

import scala.concurrent.Future

trait EmailSubmissionService {

  def submit(claim: Claim): Future[ClaimSubmission]
}
