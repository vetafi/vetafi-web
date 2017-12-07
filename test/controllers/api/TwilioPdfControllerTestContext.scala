package controllers.api

import java.time.Instant
import java.util.UUID

import com.google.inject.AbstractModule
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.BasicAuthProvider
import com.mohiva.play.silhouette.impl.util.SecureRandomIDGenerator
import com.mohiva.play.silhouette.persistence.daos.{DelegableAuthInfoDAO, MongoAuthInfoDAO}
import com.typesafe.config.ConfigFactory
import models.daos.FormDAO
import models._
import modules.JobModule
import net.codingwell.scalaguice.ScalaModule
import org.mockito.Mockito
import org.specs2.specification.Scope
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsValue
import play.api.{Application, Configuration}
import services.documents.DocumentService
import services.documents.pdf.PDFConcatenator
import utils.auth.{TwilioRequestValidator, TwilioRequestValidatorImpl}
import utils.secrets.SecretsManager

trait TwilioPdfControllerTestContext extends Scope {

  val mockFormDao: FormDAO = Mockito.mock(classOf[FormDAO])
  val mockDocumentService: DocumentService = Mockito.mock(classOf[DocumentService])
  val mockBasicAuthProvider: BasicAuthProvider = Mockito.mock(classOf[BasicAuthProvider])
  val mockTwilioUserDao: DelegableAuthInfoDAO[TwilioUser] = Mockito.mock(classOf[DelegableAuthInfoDAO[TwilioUser]])
  val mockPdfConcatenator: PDFConcatenator = Mockito.mock(classOf[PDFConcatenator])
  val mockSecureRandomIdGenerator: SecureRandomIDGenerator = Mockito.mock(classOf[SecureRandomIDGenerator])
  val mockConfiguration: Configuration = Mockito.mock(classOf[Configuration])
  val mockSecretsManager: SecretsManager = Mockito.mock(classOf[SecretsManager])
  Mockito.when(mockConfiguration.getString("twilio.authTokenSecretName"))
    .thenReturn(Some("fakeSecretName"))
  Mockito.when(mockSecretsManager.getSecretUtf8("fakeSecretName")).thenReturn("12345")
  val requestValidator = new TwilioRequestValidatorImpl(mockConfiguration, mockSecretsManager)

  val userID: UUID = UUID.randomUUID()

  /**
   * An identity.
   */
  var identity = User(
    userID = userID,
    loginInfo = LoginInfo("credentials", "user@website.com"),
    firstName = None,
    lastName = None,
    fullName = None,
    email = None,
    avatarURL = None,
    activated = true,
    contact = None
  )

  var testClaim = Claim(
    userID = identity.userID,
    claimID = UUID.randomUUID(),
    key = "fakeKey",
    state = Claim.State.INCOMPLETE,
    stateUpdatedAt = java.util.Date.from(Instant.now()),
    recipients = Seq(
      Recipient(Recipient.Type.FAX, "18005555555"),
      Recipient(Recipient.Type.EMAIL, "test@x.com")
    )
  )

  var testForm = ClaimForm("VBA-21-0966-ARE", Map.empty[String, JsValue], identity.userID, testClaim.claimID, 0, 0, 0, 0)

  class FakeModule extends AbstractModule with ScalaModule {
    def configure(): Unit = {
      bind[FormDAO].toInstance(mockFormDao)
      bind[DocumentService].toInstance(mockDocumentService)
      bind[BasicAuthProvider].toInstance(mockBasicAuthProvider)
      bind[DelegableAuthInfoDAO[TwilioUser]].toInstance(mockTwilioUserDao)
      bind[PDFConcatenator].toInstance(mockPdfConcatenator)
      bind[SecureRandomIDGenerator].toInstance(mockSecureRandomIdGenerator)
      bind[TwilioRequestValidator].toInstance(requestValidator)
    }
  }

  val application: Application = GuiceApplicationBuilder()
    .configure(Configuration(ConfigFactory.load("application.test.conf")))
    .disable(classOf[JobModule])
    .overrides(new FakeModule)
    .build()
}
