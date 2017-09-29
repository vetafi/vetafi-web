package models.daos

import models.TwilioFax
import reactivemongo.api.commands.WriteResult

import scala.concurrent.Future

trait TwilioFaxDAO {
  def save(twilioFax: TwilioFax): Future[WriteResult]
}
