package controllers.api

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import models.daos.{ FormDAO, UserValuesDAO }
import models.{ ClaimForm, User }
import play.api.Logger
import play.api.libs.json.{ JsBoolean, JsError, JsValue, Json }
import play.api.mvc._
import services.documents.DocumentService
import services.forms.{ ClaimService, ContactInfoService }
import utils.auth.DefaultEnv
import org.log4s._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Controller for CRUD operations on ClaimForms
 */
class FormController @Inject() (
  val formDAO: FormDAO,
  val userValuesDAO: UserValuesDAO,
  val claimService: ClaimService,
  val contactInfoService: ContactInfoService,
  val documentService: DocumentService,
  components: ControllerComponents,
  silhouette: Silhouette[DefaultEnv]) extends AbstractController(components) {

  private[this] val logger = getLogger

  def getFormsForClaim(claimID: UUID): Action[AnyContent] = silhouette.SecuredAction.async {
    request =>
      {
        formDAO.find(request.identity.userID, claimID).map {
          case forms if forms.nonEmpty => Ok(Json.toJson(forms))
          case _ => NotFound
        }
      }
  }

  def getForm(claimID: UUID, formKey: String): Action[AnyContent] = silhouette.SecuredAction.async {
    request =>
      {
        formDAO.find(request.identity.userID, claimID, formKey).map {
          case Some(form) => Ok(Json.toJson(form))
          case _ => NotFound
        }
      }
  }

  def saveForm(claimID: UUID, formKey: String) =
    silhouette.SecuredAction.async(parse.json) {
      (request: SecuredRequest[DefaultEnv, JsValue]) =>
        {
          val dataResult = request.body.validate[Map[String, JsValue]]

          dataResult.fold(
            errors => {
              logger.warn(s"saveForm validation errors: $errors")
              Future.successful(
                BadRequest(Json.obj("status" -> "error", "message" -> JsError.toJson(errors))))
            },
            (data: Map[String, JsValue]) => {
              formDAO.find(request.identity.userID, claimID, formKey).flatMap {
                case Some(claimForm) =>
                  val formWithProgress: ClaimForm =
                    claimService.calculateProgress(claimForm.copy(responses = data))

                  val formWithSignatureStatus: ClaimForm =
                    formWithProgress.copy(isSigned = formWithProgress.responses.contains("signature"))
                  logger.info(s"Form isSigned = ${formWithSignatureStatus.isSigned}")

                  (for {
                    formSaveFuture <- formDAO.save(request.identity.userID, claimID, formKey, formWithSignatureStatus)
                    updateUserValuesFuture <- updateUserValues(request.identity, data)
                  } yield {
                    logger.info(s"Form saved and user values updated for ${request.identity.userID}")
                    Created(Json.obj("status" -> "ok"))
                  }).recover {
                    case _: RuntimeException => InternalServerError
                  }

                case None =>
                  Future.successful(NotFound)
              }
            })
        }
    }

  def getFormSignatureStatus(claimID: UUID, formKey: String): Action[AnyContent] = silhouette.SecuredAction.async {
    request =>
      formDAO.find(request.identity.userID, claimID, formKey).map {
        case Some(claimForm) =>
          Ok(JsBoolean(claimForm.responses.contains("signature")))
        case None =>
          NotFound
      }
  }

  def getPdf(claimID: UUID, formKey: String): Action[AnyContent] = silhouette.SecuredAction.async {
    request =>
      formDAO.find(request.identity.userID, claimID, formKey).flatMap {
        case Some(claimForm) =>
          documentService.render(claimForm).map {
            content =>
              logger.info(s"PDF rendered for user ${request.identity.userID}")
              Ok(content).as("application/pdf").withCookies(
                Cookie("fileDownloadToken", "1", secure = false, httpOnly = false))
          }
        case None =>
          Future.successful(NotFound)
      }
  }

  def viewPage(claimID: UUID, formKey: String, page: Int): Action[AnyContent] = silhouette.SecuredAction.async {
    request =>
      formDAO.find(request.identity.userID, claimID, formKey).flatMap {
        case Some(claimForm) =>
          documentService.renderPage(claimForm, page).map {
            content =>
              logger.info(s"PDF page rendered for user ${request.identity.userID}")
              Ok(content).as("image/png")
          }
        case None =>
          Future.successful(NotFound)
      }
  }

  def pdfLoadingScreen(): Action[AnyContent] = Action {
    request =>
      Ok("Please wait while your document is being generated...")
  }

  def updateUserValues(identity: User, values: Map[String, JsValue]): Future[Unit] = {
    Logger.logger.info(s"updateUserValues called with $values")
    userValuesDAO.update(identity.userID, values).flatMap {
      case ok if ok.ok =>
        contactInfoService.updateContactInfo(identity.userID).map {
          case userUpdated if userUpdated.nonEmpty && userUpdated.get.ok =>
            Nil
          case _ =>
            throw new RuntimeException("Failed to update user info.")
        }
      case _ => Future.successful(
        throw new RuntimeException("Failed to update user values."))
    }
  }
}
