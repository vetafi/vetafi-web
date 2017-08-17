package utils.seamlessdocs

import play.api.libs.json.{ JsValue, Json, OFormat }

/**
 * Case classes representing the API response from the seamless docs api.
 */
case class SeamlessErrorResponse(error: Boolean, error_log: Seq[SeamlessAPIError]) {

}

object SeamlessErrorResponse {
  implicit val jsonFormat: OFormat[SeamlessErrorResponse] = Json.format[SeamlessErrorResponse]
}

case class SeamlessAPIError(
  error_code: String,
  error_message: String,
  error_description: Option[String]
) {

}

object SeamlessAPIError {
  implicit val jsonFormat: OFormat[SeamlessAPIError] = Json.format[SeamlessAPIError]
}
/*
    * {
    * "modified_ts": "2015-09-25 22:40:51.281342",
    * "created_ts": "2015-09-25 22:40:42.709018",
    * "application_id": "AP15091000010170784",
    * "form_id": "CO15081000010137483",
    * "user_id": null,
    * "notes": null,
    * "is_active": "t",
    * "application_data": {
    * "some_input": {
    * "input_name": "some_input",
    * "raw_value": "abc",
    * "label": "Some Input Field"
    * },
    * "another_input": {
    * "input_name": "another_input",
    * "raw_value": "def",
    * "label": "Another Input Field"
    * }
    * },
    * "pdf_cloud_file_id": null,
    * "user_agent_xml": [],
    * "geo_data_xml": {
    * "continent_code": "NA",
    * "country_code": "US",
    * "country_code3": "USA",
    * "country_name": "United States",
    * "region": "FL",
    * "city": "Miami",
    * "postal_code": "33125",
    * "latitude": "25.782899856567",
    * "longitude": "-80.239501953125",
    * "dma_code": "528",
    * "area_code": "305",
    * "ip_address": "73.46.132.58"
    * },
    * "referrer_url": "https://www.seamlessdocs.com",
    * "ip_address": "73.46.132.58",
    * "submission_pdf_url": "http://dev-cdn.seamlessdocs.com/sig_files/201509/Form_For_Testing_1FURGp44U2xMXN.pdf",
    * "field_positions_xml": [],
    * "group_id": null,
    * "overrides_xml": [],
    * "submission_file_urls": [
    * "http://dev-cdn.seamlessdocs.com/sig_files/201509/Form_For_Testing_1FURGp44U2xMXN.tiff"
    * ],
    * "is_incomplete": null
    * }
 */
case class SeamlessApplication(
  modified_ts: java.util.Date,
  created_ts: java.util.Date,
  application_id: String,
  form_id: String,
  is_active: String,
  application_data: Option[Map[String, SeamlessApplicationDatum]],
  submission_pdf_url: String
) {

}

object SeamlessApplication {
  implicit val jsonFormat: OFormat[SeamlessApplication] = Json.format[SeamlessApplication]
}

case class SeamlessApplicationDatum(input_name: String, raw_value: String, label: String) {

}

object SeamlessApplicationDatum {
  implicit val jsonFormat: OFormat[SeamlessApplicationDatum] = Json.format[SeamlessApplicationDatum]
}

/**
 * {
 *   "legal_name": "John Doe",
 *   "birth_date": "June 2, 1978",
 *   "gender": "Male",
 *   "signer_data": {
 *     "fullname": "City Clerk",
 *     "email":"preparer@example.com"
 *   },
 *   "recipients": {
 *     "prepared_for": {
 *       "fullname": "John Doe",
 *       "email": "johndoe@example.com"
 *     }
 *   }
 * }
 */
case class SeamlessFormPreparation(
  singer_data: SeamlessRecipientInfo,
  recipients: SeamlessRecipient,
  answers: Map[String, JsValue]
) {

}

object SeamlessFormPreparation {
  implicit val jsonFormat: OFormat[SeamlessFormPreparation] = Json.format[SeamlessFormPreparation]
}

case class SeamlessRecipient(prepared_for: SeamlessRecipientInfo) {

}

object SeamlessRecipient {
  implicit val jsonFormat: OFormat[SeamlessRecipient] = Json.format[SeamlessRecipient]

}

case class SeamlessRecipientInfo(fullname: String, email: String) {

}

object SeamlessRecipientInfo {
  implicit val jsonFormat: OFormat[SeamlessRecipientInfo] = Json.format[SeamlessRecipientInfo]
}

/**
 * {
 * "error_code": "submission_halt",
 * "error_message": "Error executing signature workflow: Unable to find recipient info of the next signer '71f0d6844867a206d837f6728ddb545b'",
 * "error_description": "submission:execute"
 * }
 */
case class SeamlessErrorLog(
  error_code: String,
  error_message: String,
  error_description: String
)

object SeamlessErrorLog {
  implicit val jsonFormat: OFormat[SeamlessErrorLog] = Json.format[SeamlessErrorLog]
}

/**
 * {
 * "result": true,
 * "application_id": "AP15021000011409822",
 * "description": "Submission successful"
 * }
 */
case class SeamlessApplicationCreateResponse(
  result: Boolean,
  application_id: String,
  description: String
) {

}

object SeamlessApplicationCreateResponse {
  implicit val jsonFormat: OFormat[SeamlessApplicationCreateResponse] = Json.format[SeamlessApplicationCreateResponse]
}

/**
 * {
 * "status": "Incomplete (pending)",
 * "total_signers": 3,
 * "signatures": 1
 * }
 */
case class SeamlessApplicationStatus(
  status: String,
  total_signers: Int,
  signatures: Int
) {

}

object SeamlessApplicationStatus {
  implicit val jsonFormat: OFormat[SeamlessApplicationStatus] = Json.format[SeamlessApplicationStatus]
}

/**
 * {"key":"2748132ac631d103455f407e6250ad9f","recipient_type":"userdefined","order":"1","reference":"Signer1","color":"#D7CB35","signer_type":"userdefined","signer_key":"2748132ac631d103455f407e6250ad9f"}
 */
case class SeamlessSigner(
  key: String,
  recipient_type: String,
  order: Option[String],
  reference: String,
  color: String,
  signer_type: String,
  signer_key: String
)

object SeamlessSigner {
  implicit val jsonFormat: OFormat[SeamlessSigner] = Json.format[SeamlessSigner]
}
