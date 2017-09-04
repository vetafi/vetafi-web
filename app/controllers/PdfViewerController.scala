package controllers

import java.util.UUID
import javax.inject.Inject

import models.daos.FormDAO
import play.api.mvc.{Action, AnyContent, Controller}
import services.documents.DocumentService

class PdfViewerController extends Controller {

  def view(url: String): Action[AnyContent] = Action {
    Ok(views.html.pdfViewer(url))
  }


}
