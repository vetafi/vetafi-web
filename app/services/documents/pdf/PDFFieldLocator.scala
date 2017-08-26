package services.documents.pdf

import play.api.libs.json.{ Json, OFormat }

/**
 * JSON Schema for locating the PDF field for a form field
 */
case class PDFFieldLocator(
  pdfId: String,
  elementId: String,
  concatOrder: Int = 0,
  // If the PDF field id depends on the value (i.e. checkbox components)
  idMap: Option[Map[String, String]] = None,
  // Define a substring of the value to be placed in the field
  substringStart: Option[Int] = None,
  substringEnd: Option[Int] = None,
  // Is the value a base64 encoded image to be overlayed?
  isBase64ImageBlob: Boolean = false
) {
}

object PDFFieldLocator {
  implicit val jsonFormat: OFormat[PDFFieldLocator] = Json.format[PDFFieldLocator]
}
