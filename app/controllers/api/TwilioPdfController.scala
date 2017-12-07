package controllers.api

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import models.daos.{ FormDAO, UserValuesDAO }
import org.log4s.{ MDC, getLogger }
import play.api.mvc.{ Action, AnyContent, Controller }
import services.documents.DocumentService
import services.documents.pdf.PDFConcatenator
import services.forms.{ ClaimService, ContactInfoService }
import utils.auth.{ BasicAuthErrorHandler, DigestAuthErrorHandler, TwilioAuthEnv, TwilioRequestValidator }

import scala.collection.parallel.ParSeq
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class TwilioPdfController @Inject() (
  val formDAO: FormDAO,
  val userValuesDAO: UserValuesDAO,
  val claimService: ClaimService,
  val contactInfoService: ContactInfoService,
  val documentService: DocumentService,
  silhouette: Silhouette[TwilioAuthEnv],
  val basicAuthErrorHandler: BasicAuthErrorHandler,
  val pdfConcatenator: PDFConcatenator,
  val twilioRequestValidator: TwilioRequestValidator
) extends Controller {

  private[this] val logger = getLogger

  def callback(): Action[AnyContent] = Action.async {
    request =>
      if (twilioRequestValidator.authenticate(request)) {
        logger.info("Got a callback: " + request.body)
        Future.successful(Ok)
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
