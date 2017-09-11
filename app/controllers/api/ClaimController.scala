package controllers.api

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import models.daos.{ClaimDAO, FormDAO}
import models._
import play.api.libs.json.{JsError, JsValue, Json}
import play.api.mvc.{Action, _}
import services.documents.DocumentService
import services.forms.{ClaimService, FormConfigManager}
import services.submission.{EmailSubmissionService, FaxSubmissionService, RecipientService}
import utils.auth.DefaultEnv
import org.log4s._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * API endpoint for CRUD operations on claims.
 */
class ClaimController @Inject() (
  val claimDAO: ClaimDAO,
  val formDAO: FormDAO,
  val claimService: ClaimService,
  val documentService: DocumentService,
  val formConfigManager: FormConfigManager,
  val recipientService: RecipientService,
  silhouette: Silhouette[DefaultEnv],
  faxSubmissionService: FaxSubmissionService,
  emailSubmissionService: EmailSubmissionService
) extends Controller {

  private[this] val logger = getLogger

  def getClaims: Action[AnyContent] = silhouette.SecuredAction.async {
    request =>
      {
        claimDAO.findClaims(request.identity.userID).map(
          claims => Ok(Json.toJson(claims))
        )
      }
  }

  def getClaim(claimID: UUID): Action[AnyContent] = silhouette.SecuredAction.async {
    request =>
      {
        claimDAO.findClaim(request.identity.userID, claimID).map {
          case Some(claim) => Ok(Json.toJson(claim))
          case None => NotFound
        }
      }
  }

  private def createForms(userID: UUID, claimID: UUID, forms: Seq[String]): Future[Seq[Boolean]] = {
    val futures = forms.map((key: String) => {
      val newForm = claimService.calculateProgress(ClaimForm(key, Map.empty[String, JsValue], userID, claimID, 0, 0, 0, 0, externalFormId = Some(formConfigManager.getFormConfigs(key).vfi.externalId)))

      for {
        formSaveFuture <- formDAO.save(userID, claimID, key, newForm)
      } yield {
        true
      }

    })

    Future.sequence(futures)
  }

  def create: Action[JsValue] = silhouette.SecuredAction.async(BodyParsers.parse.json) {
    request =>
      {
        val formKeySeqResult = request.body.validate[StartClaimRequest]
        formKeySeqResult.fold(
          errors => {
            Future.successful(BadRequest(Json.obj("status" -> "error", "message" -> JsError.toJson(errors))))
          },
          startClaimRequest => {
            claimDAO.findIncompleteClaim(request.identity.userID).flatMap {
              case Some(claim) => Future.successful(Ok(Json.toJson(claim)))
              case None => claimDAO.create(request.identity.userID, startClaimRequest.key).flatMap {
                case ok if ok.ok => claimDAO.findIncompleteClaim(request.identity.userID).flatMap {
                  case Some(claim) => createForms(claim.userID, claim.claimID, startClaimRequest.forms).map {
                    _ =>
                      MDC.withCtx(
                        "userID" -> claim.userID.toString,
                        "claimID" -> claim.claimID.toString,
                        "claimKey" -> claim.key
                      ) {
                          logger.info(s"Created new claim: $claim")
                        }
                      Created(Json.toJson(claim))
                  }
                  case None => Future.successful(InternalServerError(Json.obj(
                    "status" -> "error"
                  )))
                }
                case _ => Future.successful(InternalServerError(Json.obj(
                  "status" -> "error"
                )))
              }
            }
          }
        )
      }
  }

  def submitClaimToRecipients(claim: Claim): Future[Seq[ClaimSubmission]] = {
    Future.sequence(claim.recipients.map {
      case faxDestination if faxDestination.recipientType == Recipient.Type.FAX =>
        faxSubmissionService.submit(claim)
      case emailDestination if emailDestination.recipientType == Recipient.Type.EMAIL =>
        faxSubmissionService.submit(claim)
    })
  }

  def findUpdateAndSubmitClaim(claim: Claim, recipients: Seq[Recipient]): Future[Result] = {
    claimDAO.save(claim.userID, claim.claimID, claim.copy(recipients = recipients)).flatMap {
      case updateRecipients if updateRecipients.ok => submitClaimToRecipients(claim).flatMap {
        submissions =>
          claimDAO.submit(claim.userID, claim.claimID, submissions).flatMap {
            case submitted if submitted.ok =>
              val allSuccess: Boolean = submissions.map(_.success).reduce(_ && _)
              if (allSuccess) {
                Future.successful(Ok(Json.obj("status" -> "ok", "errors" -> Json.arr())))
              } else {
                val failures: Seq[ClaimSubmission] = submissions.filter(!_.success)
                Future.successful(InternalServerError(
                  Json.obj(
                    "status" -> "error",
                    "errors" -> Json.toJson(failures))))
              }
            case _ => Future.successful(InternalServerError)
          }

      }
      case _ => Future.successful(InternalServerError)
    }
  }

  def submit(claimID: UUID): Action[JsValue] = silhouette.SecuredAction.async(BodyParsers.parse.json) {
    request =>
      {
        val recipientsResult = request.body.validate[Seq[Recipient]]
        recipientsResult.fold(
          errors => {
            Future.successful(BadRequest(Json.obj("status" -> "error", "message" -> JsError.toJson(errors))))
          },
          recipients => {
            claimDAO.findClaim(request.identity.userID, claimID).flatMap {
              case Some(claim) =>
                if (claim.state == Claim.State.INCOMPLETE) {
                  val defaultRecipients = recipientService.getDefaultRecipients(claim)
                  findUpdateAndSubmitClaim(claim, (defaultRecipients ++ recipients).toSeq)
                } else {
                  Future.successful(InternalServerError)
                }
              case None => Future.successful(NotFound)
            }
          }
        )
      }
  }
}
