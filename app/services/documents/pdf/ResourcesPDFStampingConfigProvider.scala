package services.documents.pdf

import javax.inject.Inject

import org.log4s.getLogger
import play.api.Configuration
import play.api.libs.json.Json

class ResourcesPDFStampingConfigProvider @Inject()(configuration: Configuration) extends PDFStampingConfigProvider {

  private[this] val logger = getLogger

  lazy val configs: Map[String, Seq[PDFFieldLocator]] = {
    configuration.getStringSeq("forms.enabled").get.map(
      formKey =>
        Json.parse(
          getClass.getClassLoader.getResourceAsStream(s"forms/$formKey.locators.pdf")).validate[Seq[PDFFieldLocator]]
          .fold(
            errors => {
              val msg = s"Errors while parsing form config JSON from" +
                s" forms/$formKey.locators.pdf: ${errors.toString}"
              logger.error(msg)
              throw new RuntimeException(msg)
            },
            formConfig => {
              (formKey, formConfig)
            }
          )
    ).toMap
  }

  override def getPDFFieldLocators(key: String): Seq[PDFFieldLocator] = configs(key)
}
