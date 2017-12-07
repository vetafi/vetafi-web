package controllers.api

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import models.daos.{ClaimDAO, FormDAO, TwilioFaxDAO, UserValuesDAO}
import models.{Claim, TwilioFax}
import org.log4s.{MDC, getLogger}
import play.api.mvc.{Action, AnyContent, Controller, Request}
import services.documents.DocumentService
import services.documents.pdf.PDFConcatenator
import services.forms.{ClaimService, ContactInfoService}
import utils.auth.{BasicAuthErrorHandler, TwilioAuthEnv, TwilioRequestValidator}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TwilioController @Inject()(
                                  val formDAO: FormDAO,
                                  val userValuesDAO: UserValuesDAO,
                                  val twilioFaxDAO: TwilioFaxDAO,
                                  val claimDAO: ClaimDAO,
                                  val claimService: ClaimService,
                                  val contactInfoService: ContactInfoService,
                                  val documentService: DocumentService,
                                  silhouette: Silhouette[TwilioAuthEnv],
                                  val basicAuthErrorHandler: BasicAuthErrorHandler,
                                  val pdfConcatenator: PDFConcatenator,
                                  val twilioRequestValidator: TwilioRequestValidator
                                ) extends Controller {

  private[this] val logger = getLogger


  def updateFaxModel(faxStatus: String, faxId: String): Future[TwilioFax] = {
    twilioFaxDAO.find(faxId).flatMap {
      case Some(twilioFax) =>
        twilioFaxDAO.save(faxId, twilioFax.copy(status = faxStatus)).map {
          case ok if ok.ok => twilioFax
          case _ => throw new RuntimeException("Failed to update fax model.")
        }
      case None =>
        throw new IllegalArgumentException(s"Fax id $faxId not found")
    }
  }

  def updateClaimModel(userID: UUID, claimID: UUID, faxStatus: String): Future[Claim] = {
    claimDAO.findClaim(userID, claimID).flatMap {
      case Some(claim) =>
        val claimState = if (faxStatus == "delivered") Claim.State.SUBMITTED else claim.state
        val newClaim = claim.copy(state = claimState)
        claimDAO.save(userID, claimID, claim.copy(state = claimState)).map {
          case ok if ok.ok => newClaim
          case _ => throw new RuntimeException("Failed to update claim model.")
        }
    }
  }

  def updateClaimAndFaxModelsFromParams(params: Map[String, String]): Future[(TwilioFax, Claim)] = {
    params.get("FaxSid") match {
      case Some(faxId) =>
        params.get("FaxStatus") match {
          case Some(faxStatus) =>
            updateFaxModel(faxStatus, faxId).flatMap {
              (twilioFax: TwilioFax) =>
                updateClaimModel(twilioFax.userID, twilioFax.claimID, faxStatus).map {
                  (claim: Claim) =>
                    (twilioFax, claim)
                }
            }
          case None => throw new IllegalArgumentException("Request must have FaxStatus")
        }
      case None => throw new IllegalArgumentException("Request must have FaxSid")
    }

  }

  def updateClaimAndFaxModelsFromRequest(request: Request[AnyContent]): Future[(TwilioFax, Claim)] = {
    request.body.asFormUrlEncoded match {
      case None => throw new IllegalArgumentException("Request must have url encoded form data.")
      case Some(params: Map[String, Seq[String]]) =>
        val cleanedParams: Map[String, String] = params.toSeq.map {
          case (k, v) =>
            (k,
              v.reduceOption(_ + _) match {
                case None => ""
                case Some(value) => value
              })
        }.toMap
        updateClaimAndFaxModelsFromParams(cleanedParams)
    }
  }

  def faxCallback(): Action[AnyContent] = Action.async {
    (request: Request[AnyContent]) =>
      if (twilioRequestValidator.authenticate(request)) {
        logger.info("Got a callback: " + request.body)
        updateClaimAndFaxModelsFromRequest(request).map {
          _ => Ok
        }
      } else {
        logger.info("Got a bad callback: " + request.body + request.headers.toSimpleMap)
        Future.successful(Unauthorized)
      }
  }

  def getPdf(userID: UUID, claimID: UUID): Action[AnyContent] = silhouette.SecuredAction(basicAuthErrorHandler).async {
    implicit request =>
      MDC.withCtx(
        "userID" -> userID.toString,
        "claimID" -> claimID.toString
      ) {
        logger.info("Received request from twilio for claim PDF.")

        val formsFuture = formDAO.find(userID, claimID)

        formsFuture.flatMap(
          forms => {
            logger.info(s"Will concatenate ${forms.length} forms.")
            Future.sequence(forms.par.map(documentService.render).seq)
          }
        ).map(
          (pdfs: Seq[Array[Byte]]) => {
            logger.info(s"Concatenating ${pdfs.length} of sizes " + pdfs.map(_.length).toString())
            pdfConcatenator.concat(pdfs)
          }
        ).map {
          (concatedPdf: Array[Byte]) =>
            logger.info("Serving concatenated pdf.")
            Ok(concatedPdf).as("application/pdf")
        }
      }
  }

}
