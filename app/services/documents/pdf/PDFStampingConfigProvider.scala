package services.documents.pdf

trait PDFStampingConfigProvider {

  def getPDFFieldLocators(key: String): Seq[PDFFieldLocator]
}
