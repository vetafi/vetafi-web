package services.documents.pdf

import play.api.test.PlaySpecification

class PDFMappingTest extends PlaySpecification {

  "mapStringValues" should {
    "substring and concat correctly" in {
      val stringStringMap = PDFMapping.mapStringValues(
        Map(
          ("field1", "a"),
          ("field2", "b"),
          ("field3", "c"),
          ("field4", "abc")
        ),
        Seq(
          PDFFieldLocator("pdfField", "field1", 0, None, None, None, false),
          PDFFieldLocator("pdfField", "field2", 1, None, None, None, false),
          PDFFieldLocator("pdfField", "field3", 2, None, None, None, false),
          PDFFieldLocator("pdfSubSField", "field4", 0, None, Some(0), Some(1), false)
        ))

      "a b c" must be equalTo stringStringMap("pdfField")
      "a" must be equalTo stringStringMap("pdfSubSField")
    }
  }
}
