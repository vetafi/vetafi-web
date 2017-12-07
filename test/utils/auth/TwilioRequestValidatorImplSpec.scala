package utils.auth

import org.mockito.Mockito
import play.api.Configuration
import play.api.test.{ FakeRequest, PlaySpecification }
import utils.secrets.SecretsManager

class TwilioRequestValidatorImplSpec extends PlaySpecification {

  "The twilio request validator" should {
    "validate the example request from documentation" in {
      val mockConfiguration = Mockito.mock(classOf[Configuration])
      val mockSecretsManager = Mockito.mock(classOf[SecretsManager])

      Mockito.when(mockConfiguration.getString("twilio.authTokenSecretName"))
        .thenReturn(Some("fakeSecretName"))

      Mockito.when(mockSecretsManager.getSecretUtf8("fakeSecretName")).thenReturn("12345")

      val requestValidator = new TwilioRequestValidatorImpl(mockConfiguration, mockSecretsManager)

      val request = FakeRequest("POST", "/myapp.php?foo=1&bar=2")
        .withHeaders(
          "X-Twilio-Signature" -> "RSOYDt4T1cUTdK1PDd93/VVr8B8=",
          "X-Forwarded-Proto" -> "https",
          "Host" -> "mycompany.com")
        .withFormUrlEncodedBody(
          "CallSid" -> "CA1234567890ABCDE",
          "Caller" -> "+14158675309",
          "Digits" -> "1234",
          "From" -> "+14158675309",
          "To" -> "+18005551212"
        )

      val result: Boolean = requestValidator.authenticate(request)

      result must beTrue
    }
  }
}
