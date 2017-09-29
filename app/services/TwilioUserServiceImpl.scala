package services

import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import models.TwilioUser

import scala.concurrent.Future

class TwilioUserServiceImpl @Inject() (twilioUserDAO: DelegableAuthInfoDAO[TwilioUser]) extends TwilioUserService {
  override def retrieve(loginInfo: LoginInfo): Future[Option[TwilioUser]] = {
    twilioUserDAO.find(loginInfo)
  }

  /**
   * Save new TwilioUser or update the existing TwilioUser info.
   */
  override def save(loginInfo: LoginInfo, twilioUser: TwilioUser): Future[TwilioUser] = {
    twilioUserDAO.save(loginInfo, twilioUser)
  }
}
