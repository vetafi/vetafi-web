package services.documents.pdf

import java.io.{ File, FileOutputStream }
import java.util.UUID

import org.apache.commons.io.IOUtils
import org.specs2.execute.{ AsResult, Result }
import org.specs2.specification.ForEach
import play.api.libs.json.{ JsBoolean, JsString }
import play.api.test.PlaySpecification

trait TempDirectory extends ForEach[File] {
  def foreach[R: AsResult](a: File => R): Result = {
    val temp = createTemporaryDirectory("")
    try {
      AsResult(a(temp))
    } finally {
      removeTemporaryDirectory(temp)
    }
  }

  /** Creates a new temporary directory and returns it's location. */
  def createTemporaryDirectory(suffix: String): File = {
    val base = new File(System.getProperty("java.io.tmpdir"))
    val dir = new File(base, UUID.randomUUID().toString + suffix)
    dir.mkdirs()
    dir
  }

  /** Removes a directory (recursively). */
  def removeTemporaryDirectory(dir: File): Unit = {
    def recursion(f: File): Unit = {
      if (f.isDirectory) {
        f.listFiles().foreach(child => recursion(child))
      }
      f.delete()
    }

    recursion(dir)
  }
}

class PDFStampingTest extends PlaySpecification with TempDirectory {

  lazy val SIGNATURE: String = IOUtils.toString(getClass.getClassLoader.getResourceAsStream("test_image"))

  "pdf stamper" should {
    "stamp checkboxes" in { TMP: File =>
      val pdfTemplate =
        getClass.getClassLoader.getResourceAsStream("forms/VBA-21-0966-ARE.pdf")
      val tmpFile = new File(TMP, "test.pdf")

      val idMap = Map(
        "Yes" -> "F[0].Page_1[0].Compensation[1]",
        "No" -> "F[0].Page_1[0].Pension[1]"
      )

      PDFStamping.stampPdf(
        pdfTemplate,
        Map("veteran_previous_claim_with_va_y_n" -> JsString("Yes")),
        Seq(
          PDFFieldLocator(null, "veteran_previous_claim_with_va_y_n", 0, Some(idMap), None, None, false)
        ),
        new FileOutputStream(tmpFile)
      )

      success
    }

    "stamp radios" in { TMP: File =>
      val pdfTemplate =
        getClass.getClassLoader.getResourceAsStream("forms/VBA-21-0966-ARE.pdf")
      val tmpFile = new File(TMP, "test.pdf")

      PDFStamping.stampPdf(
        pdfTemplate,
        Map("veteran_previous_claim_with_va_y_n" -> JsBoolean(true)),
        Seq(
          PDFFieldLocator("F[0].Page_1[0].Compensation[1]", "veteran_previous_claim_with_va_y_n", 0, None, None, None, false)
        ),
        new FileOutputStream(tmpFile)
      )

      success
    }

    "stamp signature" in {
      TMP: File =>
        val pdfTemplate = getClass.getClassLoader.getResourceAsStream("forms/VBA-21-0966-ARE.pdf")
        val tmpFile = new File(TMP, "test.pdf")

        PDFStamping.stampPdf(
          pdfTemplate,
          Map(
            "signature" -> JsString(SIGNATURE)
          ),
          Seq(
            PDFFieldLocator(
              "F[0].Page_1[0].SignatureField1[0]",
              "signature", 0, None, None, None, true
            )
          ),
          new FileOutputStream(tmpFile)
        )

        success
    }
  }
}
