package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{ Credentials, PasswordHasherRegistry, PasswordInfo }
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import forms.{ ChangePasswordForm, ForgotPasswordForm }
import play.api.i18n.{ I18nSupport, Messages, MessagesApi }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.{ Action, AnyContent, Controller }
import _root_.services.UserService
import utils.auth.{ DefaultEnv, WithProvider }

import scala.concurrent.Future

/**
 * The `Change Password` controller.
 *
 * @param messagesApi            The Play messages API.
 * @param silhouette             The Silhouette stack.
 * @param userService            The user service implementation.
 * @param credentialsProvider    The credentials provider.
 * @param authInfoRepository     The auth info repository.
 * @param passwordHasherRegistry The password hasher registry.
 */
class ChangePasswordController @Inject() (
  val messagesApi: MessagesApi,
  silhouette: Silhouette[DefaultEnv],
  userService: UserService,
  credentialsProvider: CredentialsProvider,
  authInfoRepository: AuthInfoRepository,
  passwordHasherRegistry: PasswordHasherRegistry)
  extends Controller with I18nSupport {

  /**
   * Views the `Change Password` page.
   *
   * @return The result to display.
   */
  def view: Action[AnyContent] = silhouette.SecuredAction(WithProvider[DefaultEnv#A](CredentialsProvider.ID)) { implicit request =>
    Ok(
      views.html.authLayout(
        "change-password-view",
        "")(
          views.html.changePassword(ChangePasswordForm.form, request.identity)))
  }

  /**
   * Changes the password.
   *
   * @return The result to display.
   */
  def submit: Action[AnyContent] = silhouette.SecuredAction(WithProvider[DefaultEnv#A](CredentialsProvider.ID)).async { implicit request =>
    ChangePasswordForm.form.bindFromRequest.fold(
      errors => Future.successful(BadRequest(
        views.html.authLayout(
          "forgot-password-view",
          "Error.")(
            views.html.changePassword(errors, request.identity)))),
      password => {
        val (currentPassword, newPassword, confirmPassword) = password
        if (newPassword != confirmPassword) {
          BadRequest(
            views.html.authLayout(
              "forgot-password-view",
              "Passwords do not match.")(
                views.html.changePassword(ChangePasswordForm.form, request.identity)))
        }

        val credentials = Credentials(request.identity.email.getOrElse(""), currentPassword)
        credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
          val passwordInfo = passwordHasherRegistry.current.hash(newPassword)
          authInfoRepository.update[PasswordInfo](loginInfo, passwordInfo).map { _ =>
            Ok
          }
        }.recover {
          case e: ProviderException => InternalServerError
        }
      })
  }
}
