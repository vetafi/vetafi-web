package services.submission

import models.{Claim, Recipient}

/**
 * Service used to discover default recipients for a given Claim.
 */
trait RecipientService {

  def getDefaultRecipients(claim: Claim): Set[Recipient]

}
