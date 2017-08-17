package services.forms

import com.google.inject.Inject
import models.FormConfig
import play.Configuration
import play.api.libs.json._
import org.log4s._

import scala.collection.JavaConversions._

/**
 * FormConfigManager backed by JSON files in the project resources.
 */
class JsonResourceFormConfigManager @Inject() (configuration: Configuration) extends FormConfigManager {

  private[this] val logger = getLogger

  def loadFormConfigFromResource(formKey: String): FormConfig = {
    val inputStream = getClass.getClassLoader.getResource(
      s"${configuration.getString("forms.dir")}/${formKey}.json"
    ).openStream()
    val result: JsResult[FormConfig] = Json.parse(inputStream).validate[FormConfig]

    result.fold(
      errors => {
        val msg = s"Errors while parsing form config JSON at ${configuration.getString("forms.dir")}/$formKey: ${errors.toString}"
        logger.error(msg)
        throw new RuntimeException(msg)
      },
      formConfig => {
        formConfig
      }
    )
  }

  lazy val formConfigs: Map[String, FormConfig] = {
    val enabledForms = configuration.getStringList("forms.enabled")
    enabledForms.map(
      (formKey: String) => {
        (formKey, loadFormConfigFromResource(formKey))
      }
    ).toMap
  }

  override def getFormConfigs: Map[String, FormConfig] = formConfigs
}
