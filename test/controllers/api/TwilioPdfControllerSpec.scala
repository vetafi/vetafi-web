package controllers.api

import java.util.UUID

import controllers.CSRFTest
import models.Claim
import play.api.libs.json.JsResult
import play.api.mvc.AnyContentAsEmpty
import play.api.test.{FakeRequest, PlaySpecification, WithApplication}

class TwilioPdfControllerSpec extends PlaySpecification with CSRFTest {

  "the getPdf action should" should {
    "return 401" in new TwilioPdfControllerTestContext {
      new WithApplication(application) {
        val getRequest: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(controllers.api.routes.TwilioPdfController.getPdf(UUID.randomUUID(), UUID.randomUUID()))
        val csrfReq = addToken(getRequest)
        val getResult = route(app, csrfReq).get

        status(getResult) must be equalTo UNAUTHORIZED
      }
    }
  }
}
