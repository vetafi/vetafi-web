package services.submission

import java.time.Instant
import java.util.UUID

import com.google.inject.AbstractModule
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{ PasswordHasher, PasswordHasherRegistry }
import com.mohiva.play.silhouette.impl.util.SecureRandomIDGenerator
import com.typesafe.config.ConfigFactory
import models._
import models.daos.{ ClaimDAO, TwilioFaxDAO }
import net.codingwell.scalaguice.ScalaModule
import org.mockito.Mockito
import org.specs2.specification.Scope
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsValue
import play.api.{ Application, Configuration }
import services.TwilioUserService
import services.time.ClockService
import utils.secrets.SecretsManager

trait TwilioFaxSubmissionServiceTestContext extends Scope {

  val mockSecretsManager: SecretsManager = Mockito.mock(classOf[SecretsManager])
  val mockTwilioUserService: TwilioUserService = Mockito.mock(classOf[TwilioUserService])
  val mockTwilioFaxDAO: TwilioFaxDAO = Mockito.mock(classOf[TwilioFaxDAO])
  val mockSecureRandomIDGenerator: SecureRandomIDGenerator = Mockito.mock(classOf[SecureRandomIDGenerator])
  val mockClaimDAO: ClaimDAO = Mockito.mock(classOf[ClaimDAO])
  val mockFaxApi: FaxApi = Mockito.mock(classOf[FaxApi])
  val mockClockService: ClockService = Mockito.mock(classOf[ClockService])
  val userID: UUID = UUID.randomUUID()
  val mockAuthInfoRepository: AuthInfoRepository = Mockito.mock(classOf[AuthInfoRepository])
  val mockPasswordHasher: PasswordHasher = Mockito.mock(classOf[PasswordHasher])
  val mockPasswordHasherRegistry: PasswordHasherRegistry = PasswordHasherRegistry(mockPasswordHasher, Seq())

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
    contact = None)

  var testClaim = Claim(
    userID = identity.userID,
    claimID = UUID.randomUUID(),
    key = "fakeKey",
    state = Claim.State.INCOMPLETE,
    stateUpdatedAt = java.util.Date.from(Instant.now()),
    recipients = Seq(
      Recipient(Recipient.Type.FAX, "18005555555"),
      Recipient(Recipient.Type.EMAIL, "test@x.com")))

  var testForm = ClaimForm("VBA-21-0966-ARE", Map.empty[String, JsValue], identity.userID, testClaim.claimID, 0, 0, 0, 0)

  class FakeModule extends AbstractModule with ScalaModule {
    def configure(): Unit = {
      bind[SecretsManager].toInstance(mockSecretsManager)
      bind[TwilioUserService].toInstance(mockTwilioUserService)
      bind[TwilioFaxDAO].toInstance(mockTwilioFaxDAO)
      bind[SecureRandomIDGenerator].toInstance(mockSecureRandomIDGenerator)
      bind[ClaimDAO].toInstance(mockClaimDAO)
      bind[FaxApi].toInstance(mockFaxApi)
      bind[ClockService].toInstance(mockClockService)
      bind[PasswordHasherRegistry].toInstance(mockPasswordHasherRegistry)
      bind[AuthInfoRepository].toInstance(mockAuthInfoRepository)
    }
  }

  val application: Application = GuiceApplicationBuilder()
    .configure(Configuration(ConfigFactory.load("application.test.conf")))
    .overrides(new FakeModule)
    .build()
}
