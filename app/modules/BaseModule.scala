package modules

import java.time.Clock

import com.google.inject.AbstractModule
import models.daos._
import net.codingwell.scalaguice.ScalaModule
import play.modules.reactivemongo.ReactiveMongoApi
import services._
import services.documents.pdf._
import services.documents.{ DocumentService, ITextDocumentService }
import services.forms._
import services.submission._
import services.time.{ ClockService, SystemClockService }
import utils.seamlessdocs.RequestUtils
import utils.secrets.{ BiscuitSecretsManager, SecretsManager }

/**
 * The base Guice module, manages Dependency Injection for interfaces defined by our project.
 *
 * Do not add bindings for library interfaces, i.e. Silhouette
 */
class BaseModule extends AbstractModule with ScalaModule {

  /**
   * Configures the module.
   */
  def configure(): Unit = {
    bind[AuthTokenDAO].to[AuthTokenDAOImpl]
    bind[AuthTokenService].to[AuthTokenServiceImpl]
    bind[UserDAO].to[UserDAOImpl]
    bind[UserValuesDAO].to[UserValuesDAOImpl]
    bind[ClaimDAO].to[ClaimDAOImpl]
    bind[FormDAO].to[FormDAOImpl]
    bind[MailingListDAO].to[MailingListDAOImpl]
    bind[SecretsManager].to[BiscuitSecretsManager]
    bind[FormConfigManager].to[JsonResourceFormConfigManager]
    bind[ContactInfoService].to[ContactInfoServiceImpl]
    bind[ClaimService].to[ClaimServiceImpl]
    bind[FaxSubmissionService].to[TwilioFaxSubmissionService]
    bind[DocumentService].to[ITextDocumentService]
    bind[PDFStampingConfigProvider].to[ResourcesPDFStampingConfigProvider]
    bind[PDFTemplateProvider].to[ResourcesPDFTemplateProvider]
    bind[ReactiveMongoApi].to[BiscuitPasswordMongoApi]
    bind[UserValuesService].to[UserValuesServiceImpl]
    bind[RequestUtils].toInstance(new RequestUtils(Clock.systemUTC()))
    bind[EmailSubmissionService].to[SESEmailSubmissionService]
    bind[RecipientService].to[RecipientServiceImpl]
    bind[PDFConcatenator].to[ITextPDFConcatenator]
    bind[FaxApi].to[TwilioFaxApi]
    bind[ClockService].to[SystemClockService]
  }
}
