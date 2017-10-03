package services.submission

import java.net.URL
import javax.inject.Inject

import com.twilio.Twilio
import com.twilio.rest.fax.v1.{ Fax, FaxCreator }
import models.{ Claim, ClaimSubmission, TwilioFax, TwilioUser }
import play.api.Configuration
import utils.secrets.SecretsManager

class TwilioFaxApi @Inject() (configuration: Configuration, secretsManager: SecretsManager) extends FaxApi {
  lazy val accountSid: String = secretsManager.getSecretUtf8(
    configuration.getString("twilio.accountSidSecretName").get
  )

  lazy val authTokenSecretName: String = secretsManager.getSecretUtf8(
    configuration.getString("twilio.authTokenSecretName").get
  )

  def sendFax(claim: Claim,
              claimSubmission: ClaimSubmission,
              twilioUser: TwilioUser): TwilioFax = {
    Twilio.init(accountSid, authTokenSecretName)
    val mediaUrl: URL = new URL("https://www.twilio.com/docs/documents/25/justthefaxmaam.pdf")
    val faxCreator: FaxCreator = Fax.creator(claimSubmission.to, mediaUrl.toURI)
    faxCreator.setFrom(claimSubmission.from)
    val fax = faxCreator.create()

    TwilioFax(
      claimID = claim.claimID,
      claimSubmissionID = claimSubmission.claimSubmissionID,
      dateCreated = fax.getDateCreated.toDate,
      dateUpdated = fax.getDateUpdated.toDate,
      to = fax.getTo,
      from = fax.getFrom,
      twilioFaxId = fax.getSid,
      status = fax.getStatus.toString
    )
  }
}
