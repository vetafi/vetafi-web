package controllers

import play.api.mvc.{ Action, AnyContent, Controller }

class PdfViewerController extends Controller {

  def view(url: String): Action[AnyContent] = Action {
    Ok(views.html.pdfViewer(url))
  }
}
