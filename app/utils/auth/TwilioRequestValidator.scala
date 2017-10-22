package utils.auth

import play.api.mvc.{ AnyContent, Request }

trait TwilioRequestValidator {
  def authenticate[B <: AnyContent](request: Request[B]): Boolean
}
