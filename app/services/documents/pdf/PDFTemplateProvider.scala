package services.documents.pdf

import java.io.InputStream

trait PDFTemplateProvider {

  def getTemplate(key: String): InputStream
}
