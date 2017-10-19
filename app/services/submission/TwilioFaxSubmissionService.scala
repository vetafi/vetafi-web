package services.submission

import java.net.URL
import java.util.{ Date, UUID }
import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.util.SecureRandomIDGenerator
import models.daos.{ ClaimDAO, TwilioFaxDAO }
import models.{ Claim, ClaimSubmission, TwilioFax, TwilioUser }
import play.api.Configuration
import reactivemongo.api.commands.WriteResult
import services.TwilioUserService
import services.time.ClockService
import utils.auth.DigestAuthProvider
import utils.secrets.SecretsManager

import scala.concurrent.ExecutionContext.Implicits.global
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
  claimDAO: ClaimDAO,
  faxApi: FaxApi,
  clockService: ClockService
) extends FaxSubmissionService {

  val fromNumber: String = configuration.getString("twilio.number").get
  val toNumber: String = configuration.getString("submission.va.fax").get

  def createNewTwilioUser(claim: Claim): Future[TwilioUser] = {
    secureRandomIDGenerator.generate.flatMap {
      password =>
        twilioUserService.save(
          LoginInfo(DigestAuthProvider.ID, claim.claimID.toString),
          TwilioUser(claim.claimID, password)
        )
    }
  }

  def updateClaim(claim: Claim, claimSubmission: ClaimSubmission): Future[WriteResult] = {
    claimDAO.save(claim.userID, claim.claimID, claim.copy(submissions = Seq(claimSubmission)))
  }

  def saveTwilioFax(twilioFax: TwilioFax): Future[WriteResult] = {
    twilioFaxDAO.save(
      twilioFax
    )
  }

  def saveResults(claimSubmission: ClaimSubmission, claim: Claim, fax: TwilioFax): Future[ClaimSubmission] = {
    saveTwilioFax(fax).flatMap {
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

  def getResource(claim: Claim, twilioUser: TwilioUser): URL = {
    val hostname: String = configuration.getString("hostname").get
    new URL(s"https://${twilioUser.userID}:${twilioUser.apiPassword}@$hostname/api/twilioPdfEndpoint/${claim.userID}/${claim.claimID}")
  }

  override def submit(claim: Claim): Future[ClaimSubmission] = {
    createNewTwilioUser(claim).flatMap {
      twilioUser =>
        val claimSubmission: ClaimSubmission = ClaimSubmission(
          UUID.randomUUID(),
          toNumber,
          fromNumber,
          getClass.getSimpleName,
          Date.from(clockService.getCurrentTime),
          success = true
        )
        val twilioFax = faxApi.sendFax(
          claim,
          claimSubmission,
          twilioUser,
          getResource(claim, twilioUser)
        )
        saveResults(claimSubmission, claim, twilioFax).map {
          claimSubmission => claimSubmission
        }
    }
  }
}
