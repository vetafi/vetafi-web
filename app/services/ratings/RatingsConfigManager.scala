package services.ratings

import play.api.libs.json.JsValue

trait RatingsConfigManager {

  def getRatingsConfigs: JsValue
}
