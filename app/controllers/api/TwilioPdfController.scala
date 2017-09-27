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
import utils.auth.{ DigestAuthErrorHandler, TwilioAuthEnv }

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
  val digestAuthErrorHandler: DigestAuthErrorHandler,
  val pdfConcatenator: PDFConcatenator
) extends Controller {

  private[this] val logger = getLogger

  def getPdf(userID: UUID, claimID: UUID): Action[AnyContent] = silhouette.SecuredAction(digestAuthErrorHandler).async {
    implicit request =>
      MDC.withCtx(
        "userID" -> userID.toString,
        "claimID" -> claimID.toString
      ) {
          logger.info("Received request from twilio for claim PDF.")

          val formsFuture = formDAO.find(request.identity.userID, claimID)

          formsFuture.flatMap(
            forms => {
              logger.info(s"Will concatenate ${forms.length} forms.")
              Future.sequence(forms.par.map(documentService.render).seq)
            }
          ).map(
              (pdfs: Seq[Array[Byte]]) => {
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
