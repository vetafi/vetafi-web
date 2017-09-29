package models.daos

import javax.inject.Inject

import models.TwilioFax
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.commands.WriteResult
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.Future

class TwilioFaxDAOImpl @Inject()(val reactiveMongoApi: ReactiveMongoApi) extends TwilioFaxDAO {
  def collection: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection("twilio.fax"))

  override def save(twilioFax: TwilioFax): Future[WriteResult] = {
    collection.flatMap(_.insert(twilioFax))
  }
}
