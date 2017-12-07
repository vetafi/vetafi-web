package models

import java.util.UUID

import play.api.libs.json.{ Format, Json, OFormat }
import utils.EnumUtils

case class Recipient(
  recipientType: Recipient.Type.Value,
  value: String
) {

}

object Recipient {

  object Type extends Enumeration {
    type Type = Value
    val FAX, EMAIL = Value
  }

  implicit val enumFormat: Format[Recipient.Type.Value] = EnumUtils.enumFormat(Recipient.Type)

  implicit val jsonFormat: OFormat[Recipient] = Json.format[Recipient]

}

case class StartClaimRequest(
  key: String,
  description: String,
  forms: Seq[String]
) {

}

object StartClaimRequest {
  implicit val jsonFormat: OFormat[StartClaimRequest] = Json.format[StartClaimRequest]
}

/**
 * A claim represents 1 or more Forms grouped together for submission.
 */
case class Claim(
  userID: UUID,
  claimID: UUID,
  key: String,
  state: Claim.State.Value,
  stateUpdatedAt: java.util.Date,
  recipients: Seq[Recipient],
  submissions: Seq[ClaimSubmission] = Seq.empty[ClaimSubmission]
) {

}

object Claim {

  object State extends Enumeration {
    type State = Value
    val INCOMPLETE, SIGNING, DISCARDED, SUBMITTED, SUBMISSION_ERROR = Value
  }

  implicit val enumFormat: Format[Claim.State.Value] = EnumUtils.enumFormat(Claim.State)

  implicit val jsonFormat: OFormat[Claim] = Json.format[Claim]
}

case class ClaimSubmission(
  claimSubmissionID: UUID,
  to: String,
  from: String,
  method: String,
  dateSubmitted: java.util.Date,
  success: Boolean
) {

}

object ClaimSubmission {
  implicit val jsonFormat: OFormat[ClaimSubmission] = Json.format[ClaimSubmission]
}
