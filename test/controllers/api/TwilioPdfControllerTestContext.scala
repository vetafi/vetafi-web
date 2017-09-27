package controllers.api

import com.google.inject.AbstractModule
import com.typesafe.config.ConfigFactory
import models.daos.FormDAO
import modules.JobModule
import net.codingwell.scalaguice.ScalaModule
import org.mockito.Mockito
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, Configuration}
import services.documents.DocumentService

class TwilioPdfControllerTestContext {

  val mockFormDao: FormDAO = Mockito.mock(classOf[FormDAO])
  val mockDocumentService: DocumentService = Mockito.mock(classOf[DocumentService])

  class FakeModule extends AbstractModule with ScalaModule {
    def configure(): Unit = {
      bind[FormDAO].toInstance(mockFormDao)
      bind[DocumentService].toInstance(mockDocumentService)
    }
  }

  lazy val application: Application = GuiceApplicationBuilder()
    .configure(Configuration(ConfigFactory.load("application.test.conf")))
    .disable(classOf[JobModule])
    .overrides(new FakeModule)
    .build()
}
