package services.documents.pdf

import java.io.{ InputStream, OutputStream }

trait PDFConcatenator {

  def concat(pdfs: Seq[Array[Byte]]): Array[Byte]
}
