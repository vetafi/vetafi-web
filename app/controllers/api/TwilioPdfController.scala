package controllers.api

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import models.daos.{FormDAO, UserValuesDAO}
import org.log4s.getLogger
import play.api.mvc.{Action, AnyContent, Controller, Cookie}
import services.documents.DocumentService
import services.forms.{ClaimService, ContactInfoService}
import utils.auth.{DefaultEnv, DigestAuthErrorHandler, TwilioAuthEnv}

import scala.concurrent.Future

class TwilioPdfController @Inject() (
                                      val formDAO: FormDAO,
                                      val userValuesDAO: UserValuesDAO,
                                      val claimService: ClaimService,
                                      val contactInfoService: ContactInfoService,
                                      val documentService: DocumentService,
                                      silhouette: Silhouette[TwilioAuthEnv],
                                      val digestAuthErrorHandler: DigestAuthErrorHandler
                                    ) extends Controller {

  private[this] val logger = getLogger

  def getPdf: Action[AnyContent] = silhouette.SecuredAction(digestAuthErrorHandler).async {
    implicit request =>
      formDAO.find(request.identity.userID, claimID, formKey).flatMap {
        case Some(claimForm) =>
          documentService.render(claimForm).map {
            content =>
              logger.info(s"PDF rendered for user ${request.identity.userID}")
              Ok(content).as("application/pdf").withCookies(
                Cookie("fileDownloadToken", "1", secure = false, httpOnly = false)
              )
          }
        case None =>
          Future.successful(NotFound)
      }
  }
}
