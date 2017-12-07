package models.daos

import models.TwilioFax
import reactivemongo.api.commands.WriteResult

import scala.concurrent.Future

trait TwilioFaxDAO {
  def save(twilioFaxId: String, twilioFax: TwilioFax): Future[WriteResult]

  def find(twilioFaxId: String): Future[Option[TwilioFax]]
}
