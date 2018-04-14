package models.daos

import java.util.UUID
import javax.inject.Inject

import models._
import play.api.Logger
import play.api.libs.json.{ Json, _ }
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json._
import reactivemongo.api.commands.{ UpdateWriteResult, WriteResult }
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserValuesDAOImpl @Inject() (
  val reactiveMongoApi: ReactiveMongoApi,
  val userDAO: UserDAO) extends UserValuesDAO {

  def collection: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection("user_values"))

  /**
   * Finds user values for a user by their userID.
   *
   * @param userID The ID of the user to find values for.
   * @return The found user values or None if no user for the given ID could be found.
   */
  override def find(userID: UUID): Future[Option[UserValues]] = {
    collection.flatMap {
      _.find(Json.obj("userID" -> userID.toString)).one[UserValues]
    }
  }

  /**
   * Update the [String, String] map of user values.
   *
   * @param values New user values, will overwrite existing values of the same key.
   */
  override def update(userID: UUID, values: Map[String, JsValue]): Future[WriteResult] = {
    collection.flatMap((userValuesCollection: JSONCollection) => {

      val userValuesOptionFuture: Future[Option[UserValues]] =
        userValuesCollection.find(Json.obj("userID" -> userID)).one[UserValues]

      val existingValuesFuture: Future[Map[String, JsValue]] = userValuesOptionFuture.map {
        case userValues if userValues.nonEmpty => userValues.get.values
        case _ => Map()
      }

      existingValuesFuture.flatMap((existingValues: Map[String, JsValue]) => {
        Logger.logger.info(s"Existing values: $existingValues")
        userValuesCollection.update(
          Json.obj("userID" -> userID),
          // The values on the RHS of `++` will overwrite the values of the LHS
          Json.obj("$set" -> Json.obj("values" -> Json.toJson(existingValues ++ values))))
      })
    })
  }

  override def initialize(userID: UUID): Future[WriteResult] = collection.flatMap {
    (userValuesCollection: JSONCollection) =>
      userValuesCollection.find(Json.obj("userID" -> userID)).one[User].flatMap {
        case Some(_) =>
          Future.successful(UpdateWriteResult(
            ok = true,
            1, 1, Seq(), Seq(), None, None, None))
        case None =>
          userValuesCollection.insert(UserValues(userID, Map()))
      }
  }
}
