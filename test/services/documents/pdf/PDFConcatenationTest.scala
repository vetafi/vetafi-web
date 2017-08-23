package services.documents.pdf

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import com.itextpdf.text.pdf.PdfReader
import play.api.test.PlaySpecification


class PDFConcatenationTest extends PlaySpecification {

  "the concat method" should {
    "produce the correct number of pages when two documents are concatenated" in {
      val pdf1 =
        getClass.getClassLoader.getResourceAsStream("forms/VBA-21-0966-ARE.pdf")
      val pdf2 =
        getClass.getClassLoader.getResourceAsStream("forms/VBA-21-526EZ-ARE.pdf")

      val byteArrayOutputStream = new ByteArrayOutputStream()

      PDFConcatenation.concat(Seq(pdf1, pdf2), byteArrayOutputStream)

      val concatReader = new PdfReader(new ByteArrayInputStream(byteArrayOutputStream.toByteArray))

      val pdf1Reader =
        new PdfReader(getClass.getClassLoader.getResourceAsStream("forms/VBA-21-0966-ARE.pdf"))
      val pdf2Reader =
        new PdfReader(getClass.getClassLoader.getResourceAsStream("forms/VBA-21-526EZ-ARE.pdf"))

      pdf1Reader.getNumberOfPages + pdf2Reader.getNumberOfPages must be equalTo concatReader.getNumberOfPages
    }
  }
}
