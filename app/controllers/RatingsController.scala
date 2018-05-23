package controllers

import javax.inject.Inject

import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import services.ratings.RatingsConfigManager

class RatingsController @Inject()(components: ControllerComponents,
                                  ratingsConfigManager: RatingsConfigManager)
  extends AbstractController(components) {

  def get(): Action[AnyContent] = Action {
    Ok(ratingsConfigManager.getRatingsConfigs)
  }
}
