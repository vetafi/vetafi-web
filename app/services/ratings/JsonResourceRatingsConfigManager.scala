package services.ratings

import com.google.inject.Inject
import org.log4s.getLogger
import play.api.Configuration
import play.api.libs.json.{ JsArray, JsObject, Json }

class JsonResourceRatingsConfigManager @Inject() (configuration: Configuration) extends RatingsConfigManager {

  private[this] val logger = getLogger
  val ratingsResources = Seq(
    "ratings_for_inactive_nonpulmonary_tuberculosis_in_effect_on_august_19_1968.json",
    "ratings_for_inactive_nonpulmonary_tuberculosis_initially_entitled_after_august_19_1968.json",
    "ratings_of_the_genitourinary_system_diagnoses.json",
    "ratings_of_the_genitourinary_system_dysfunctions.json",
    "schedule_of_ratings_cardiovascular_system.json",
    "schedule_of_ratings_dental_and_oral_conditions.json",
    "schedule_of_ratings_digestive_system.json",
    "schedule_of_ratings_ear.json",
    "schedule_of_ratings_endocrine_system.json",
    "schedule_of_ratings_eye.json",
    "schedule_of_ratings_gynecological_conditions_and_disorders_of_the_breast.json",
    "schedule_of_ratings_hemic_and_lymphatic_systems.json",
    "schedule_of_ratings_infectious_diseases_immune_disorders_and_nutritional_deficiencies.json",
    "schedule_of_ratings_mental_disorders.json",
    "schedule_of_ratings_muscle_injuries.json",
    "schedule_of_ratings_musculoskeletal_system.json",
    "schedule_of_ratings_neurological_conditions_and_convulsive_disorders.json",
    "schedule_of_ratings_other_sense_organs.json",
    "schedule_of_ratings_respiratory_system.json",
    "schedule_of_ratings_skin.json")

  lazy val jsonObjects: Seq[JsObject] = {
    ratingsResources.map {
      fileName =>
        val inputStream = getClass.getClassLoader.getResource(
          s"ratings/$fileName").openStream()
        Json.parse(inputStream).as[JsObject]
    }
  }

  override def getRatingsConfigs = {
    JsArray(jsonObjects)
  }
}
