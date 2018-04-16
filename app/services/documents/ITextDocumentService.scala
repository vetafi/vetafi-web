package services.documents

import java.awt.image.RenderedImage
import java.io.{ ByteArrayInputStream, ByteArrayOutputStream, File, InputStream }
import javax.imageio.ImageIO
import javax.inject.Inject

import models.ClaimForm
import org.apache.commons.io.{ FileUtils, IOUtils }
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer
import org.log4s.getLogger
import play.api.Configuration
import services.documents.pdf.{ PDFFieldLocator, PDFStamping, PDFStampingConfigProvider, PDFTemplateProvider }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ITextDocumentService @Inject() (
  pDFStampingConfigProvider: PDFStampingConfigProvider,
  pDFTemplateProvider: PDFTemplateProvider,
  configuration: Configuration) extends DocumentService {

  private[this] val logger = getLogger

  val FONT_FILES_TO_INSTALL = Seq(
    "Courier New Bold Italic.ttf",
    "Courier New Bold.ttf",
    "Courier New Italic.ttf",
    "Courier New.ttf")

  def installFonts(): Unit = {
    val base = System.getProperty("user.home") + "/.fonts"
    val dir = new File(base)
    dir.mkdirs()

    FONT_FILES_TO_INSTALL.foreach {
      fontFile =>
        val stream = getClass.getClassLoader.getResourceAsStream(s"fonts/$fontFile")
        val newFile = new File(base + "/" + fontFile)

        if (!newFile.exists()) {
          logger.info(s"Installing $fontFile")
          FileUtils.writeByteArrayToFile(
            newFile,
            IOUtils.toByteArray(stream),
            false)
        }
    }
  }

  if (configuration.get[Boolean]("fonts.install")) {
    installFonts()
  }

  /**
   * Get PDF for document from document service.
   *
   * @param form
   * @return
   */
  override def render(form: ClaimForm) = Future {
    val locators: Seq[PDFFieldLocator] = pDFStampingConfigProvider.getPDFFieldLocators(form.key)
    val template: InputStream = pDFTemplateProvider.getTemplate(form.key)
    val out = new ByteArrayOutputStream()

    PDFStamping.stampPdf(template, form.responses, locators, out)

    out.toByteArray
  }

  /**
   * Get final signed PDF url for document from document service.
   *
   * @param form
   * @return
   */
  override def renderSigned(form: ClaimForm): Nothing = ???

  /**
   * Submit document to document service for signature.
   *
   * @param form
   * @return
   */
  override def submitForSignature(form: ClaimForm): Nothing = ???

  /**
   * Get signature link for document.
   *
   * @param form
   * @return
   */
  override def signatureLink(form: ClaimForm): Nothing = ???

  /**
   * Get the signature status of the form.
   *
   * @param form
   * @return
   */
  override def isSigned(form: ClaimForm): Nothing = ???

  override def renderPage(form: ClaimForm, page: Int): Future[Array[Byte]] = render(form).map {
    (pdf: Array[Byte]) =>
      val doc = PDDocument.load(new ByteArrayInputStream(pdf))
      try {
        val renderer = new PDFRenderer(doc)
        val image = renderer.renderImageWithDPI(page, 300)
        val out = new ByteArrayOutputStream()
        ImageIO.write(image.asInstanceOf[RenderedImage], "PNG", out)
        out.toByteArray
      } finally {
        doc.close()
      }
  }
}
