package models

import java.util.UUID

import play.api.libs.json.{ Json, OFormat }

case class TwilioFax(
  userID: UUID,
  claimID: UUID,
  claimSubmissionID: UUID,
  dateCreated: java.util.Date,
  dateUpdated: java.util.Date,
  to: String,
  from: String,
  twilioFaxId: String,
  status: String) {

}

object TwilioFax {
  implicit val jsonFormat: OFormat[TwilioFax] = Json.format[TwilioFax]
}
