package services.documents

import java.io.{ByteArrayOutputStream, InputStream}
import javax.inject.Inject

import models.ClaimForm
import services.documents.pdf.{PDFFieldLocator, PDFStamping, PDFStampingConfigProvider, PDFTemplateProvider}

import scala.concurrent.Future

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

    val responsesAsString: Map[String, String] = form.responses.map {
      case (k, v) => (k, v.toString())
    }

    PDFStamping.stampPdf(template, responsesAsString, locators, out)

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
}
