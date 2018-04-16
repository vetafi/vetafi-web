package services.forms

import java.util.UUID

import models._
import org.mockito.Mockito
import play.api.libs.json.JsString
import play.api.test.{ PlaySpecification, WithApplication }

class ClaimServiceSpec extends PlaySpecification {

  "The ClaimServiceImpl" should {
    "evaluate hide expressions correctly" in new FormTestContext {

      new WithApplication(application) {

        val claimService: ClaimServiceImpl = app.injector.instanceOf[ClaimServiceImpl]

        claimService.shouldBeAnswered(Map("condition" -> JsString("x")))(
          Field(
            "test",
            Field.TemplateType.input,
            TemplateOptions("test", None, None, None, None),
            None,
            Some("model.condition === \"x\""))) must beFalse

        claimService.shouldBeAnswered(Map("condition" -> JsString("y")))(
          Field(
            "test",
            Field.TemplateType.input,
            TemplateOptions("test", None, None, None, None),
            None,
            Some("model.condition === \"x\""))) must beTrue
      }
    }
  }

  "calculate remaining questions correctly" in new FormTestContext {
    Mockito.when(mockFormConfigManager.getFormConfigs).thenReturn(mockFormConfig)

    new WithApplication(application) {
      val claimService: ClaimServiceImpl = app.injector.instanceOf[ClaimServiceImpl]

      val result: ClaimForm = claimService.calculateProgress(
        ClaimForm("test", Map(
          "condition1" -> JsString("x"),
          "condition2" -> JsString("y"),
          "optionalField1" -> JsString("answer"),
          "requiredField1" -> JsString("answer")), UUID.randomUUID(), UUID.randomUUID(), 0, 0, 0, 0))

      result.answeredOptional must be equalTo 1
      result.answeredRequired must be equalTo 1
      result.optionalQuestions must be equalTo 2
      result.requiredQuestions must be equalTo 3
    }
  }
}
