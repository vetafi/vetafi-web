package controllers

import java.net.URLDecoder
import java.util.UUID
import javax.inject.Inject

import _root_.services.email.EmailService
import _root_.services.{ AuthTokenService, UserService }
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import play.api.i18n.{ I18nSupport, Messages }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._
import utils.auth.DefaultEnv

import scala.concurrent.Future
import scala.language.postfixOps

/**
 * The `Activate Account` controller.
 */
class ActivateAccountController @Inject() (
  silhouette: Silhouette[DefaultEnv],
  userService: UserService,
  authTokenService: AuthTokenService,
  emailService: EmailService,
  components: ControllerComponents)
  extends AbstractController(components) with I18nSupport {

  /**
   * Sends an account activation email to the user with the given email.
   *
   * @param email The email address of the user to send the activation mail to.
   * @return The result to display.
   */
  def send(email: String): Action[AnyContent] = silhouette.UnsecuredAction.async { implicit request =>
    val decodedEmail = URLDecoder.decode(email, "UTF-8")
    val loginInfo = LoginInfo(CredentialsProvider.ID, decodedEmail)
    val result = Redirect(routes.SignInController.view()).flashing("info" -> Messages("activation.email.sent", decodedEmail))

    userService.retrieve(loginInfo).flatMap {
      case Some(user) if !user.activated =>
        authTokenService.create(user.userID).flatMap { authToken =>
          val url = routes.ActivateAccountController.activate(authToken.id).absoluteURL()
          user.email match {
            case Some(emailAddress) =>
              emailService.sendEmail(
                emailAddress,
                request.messages.messages("email.activate.account.subject"),
                views.txt.emails.activateAccount(user, url).body).map {
                  emailSuccess =>
                    if (emailSuccess) {
                      result
                    } else {
                      Redirect(routes.SignInController.view())
                        .flashing("success" -> request.messages.messages("error"))
                    }
                }
            case None =>
              Future.successful(result)
          }
        }
      case None => Future.successful(result)
    }
  }

  /**
   * Activates an account.
   *
   * @param token The token to identify a user.
   * @return The result to display.
   */
  def activate(token: UUID): Action[AnyContent] = silhouette.UnsecuredAction.async { implicit request =>
    authTokenService.validate(token).flatMap {
      case Some(authToken) => userService.retrieve(authToken.userID).flatMap {
        case Some(user) if user.loginInfo.providerID == CredentialsProvider.ID =>
          userService.save(user.copy(activated = true)).map { _ =>
            Redirect(routes.SignInController.view())
              .flashing("success" -> request.messages.messages("account.activated"))
          }
        case _ => Future.successful(Redirect(routes.SignInController.view())
          .flashing("error" -> request.messages.messages("invalid.activation.link")))
      }
      case None => Future.successful(Redirect(routes.SignInController.view())
        .flashing("error" -> request.messages.messages("invalid.activation.link")))
    }
  }
}
