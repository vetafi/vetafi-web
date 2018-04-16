package models.daos

import javax.inject.Inject

import models.{ Claim, TwilioFax }
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.commands.WriteResult
import reactivemongo.play.json.collection.JSONCollection
import play.modules.reactivemongo.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TwilioFaxDAOImpl @Inject() (val reactiveMongoApi: ReactiveMongoApi) extends TwilioFaxDAO {
  def collection: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection("twilio.fax"))

  override def save(twilioFaxId: String, twilioFax: TwilioFax): Future[WriteResult] = {
    collection.flatMap(
      _.update(Json.obj("twilioFaxId" -> twilioFaxId), twilioFax))
  }

  override def find(twilioFaxId: String): Future[Option[TwilioFax]] = {
    collection.flatMap(_.find(Json.obj("twilioFaxId" -> twilioFaxId)).one[TwilioFax])
  }
}
