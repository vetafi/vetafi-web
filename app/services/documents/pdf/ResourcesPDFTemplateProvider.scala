package services.documents.pdf

import java.io.ByteArrayInputStream
import javax.inject.Inject

import org.apache.commons.io.IOUtils
import play.api.Configuration

class ResourcesPDFTemplateProvider @Inject() (configuration: Configuration) extends PDFTemplateProvider {

  val pdfTemplatesDir: String = configuration.getString("forms.pdfTemplatesDir").get
  lazy val pdfs: Map[String, Array[Byte]] = {
    configuration.getStringSeq("forms.enabled").get.map(formKey =>
      (
        formKey,
        IOUtils.toByteArray(getClass.getClassLoader.getResourceAsStream(s"$pdfTemplatesDir/$formKey.pdf"))
      )).toMap
  }

  override def getTemplate(key: String): ByteArrayInputStream = {
    new ByteArrayInputStream(pdfs(key))
  }
}
