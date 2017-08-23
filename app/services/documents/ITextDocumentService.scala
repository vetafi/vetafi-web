package services.documents
import models.ClaimForm
import services.documents.pdf.PDFStamping

class ITextDocumentService extends DocumentService {

  /**
   * Get PDF for document from document service.
   *
   * @param form
   * @return
   */
  override def render(form: ClaimForm) = ???

  /**
   * Get final signed PDF url for document from document service.
   *
   * @param form
   * @return
   */
  override def renderSigned(form: ClaimForm) = ???

  /**
   * Submit document to document service for signature.
   *
   * @param form
   * @return
   */
  override def submitForSignature(form: ClaimForm) = ???

  /**
   * Get signature link for document.
   *
   * @param form
   * @return
   */
override def signatureLink(form: ClaimForm) = ???

  /**
   * Get the signature status of the form.
   *
   * @param form
   * @return
   */
  override def isSigned(form: ClaimForm) = ???
}
