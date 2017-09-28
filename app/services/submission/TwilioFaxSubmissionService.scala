package services.submission

import java.net.URL
import java.time.Instant
import java.util.{ Date, UUID }
import javax.inject.Inject

import models.{ Claim, ClaimSubmission }
import play.api.Configuration
import utils.secrets.SecretsManager
import com.twilio.Twilio
import com.twilio.rest.fax.v1.Fax
import com.twilio.rest.fax.v1.FaxCreator

import scala.concurrent.Future

/**
 * Submission service using twilio fax api.
 */
class TwilioFaxSubmissionService @Inject() (
  configuration: Configuration,
  secretsManager: SecretsManager
) extends FaxSubmissionService {

  lazy val accountSid: String = secretsManager.getSecretUtf8(
    configuration.getString("twilio.accountSidSecretName").get
  )

  lazy val authTokenSecretName: String = secretsManager.getSecretUtf8(
    configuration.getString("twilio.authTokenSecretName").get
  )

  override def submit(claim: Claim): Future[ClaimSubmission] = {

    Twilio.init(accountSid, authTokenSecretName)

    val from = "+15017250604"
    val to = "+15558675309"
    val mediaUrl: URL = new URL("https://www.twilio.com/docs/documents/25/justthefaxmaam.pdf")
    val faxCreator: FaxCreator = Fax.creator(to, mediaUrl.toURI)
    faxCreator.setFrom(from)
    val fax: Fax = faxCreator.create()

    // TODO implement
    Future.successful(ClaimSubmission(
      UUID.randomUUID().toString,
      success = true,
      Option("Success."),
      Date.from(Instant.now())
    ))
  }
}
