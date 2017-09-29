package services.submission

import java.net.URL
import java.time.Instant
import java.util.{Date, UUID}
import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.util.SecureRandomIDGenerator
import models.{Claim, ClaimSubmission, TwilioFax, TwilioUser}
import play.api.Configuration
import utils.secrets.SecretsManager
import com.twilio.Twilio
import com.twilio.rest.fax.v1.Fax
import com.twilio.rest.fax.v1.FaxCreator
import models.daos.{ClaimDAO, TwilioFaxDAO}
import reactivemongo.api.commands.WriteResult
import services.TwilioUserService
import utils.auth.DigestAuthProvider

import scala.concurrent.Future

/**
 * Submission service using twilio fax api.
 */
class TwilioFaxSubmissionService @Inject() (
  configuration: Configuration,
  secretsManager: SecretsManager,
  twilioUserService: TwilioUserService,
  twilioFaxDAO: TwilioFaxDAO,
  secureRandomIDGenerator: SecureRandomIDGenerator,
  claimDAO: ClaimDAO
) extends FaxSubmissionService {

  lazy val accountSid: String = secretsManager.getSecretUtf8(
    configuration.getString("twilio.accountSidSecretName").get
  )

  lazy val authTokenSecretName: String = secretsManager.getSecretUtf8(
    configuration.getString("twilio.authTokenSecretName").get
  )

  val fromNumber: String = configuration.getString("twilio.number").get
  val toNumber: String = configuration.getString("submission.va.fax").get

  def createNewTwilioUser(claim: Claim): Future[Option[TwilioUser]] = {
    secureRandomIDGenerator.generate.flatMap {
      password =>
        twilioUserService.save(
          LoginInfo(DigestAuthProvider.ID, claim.claimID.toString),
          TwilioUser(claim.claimID, password))
    }
  }

  def updateClaim(claim: Claim, claimSubmission: ClaimSubmission): Future[WriteResult] = {
    claimDAO.save(claim.userID, claim.claimID, claim.copy(submissions = Seq(claimSubmission)))
  }

  def saveTwilioFax(claimSubmission: ClaimSubmission, claim: Claim, fax: Fax): Future[WriteResult] = {
    twilioFaxDAO.save(
      TwilioFax(
        claimID = claim.claimID,
        claimSubmissionID = claimSubmission.claimSubmissionID,
        dateCreated = fax.getDateCreated.toDate,
        dateUpdated = fax.getDateUpdated.toDate,
        to = fax.getTo,
        from = fax.getFrom,
        twilioFaxId = fax.getSid,
        status = fax.getStatus.toString))
  }

  def sendFax(claim: Claim, twilioUser: TwilioUser): Fax = {
    Twilio.init(accountSid, authTokenSecretName)
    val mediaUrl: URL = new URL("https://www.twilio.com/docs/documents/25/justthefaxmaam.pdf")
    val faxCreator: FaxCreator = Fax.creator(toNumber, mediaUrl.toURI)
    faxCreator.setFrom(fromNumber)
    faxCreator.create()
  }

  def saveResults(claimSubmission: ClaimSubmission, claim: Claim, fax: Fax): Future[ClaimSubmission] = {
    saveTwilioFax(claimSubmission, claim, fax).flatMap {
      case faxWrite if faxWrite.ok =>
        updateClaim(claim, claimSubmission).map {
          case claimWrite if claimWrite.ok =>
            claimSubmission
          case _ =>
            throw new Exception("Could not update Claim.")
        }
      case _ =>
        throw new Exception("Could not save fax record.")
    }
  }

  override def submit(claim: Claim): Future[ClaimSubmission] = {
    createNewTwilioUser(claim).flatMap {
      case Some(twilioUser) =>
        val fax: Fax = sendFax(claim, twilioUser)
        val claimSubmission: ClaimSubmission = ClaimSubmission(UUID.randomUUID(),
          fax.getTo,
          getClass.getSimpleName,
          Date.from(Instant.now()))
        saveResults(claimSubmission, claim, fax).map {
          claimSubmission =>
            claimSubmission
        }
      case None =>
        throw new RuntimeException("Could not create twilioUser.")
    }
  }
}
