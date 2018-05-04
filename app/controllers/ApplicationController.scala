package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.{ LogoutEvent, Silhouette }
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc._
import utils.auth.{ DefaultEnv, RedirectSecuredErrorHandler }

/**
 * The basic application controller.
 *
 * @param silhouette             The Silhouette stack.
 * @param socialProviderRegistry The social provider registry.
 */
class ApplicationController @Inject() (
  silhouette: Silhouette[DefaultEnv],
  socialProviderRegistry: SocialProviderRegistry,
  redirectSecuredErrorHandler: RedirectSecuredErrorHandler,
  components: ControllerComponents,
  configuration: Configuration)
  extends AbstractController(components) with I18nSupport {

  /**
   * Handles the Sign Out action.
   *
   * @return The result to display.
   */
  def signOut: Action[AnyContent] = silhouette.SecuredAction(redirectSecuredErrorHandler).async { implicit request =>
    val result = Redirect("/")
    silhouette.env.eventBus.publish(LogoutEvent(request.identity, request))
    silhouette.env.authenticatorService.discard(request.authenticator, result)
  }

  def googleForm: Action[AnyContent] = Action {
    Ok(views.html.googleForm(configuration.getOptional[String]("google.formUrl")))
  }
}
