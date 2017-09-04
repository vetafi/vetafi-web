package services.documents

import java.awt.Image
import java.io.{ByteArrayInputStream, ByteArrayOutputStream, IOException, InputStream}
import javax.inject.Inject

import org.ghost4j.document.PDFDocument
import org.ghost4j.renderer.SimpleRenderer
import javax.imageio.ImageIO
import java.awt.image.RenderedImage

import models.ClaimForm
import services.documents.pdf.{PDFFieldLocator, PDFStamping, PDFStampingConfigProvider, PDFTemplateProvider}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConversions._

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

      val document: PDFDocument = new PDFDocument
      document.load(new ByteArrayInputStream(pdf))

      // create renderer
      val renderer: SimpleRenderer = new SimpleRenderer

      // set resolution (in DPI)
      renderer.setResolution(300)

      // render
      val images: Seq[Image] = renderer.render(document)

      val out = new ByteArrayOutputStream()
      ImageIO.write(images(page).asInstanceOf[RenderedImage], "png", out)

      out.toByteArray
  }
}
