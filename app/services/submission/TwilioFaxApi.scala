package services.submission

import java.net.URL
import javax.inject.Inject

import com.twilio.Twilio
import com.twilio.rest.fax.v1.{ Fax, FaxCreator }
import controllers.api.routes
import models.{ Claim, ClaimSubmission, TwilioFax, TwilioUser }
import play.api.Configuration
import utils.secrets.SecretsManager
import org.log4s._

class TwilioFaxApi @Inject() (
  configuration: Configuration,
  secretsManager: SecretsManager
) extends FaxApi {

  private[this] val logger = getLogger

  lazy val accountSid: String = secretsManager.getSecretUtf8(
    configuration.getString("twilio.accountSidSecretName").get
  )

  lazy val authToken: String = secretsManager.getSecretUtf8(
    configuration.getString("twilio.authTokenSecretName").get
  )

  def sendFax(
    claim: Claim,
    claimSubmission: ClaimSubmission,
    twilioUser: TwilioUser,
    faxResource: URL
  ): TwilioFax = {
    Twilio.init(accountSid, authToken)
    MDC.withCtx("userID" -> claim.userID.toString, "claimID" -> claim.claimID.toString) {
      logger.info(s"Sending ${faxResource.toURI} to ${claimSubmission.to}")
    }
    val faxCreator: FaxCreator = Fax.creator(claimSubmission.to, faxResource.toURI)
      .setFrom(claimSubmission.from)
      .setStatusCallback(configuration.getString("scheme").get +
        configuration.getString("hostname").get +
        routes.TwilioController.faxCallback())
    val fax = faxCreator.create()

    TwilioFax(
      userID = claim.userID,
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
