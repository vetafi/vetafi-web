package modules

import _root_.services.{ TwilioUserService, TwilioUserServiceImpl, UserService, UserServiceImpl }
import com.google.inject.name.Named
import com.google.inject.{ AbstractModule, Provides }
import com.mohiva.play.silhouette.api.crypto._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services._
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.api.{ Environment, EventBus, Silhouette, SilhouetteProvider }
import com.mohiva.play.silhouette.crypto.{ JcaCrypter, JcaCrypterSettings, JcaSigner, JcaSignerSettings }
import com.mohiva.play.silhouette.impl.authenticators._
import com.mohiva.play.silhouette.impl.providers._
import com.mohiva.play.silhouette.impl.providers.oauth1.secrets.{ CookieSecretProvider, CookieSecretSettings }
import com.mohiva.play.silhouette.impl.providers.state.{ CsrfStateItemHandler, CsrfStateSettings }
import com.mohiva.play.silhouette.impl.services._
import com.mohiva.play.silhouette.impl.util._
import com.mohiva.play.silhouette.password.BCryptPasswordHasher
import com.mohiva.play.silhouette.persistence.daos.{ DelegableAuthInfoDAO, MongoAuthInfoDAO }
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import models.TwilioUser
import models.daos._
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc.CookieHeaderEncoding
import play.modules.reactivemongo.ReactiveMongoApi
import utils.auth._

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * The Guice module which wires all Silhouette dependencies.
 */
class SilhouetteModule extends AbstractModule with ScalaModule {

  /**
   * Configures the module.
   */
  def configure() {
    bind[Silhouette[DefaultEnv]].to[SilhouetteProvider[DefaultEnv]]
    bind[Silhouette[TwilioAuthEnv]].to[SilhouetteProvider[TwilioAuthEnv]]
    bind[UserService].to[UserServiceImpl]
    bind[TwilioUserService].to[TwilioUserServiceImpl]
    bind[UserDAO].to[UserDAOImpl]
    bind[IDGenerator].toInstance(new SecureRandomIDGenerator())
    bind[PasswordHasher].toInstance(new BCryptPasswordHasher)
    bind[FingerprintGenerator].toInstance(new DefaultFingerprintGenerator(false))
    bind[EventBus].toInstance(EventBus())
    bind[Clock].toInstance(Clock())
  }

  /**
   * Provides the HTTP layer implementation.
   *
   * @param client Play's WS client.
   * @return The HTTP layer implementation.
   */
  @Provides
  def provideHTTPLayer(client: WSClient): HTTPLayer = new PlayHTTPLayer(client)

  /**
   * Provides the Silhouette environment.
   *
   * @param userService          The user service implementation.
   * @param authenticatorService The authentication service implementation.
   * @param eventBus             The event bus instance.
   * @return The Silhouette environment.
   */
  @Provides
  def provideEnvironment(
    userService: UserService,
    authenticatorService: AuthenticatorService[CookieAuthenticator],
    eventBus: EventBus): Environment[DefaultEnv] = {

    Environment[DefaultEnv](
      userService,
      authenticatorService,
      Seq(),
      eventBus)
  }

  @Provides
  def provideEnvironment(
    userService: TwilioUserService,
    authenticatorService: DummyAuthenticatorService,
    eventBus: EventBus,
    authInfoRepository: AuthInfoRepository,
    passwordHasherRegistry: PasswordHasherRegistry,
    basicAuthProvider: BasicAuthProvider): Environment[TwilioAuthEnv] = {
    Environment[TwilioAuthEnv](
      userService,
      authenticatorService,
      Seq(basicAuthProvider),
      eventBus)
  }

  /**
   * Provides the social provider registry.
   *
   * @return The Silhouette environment.
   */
  @Provides
  def provideSocialProviderRegistry(
    idMeProvider: IdMeProvider): SocialProviderRegistry = {

    SocialProviderRegistry(Seq(idMeProvider))
  }

  /**
   * Provides the signer for the OAuth1 token secret provider.
   *
   * @param configuration The Play configuration.
   * @return The signer for the OAuth1 token secret provider.
   */
  @Provides
  @Named("oauth1-token-secret-signer")
  def provideOAuth1TokenSecretSigner(configuration: Configuration): Signer = {
    val config = configuration.underlying.as[JcaSignerSettings]("silhouette.oauth1TokenSecretProvider.signer")

    new JcaSigner(config)
  }

  /**
   * Provides the crypter for the OAuth1 token secret provider.
   *
   * @param configuration The Play configuration.
   * @return The crypter for the OAuth1 token secret provider.
   */
  @Provides
  @Named("oauth1-token-secret-crypter")
  def provideOAuth1TokenSecretCrypter(configuration: Configuration): Crypter = {
    val config = configuration.underlying.as[JcaCrypterSettings]("silhouette.oauth1TokenSecretProvider.crypter")

    new JcaCrypter(config)
  }

  /**
   * Provides the signer for the CSRF state item handler.
   *
   * @param configuration The Play configuration.
   * @return The signer for the CSRF state item handler.
   */
  @Provides
  @Named("csrf-state-item-signer")
  def provideCSRFStateItemSigner(configuration: Configuration): Signer = {
    val config = configuration.underlying.as[JcaSignerSettings]("silhouette.csrfStateItemHandler.signer")

    new JcaSigner(config)
  }

  /**
   * Provides the social state handler.
   *
   * @param signer The signer implementation.
   * @return The social state handler implementation.
   */
  @Provides
  def provideSocialStateHandler(
    @Named("social-state-signer") signer: Signer,
    csrfStateItemHandler: CsrfStateItemHandler): SocialStateHandler = {

    new DefaultSocialStateHandler(Set(csrfStateItemHandler), signer)
  }

  /**
   * Provides the signer for the social state handler.
   *
   * @param configuration The Play configuration.
   * @return The signer for the social state handler.
   */
  @Provides
  @Named("social-state-signer")
  def provideSocialStateSigner(configuration: Configuration): Signer = {
    val config = configuration.underlying.as[JcaSignerSettings]("silhouette.socialStateHandler.signer")

    new JcaSigner(config)
  }

  /**
   * Provides the signer for the authenticator.
   *
   * @param configuration The Play configuration.
   * @return The signer for the authenticator.
   */
  @Provides
  @Named("authenticator-signer")
  def provideAuthenticatorSigner(configuration: Configuration): Signer = {
    val config = configuration.underlying.as[JcaSignerSettings]("silhouette.authenticator.signer")

    new JcaSigner(config)
  }

  /**
   * Provides the crypter for the authenticator.
   *
   * @param configuration The Play configuration.
   * @return The crypter for the authenticator.
   */
  @Provides
  @Named("authenticator-crypter")
  def provideAuthenticatorCrypter(configuration: Configuration): Crypter = {
    val config = configuration.underlying.as[JcaCrypterSettings]("silhouette.authenticator.crypter")

    new JcaCrypter(config)
  }

  /**
   * Provides the implementation of the delegable OAuth1 auth info DAO.
   *
   * @param reactiveMongoApi The ReactiveMongo API.
   * @param config           The Play configuration.
   * @return The implementation of the delegable OAuth1 auth info DAO.
   */
  @Provides
  def provideOAuth1InfoDAO(reactiveMongoApi: ReactiveMongoApi, config: Configuration): DelegableAuthInfoDAO[OAuth1Info] = {
    implicit lazy val format = Json.format[OAuth1Info]
    new MongoAuthInfoDAO[OAuth1Info](reactiveMongoApi, config)
  }

  /**
   * Provides the implementation of the delegable OAuth2 auth info DAO.
   *
   * @param reactiveMongoApi The ReactiveMongo API.
   * @param config           The Play configuration.
   * @return The implementation of the delegable OAuth2 auth info DAO.
   */
  @Provides
  def provideOAuth2InfoDAO(reactiveMongoApi: ReactiveMongoApi, config: Configuration): DelegableAuthInfoDAO[OAuth2Info] = {
    implicit lazy val format = Json.format[OAuth2Info]
    new MongoAuthInfoDAO[OAuth2Info](reactiveMongoApi, config)
  }

  /**
   * Provides the implementation of the delegable OpenID auth info DAO.
   *
   * @param reactiveMongoApi The ReactiveMongo API.
   * @param config           The Play configuration.
   * @return The implementation of the delegable OpenID auth info DAO.
   */
  @Provides
  def provideOpenIDInfoDAO(reactiveMongoApi: ReactiveMongoApi, config: Configuration): DelegableAuthInfoDAO[OpenIDInfo] = {
    implicit lazy val format = Json.format[OpenIDInfo]
    new MongoAuthInfoDAO[OpenIDInfo](reactiveMongoApi, config)
  }

  /**
   * Provides the implementation of the delegable password auth info DAO.
   *
   * @param reactiveMongoApi The ReactiveMongo API.
   * @param config           The Play configuration.
   * @return The implementation of the delegable password auth info DAO.
   */
  @Provides
  def providePasswordInfoDAO(reactiveMongoApi: ReactiveMongoApi, config: Configuration): DelegableAuthInfoDAO[PasswordInfo] = {
    implicit lazy val format = Json.format[PasswordInfo]
    new MongoAuthInfoDAO[PasswordInfo](reactiveMongoApi, config)
  }

  @Provides
  def provideTwilioUserDAO(reactiveMongoApi: ReactiveMongoApi, config: Configuration): DelegableAuthInfoDAO[TwilioUser] = {
    implicit lazy val format = Json.format[TwilioUser]
    new MongoAuthInfoDAO[TwilioUser](reactiveMongoApi, config)
  }

  /**
   * Provides the auth info repository.
   *
   * @param passwordInfoDAO The implementation of the delegable password auth info DAO.
   * @param oauth1InfoDAO   The implementation of the delegable OAuth1 auth info DAO.
   * @param oauth2InfoDAO   The implementation of the delegable OAuth2 auth info DAO.
   * @param openIDInfoDAO   The implementation of the delegable OpenID auth info DAO.
   * @return The auth info repository instance.
   */
  @Provides
  def provideAuthInfoRepository(
    passwordInfoDAO: DelegableAuthInfoDAO[PasswordInfo],
    oauth1InfoDAO: DelegableAuthInfoDAO[OAuth1Info],
    oauth2InfoDAO: DelegableAuthInfoDAO[OAuth2Info],
    openIDInfoDAO: DelegableAuthInfoDAO[OpenIDInfo],
    twilioUserDAO: DelegableAuthInfoDAO[TwilioUser]): AuthInfoRepository = {

    new DelegableAuthInfoRepository(passwordInfoDAO, oauth1InfoDAO, oauth2InfoDAO, openIDInfoDAO, twilioUserDAO)
  }

  /**
   * Provides the authenticator service.
   *
   * @param signer               The signer implementation.
   * @param crypter              The crypter implementation.
   * @param cookieHeaderEncoding Logic for encoding and decoding `Cookie` and `Set-Cookie` headers.
   * @param fingerprintGenerator The fingerprint generator implementation.
   * @param idGenerator          The ID generator implementation.
   * @param configuration        The Play configuration.
   * @param clock                The clock instance.
   * @return The authenticator service.
   */
  @Provides
  def provideAuthenticatorService(
    @Named("authenticator-signer") signer: Signer,
    @Named("authenticator-crypter") crypter: Crypter,
    cookieHeaderEncoding: CookieHeaderEncoding,
    fingerprintGenerator: FingerprintGenerator,
    idGenerator: IDGenerator,
    configuration: Configuration,
    clock: Clock): AuthenticatorService[CookieAuthenticator] = {

    val config = configuration.underlying.as[CookieAuthenticatorSettings]("silhouette.authenticator")
    val authenticatorEncoder = new CrypterAuthenticatorEncoder(crypter)

    new CookieAuthenticatorService(config, None, signer, cookieHeaderEncoding, authenticatorEncoder, fingerprintGenerator, idGenerator, clock)
  }

  @Provides
  def provideAuthenticatorService(): DummyAuthenticatorService = {
    new DummyAuthenticatorService()
  }

  /**
   * Provides the avatar service.
   *
   * @param httpLayer The HTTP layer implementation.
   * @return The avatar service implementation.
   */
  @Provides
  def provideAvatarService(httpLayer: HTTPLayer): AvatarService = new GravatarService(httpLayer)

  /**
   * Provides the OAuth1 token secret provider.
   *
   * @param signer        The signer implementation.
   * @param crypter       The crypter implementation.
   * @param configuration The Play configuration.
   * @param clock         The clock instance.
   * @return The OAuth1 token secret provider implementation.
   */
  @Provides
  def provideOAuth1TokenSecretProvider(
    @Named("oauth1-token-secret-signer") signer: Signer,
    @Named("oauth1-token-secret-crypter") crypter: Crypter,
    configuration: Configuration,
    clock: Clock): OAuth1TokenSecretProvider = {

    val settings = configuration.underlying.as[CookieSecretSettings]("silhouette.oauth1TokenSecretProvider")
    new CookieSecretProvider(settings, signer, crypter, clock)
  }

  /**
   * Provides the CSRF state item handler.
   *
   * @param idGenerator   The ID generator implementation.
   * @param signer        The signer implementation.
   * @param configuration The Play configuration.
   * @return The CSRF state item implementation.
   */
  @Provides
  def provideCsrfStateItemHandler(
    idGenerator: IDGenerator,
    @Named("csrf-state-item-signer") signer: Signer,
    configuration: Configuration): CsrfStateItemHandler = {
    val settings = configuration.underlying.as[CsrfStateSettings]("silhouette.csrfStateItemHandler")
    new CsrfStateItemHandler(settings, idGenerator, signer)
  }

  /**
   * Provides the password hasher registry.
   *
   * @param passwordHasher The default password hasher implementation.
   * @return The password hasher registry.
   */
  @Provides
  def providePasswordHasherRegistry(passwordHasher: PasswordHasher): PasswordHasherRegistry = {
    new PasswordHasherRegistry(passwordHasher)
  }

  /**
   * Provides the credentials provider.
   *
   * @param authInfoRepository     The auth info repository implementation.
   * @param passwordHasherRegistry The password hasher registry.
   * @return The credentials provider.
   */
  @Provides
  def provideCredentialsProvider(
    authInfoRepository: AuthInfoRepository,
    passwordHasherRegistry: PasswordHasherRegistry): CredentialsProvider = {

    new CredentialsProvider(authInfoRepository, passwordHasherRegistry)
  }

  @Provides
  def provideBasicDigestAuthProvider(
    authInfoRepository: AuthInfoRepository,
    passwordHasherRegistry: PasswordHasherRegistry): BasicAuthProvider = {
    new BasicAuthProvider(authInfoRepository, passwordHasherRegistry)
  }

  /**
   * Provides the Facebook provider.
   *
   * @param httpLayer          The HTTP layer implementation.
   * @param socialStateHandler The OAuth2 state provider implementation.
   * @param configuration      The Play configuration.
   * @return The Facebook provider.
   */
  @Provides
  def provideIdMeProvider(
    httpLayer: HTTPLayer,
    socialStateHandler: SocialStateHandler,
    configuration: Configuration): IdMeProvider = {

    new IdMeProvider(
      httpLayer,
      socialStateHandler,
      configuration.underlying.as[OAuth2Settings]("silhouette.idme"))
  }
}
