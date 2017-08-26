package services.documents.pdf

import javax.inject.Inject

import org.log4s.getLogger
import play.api.Configuration
import play.api.libs.json.Json

class ResourcesPDFStampingConfigProvider @Inject() (configuration: Configuration) extends PDFStampingConfigProvider {

  private[this] val logger = getLogger
  val pdfTemplateConfigsDir: String = configuration.getString("forms.pdfTemplateConfigsDir").get

  lazy val configs: Map[String, Seq[PDFFieldLocator]] = {
    configuration.getStringSeq("forms.enabled").get.map(
      formKey => {
        logger.info(s"Parsing form config JSON from $pdfTemplateConfigsDir/$formKey.locators.json")
        Json.parse(
          getClass.getClassLoader.getResourceAsStream(s"$pdfTemplateConfigsDir/$formKey.locators.json")
        ).validate[Seq[PDFFieldLocator]]
          .fold(
            errors => {
              val msg = s"Errors while parsing form config JSON from" +
                s" $pdfTemplateConfigsDir/$formKey.locators.json: ${errors.toString}"
              logger.error(msg)
              throw new RuntimeException(msg)
            },
            formConfig => {
              (formKey, formConfig)
            }
          )
      }
    ).toMap
  }

  override def getPDFFieldLocators(key: String): Seq[PDFFieldLocator] = configs(key)
}
