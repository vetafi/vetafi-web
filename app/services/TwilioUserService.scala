package services

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import models.TwilioUser

import scala.concurrent.Future

trait TwilioUserService extends IdentityService[TwilioUser] {

  def save(loginInfo: LoginInfo, twilioUser: TwilioUser): Future[Option[TwilioUser]]
}
