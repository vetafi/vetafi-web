package controllers

import javax.inject.Inject

import services.email.EmailService
import services.{ AuthTokenService, UserService }
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import forms.ForgotPasswordForm
import models.User
import play.api.Configuration
import play.api.i18n.{ I18nSupport, Messages }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._
import utils.auth.DefaultEnv

import scala.concurrent.Future

/**
 * The `Forgot Password` controller.
 */
class ForgotPasswordController @Inject() (
  silhouette: Silhouette[DefaultEnv],
  userService: UserService,
  authTokenService: AuthTokenService,
  emailService: EmailService,
  components: ControllerComponents,
  configuration: Configuration)
  extends AbstractController(components) with I18nSupport {

  /**
   * Views the `Forgot Password` page.
   *
   * @return The result to display.
   */
  def view: Action[AnyContent] = silhouette.UnsecuredAction.async { implicit request =>
    Future.successful(Ok(
      views.html.authLayout(
        "forgot-password-view",
        "")(
          views.html.forgotPassword(ForgotPasswordForm.form))))
  }

  def trySendEmailAndGenerateResponse(user: User)(implicit messages: Messages): Future[Result] = {
    authTokenService.create(user.userID).flatMap { authToken =>

      val url = configuration.getOptional[String]("scheme").getOrElse("http://") +
        configuration.getOptional[String]("hostname").getOrElse("localhost") +
        routes.ResetPasswordController.view(authToken.id).url

      val emailFuture: Future[Boolean] = emailService.sendEmail(
        recipient = user.email.get,
        subject = messages("email.reset.password.subject"),
        message = views.txt.emails.resetPassword(user, url).body)

      emailFuture.map {
        emailResult: Boolean =>
          if (emailResult) {
            Redirect(routes.SignInController.view())
              .flashing("info" -> messages("reset.email.sent"))
          } else {

            Redirect(routes.SignInController.view())
              .flashing("error" -> messages("error"))
          }
      }.recover {
        case e =>
          Redirect(routes.SignInController.view())
            .flashing("info" -> messages("error"))
      }
    }
  }

  def tryGetUserAndGenerateResponse(email: String)(implicit messages: Messages): Future[Result] = {
    val loginInfo = LoginInfo(CredentialsProvider.ID, email)
    userService.retrieve(loginInfo).flatMap {
      case Some(user) if user.email.isDefined =>
        trySendEmailAndGenerateResponse(user)
      case None =>
        Future.successful(Redirect(routes.SignInController.view())
          .flashing("info" -> messages("error")))
    }
  }

  /**
   * Sends an email with password reset instructions.
   *
   * It sends an email to the given address if it exists in the database. Otherwise we do not show the user
   * a notice for not existing email addresses to prevent the leak of existing email addresses.
   *
   * @return The result to display.
   */
  def submit: Action[AnyContent] = silhouette.UnsecuredAction.async {
    implicit request: Request[AnyContent] =>
      ForgotPasswordForm.form.bindFromRequest.fold(
        errors => Future.successful(BadRequest(
          views.html.authLayout(
            "forgot-password-view",
            "Error.")(
              views.html.forgotPassword(errors)))),
        (email: String) => {
          tryGetUserAndGenerateResponse(email)
        })
  }
}
