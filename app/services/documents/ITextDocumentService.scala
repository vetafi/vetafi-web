package services.documents

import java.awt.Image
import java.awt.image.RenderedImage
import java.io.{ ByteArrayInputStream, ByteArrayOutputStream, InputStream }
import javax.imageio.ImageIO
import javax.inject.Inject

import models.ClaimForm
import services.documents.pdf.{ PDFFieldLocator, PDFStamping, PDFStampingConfigProvider, PDFTemplateProvider }

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.graphics.color.PDColor
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation
import org.apache.pdfbox.rendering.PDFRenderer
import org.apache.pdfbox.rendering.PageDrawer
import org.apache.pdfbox.rendering.PageDrawerParameters
import org.apache.pdfbox.util.Matrix
import org.apache.pdfbox.util.Vector
import javax.imageio.ImageIO

class ITextDocumentService @Inject() (pDFStampingConfigProvider: PDFStampingConfigProvider, pDFTemplateProvider: PDFTemplateProvider) extends DocumentService {

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
