package services.submission

import java.net.URL

import models.{Claim, ClaimSubmission, TwilioFax, TwilioUser}

trait FaxApi {

  def sendFax(claim: Claim,
              claimSubmission: ClaimSubmission,
              twilioUser: TwilioUser,
              faxResource: URL): TwilioFax
}
