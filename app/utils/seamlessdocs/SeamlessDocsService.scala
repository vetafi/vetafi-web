package utils.seamlessdocs

import java.net.URL

import play.api.libs.json.JsValue

import scala.concurrent.Future

trait SeamlessDocsService {

  def formPrepare(
    formId: String,
    name: String,
    email: String,
    signerId: String,
    data: Map[String, JsValue]): Future[Either[SeamlessApplicationCreateResponse, SeamlessErrorResponse]]

  def formSubmit(formId: String, data: Map[String, JsValue]): Future[Either[SeamlessApplicationCreateResponse, SeamlessErrorResponse]]

  def getInviteUrl(applicationId: String): Future[URL]

  def getApplication(applicationId: String): Future[SeamlessApplication]

  def getApplicationStatus(applicationId: String): Future[SeamlessApplicationStatus]

  def updatePdf(applicationId: String): Future[Either[URL, SeamlessErrorResponse]]

  def getForms: Future[JsValue]

  def getFormElements(formId: String): Future[JsValue]

  def getFormProperties(formId: String): Future[JsValue]

  def getFormSigners(formId: String): Future[Seq[SeamlessSigner]]
}
