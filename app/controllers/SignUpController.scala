package controllers

import java.util.UUID
import javax.inject.Inject

import _root_.services.email.EmailService
import _root_.services.{AuthTokenService, UserService}
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{Clock, PasswordHasherRegistry}
import com.mohiva.play.silhouette.impl.providers._
import forms.VetafiSignUpForm
import models.User
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._
import utils.auth.DefaultEnv

import scala.concurrent.Future

/**
 * The `Sign Up` controller.
 *
 * @param silhouette             The Silhouette stack.
 * @param userService            The user service implementation.
 * @param authInfoRepository     The auth info repository implementation.
 * @param authTokenService       The auth token service implementation.
 * @param passwordHasherRegistry The password hasher registry.
 */
class SignUpController @Inject() (
  silhouette: Silhouette[DefaultEnv],
  userService: UserService,
  authInfoRepository: AuthInfoRepository,
  authTokenService: AuthTokenService,
  passwordHasherRegistry: PasswordHasherRegistry,
  configuration: Configuration,
  components: ControllerComponents,
  emailService: EmailService,
  clock: Clock)
  extends AbstractController(components) with I18nSupport {

  /**
   * Views the `Sign Up` page.
   *
   * @return The result to display.
   */
  def view: Action[AnyContent] = silhouette.UnsecuredAction.async { implicit request =>
    Future.successful(Ok(
      views.html.authLayout(
        "signup-view",
        "")(
          views.html.signupForm(routes.SocialAuthController.authenticate("idme").url))))
  }

  def maybeCreateUser(loginInfo: LoginInfo, data: VetafiSignUpForm.Data): Future[Option[User]] = {
    userService.retrieve(loginInfo).flatMap {
      case Some(_) =>
        Future.successful(None)
      case None =>
        val newUser = User(
          userID = UUID.randomUUID(),
          loginInfo = loginInfo,
          firstName = None,
          lastName = None,
          fullName = None,
          email = Some(data.email),
          avatarURL = None,
          activated = !configuration.get[Boolean]("email.requireActivation"),
          contact = None)
        userService.save(newUser).map((u) => {
          Some(u)
        })
    }
  }

  def maybeSendActivationEmail(user: User, request: Request[AnyContent]): Future[Boolean] = {
    val authTokenFuture: Future[String] = authTokenService.create(user.userID).map {
      authToken =>
        routes.ActivateAccountController.activate(authToken.id).absoluteURL()
    }

    if (configuration.get[Boolean]("email.requireActivation")) {
      authTokenFuture.flatMap {
        authTokenUrl =>
          emailService.sendEmail(
            recipient = user.email.get,
            subject = request.messages.apply("email.sign.up.subject"),
            message = views.txt.emails.signUp(user, authTokenUrl).body)
      }
    } else {
      Future.successful(true)
    }
  }

  /**
   * Handles the submitted form.
   *
   * @return The result to display.
   */
  def submit: Action[AnyContent] = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    VetafiSignUpForm.form.bindFromRequest.fold(
      error =>
        Future.successful(
          BadRequest(
            views.html.authLayout(
              "signup-view",
              error.errorsAsJson.toString())(
                views.html.signupForm(routes.SocialAuthController.authenticate("idme").url)))),
      data => {
        val loginInfo = LoginInfo(CredentialsProvider.ID, data.email)
        val authInfo = passwordHasherRegistry.current.hash(data.password)
        val okResponse = Ok(
          views.html.authLayout(
            "signup-view",
            "")(views.html.signupForm(
            routes.SocialAuthController.authenticate("idme").url)))
        
        val result: Future[Future[Result]] = for {
          maybeUser: Option[User] <- maybeCreateUser(loginInfo, data)
          _ <- authInfoRepository.add(loginInfo, authInfo)
        } yield {
          maybeUser match {
            case Some(noEmailUser) if noEmailUser.email.isEmpty =>
              Future.successful(
                okResponse.flashing("error" ->
                    request.messages.apply("email.activate.send.error")))
            case None =>
              Future.successful(
                okResponse.flashing("error" ->
                    request.messages.apply("email.already.signed.up.error")))
            case Some(user) =>
              val emailSendFuture = maybeSendActivationEmail(user, request)

              emailSendFuture.map {
                case true =>
                  silhouette.env.eventBus.publish(SignUpEvent(user, request))
                  okResponse
                    .flashing("info" ->
                      request.messages.apply("email.activate.send.success"))
                case false =>
                  okResponse
                    .flashing("error" ->
                      request.messages.apply("email.activate.send.error"))
              }
          }
        }
        result.flatMap(identity)
      })
  }
}
