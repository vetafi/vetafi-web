package services.documents.pdf

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import com.itextpdf.text.pdf.PdfReader
import org.apache.commons.io.IOUtils
import play.api.test.PlaySpecification
import services.documents.pdf.ITextPDFConcatenator

class ITextPDFConcatenatorTest extends PlaySpecification {

  "the concat method" should {
    "produce the correct number of pages when two documents are concatenated" in {
      val pdf1: Array[Byte] =
        IOUtils.toByteArray(getClass.getClassLoader.getResourceAsStream("forms/VBA-21-0966-ARE.pdf"))
      val pdf2: Array[Byte] =
        IOUtils.toByteArray(getClass.getClassLoader.getResourceAsStream("forms/VBA-21-526EZ-ARE.pdf"))

      val result: Array[Byte] = new ITextPDFConcatenator().concat(Seq(pdf1, pdf2))

      val concatReader: PdfReader = new PdfReader(new ByteArrayInputStream(result))

      val pdf1Reader: PdfReader =
        new PdfReader(getClass.getClassLoader.getResourceAsStream("forms/VBA-21-0966-ARE.pdf"))
      val pdf2Reader: PdfReader =
        new PdfReader(getClass.getClassLoader.getResourceAsStream("forms/VBA-21-526EZ-ARE.pdf"))

      pdf1Reader.getNumberOfPages + pdf2Reader.getNumberOfPages must be equalTo concatReader.getNumberOfPages
    }
  }
}
