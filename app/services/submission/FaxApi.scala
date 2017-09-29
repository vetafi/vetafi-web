package services.submission

import models.{Claim, ClaimSubmission, TwilioFax, TwilioUser}

trait FaxApi {

  def sendFax(claim: Claim, claimSubmission: ClaimSubmission, twilioUser: TwilioUser): TwilioFax
}
