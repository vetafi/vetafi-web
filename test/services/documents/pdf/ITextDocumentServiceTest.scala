package services.documents.pdf

import java.util.UUID

import com.typesafe.config.ConfigFactory
import models.ClaimForm
import org.mockito.{Matchers, Mockito}
import play.api.Configuration
import play.api.libs.json.{JsString, Json}
import play.api.test.PlaySpecification
import services.documents.ITextDocumentService

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class ITextDocumentServiceTest extends PlaySpecification {

  "the render" should {
    "create an image" in {
      val pdfStampingConfigProvider: PDFStampingConfigProvider =
        Mockito.mock(classOf[PDFStampingConfigProvider])
      val pdfTemplateProvider: PDFTemplateProvider =
        Mockito.mock(classOf[PDFTemplateProvider])

      Mockito.when(pdfTemplateProvider.getTemplate(Matchers.any()))
        .thenReturn(getClass.getClassLoader.getResourceAsStream(
          "forms/pdf_templates/VBA-21-0966-ARE.pdf"))

      val result = Json.parse(
        getClass.getClassLoader.getResourceAsStream(
          "forms/pdf_template_configs/VBA-21-0966-ARE.locators.json")).validate[Seq[PDFFieldLocator]].get

      Mockito.when(pdfStampingConfigProvider.getPDFFieldLocators(Matchers.any()))
        .thenReturn(result)

      val documentService = new ITextDocumentService(
        pdfStampingConfigProvider,
        pdfTemplateProvider,
        Configuration(ConfigFactory.load("application.test.conf")))

      val renderFuture: Future[Array[Byte]] = documentService.renderPage(
        ClaimForm(
          "VBA-21-0966-ARE",
          Map("veteran_last_name" -> JsString("Hello")),
          UUID.randomUUID(),
          UUID.randomUUID(),
          0, 0, 0, 0), 0)

      val rendered: Array[Byte] = Await.result(renderFuture, Duration.Inf)

      rendered.length must be greaterThan 0
    }
  }
}
