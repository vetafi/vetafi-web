package services.documents.pdf

import java.io.ByteArrayInputStream
import javax.inject.Inject

import org.apache.commons.io.IOUtils
import play.api.Configuration

class ResourcesPDFTemplateProvider @Inject() (configuration: Configuration) extends PDFTemplateProvider {

  val pdfTemplatesDir: String = configuration.get[String]("forms.pdfTemplatesDir")
  lazy val pdfs: Map[String, Array[Byte]] = {
    configuration.get[Seq[String]]("forms.enabled").map(formKey =>
      (
        formKey,
        IOUtils.toByteArray(getClass.getClassLoader.getResourceAsStream(s"$pdfTemplatesDir/$formKey.pdf")))).toMap
  }

  override def getTemplate(key: String): ByteArrayInputStream = {
    new ByteArrayInputStream(pdfs(key))
  }
}
