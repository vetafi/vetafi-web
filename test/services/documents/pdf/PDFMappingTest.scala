package services.documents.pdf

import play.api.libs.json.JsString
import play.api.test.PlaySpecification

class PDFMappingTest extends PlaySpecification {

  "mapStringValues" should {
    "substring and concat correctly" in {
      val stringStringMap = PDFMapping.mapStringValues(
        Map(
          ("field1", JsString("a")),
          ("field2", JsString("b")),
          ("field3", JsString("c")),
          ("field4", JsString("abc"))
        ),
        Seq(
          PDFFieldLocator("pdfField", "field1", Some(0), None, None, None, None),
          PDFFieldLocator("pdfField", "field2", Some(1), None, None, None, None),
          PDFFieldLocator("pdfField", "field3", Some(2), None, None, None, None),
          PDFFieldLocator("pdfSubSField", "field4", Some(0), None, Some(0), Some(1), None)
        )
      )

      "a b c" must be equalTo stringStringMap("pdfField")
      "a" must be equalTo stringStringMap("pdfSubSField")
    }
  }
}
