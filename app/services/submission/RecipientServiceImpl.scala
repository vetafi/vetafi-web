package services.submission
import javax.inject.Inject

import models.{ Claim, Recipient }
import play.api.Configuration

class RecipientServiceImpl @Inject() (configuration: Configuration) extends RecipientService {

  override def getDefaultRecipients(claim: Claim): Set[Recipient] = {
    Set(Recipient(Recipient.Type.FAX, configuration.get[String]("submission.va.fax")))
  }
}
