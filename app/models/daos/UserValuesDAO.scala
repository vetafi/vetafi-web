package models.daos

import java.util.UUID

import models.{ ClaimForm, UserValues }
import play.api.libs.json.JsValue
import reactivemongo.api.commands.WriteResult

import scala.concurrent.Future

trait UserValuesDAO {

  /**
   * Finds user values for a user by their userID.
   *
   * @param userID The ID of the user to find values for.
   * @return The found user values or None if no user for the given ID could be found.
   */
  def find(userID: UUID): Future[Option[UserValues]]

  /**
   *
   */
  def update(userID: UUID, values: Map[String, JsValue]): Future[WriteResult]

  def initialize(userID: UUID): Future[WriteResult]
}
