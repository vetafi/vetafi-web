package services.documents.pdf;

import java.io.{ InputStream, OutputStream }

import com.itextpdf.text.Document
import com.itextpdf.text.pdf.{ PdfCopy, PdfReader };

/**
 * Concatenation of multiple pdf documents
 */
object PDFConcatenation {

  /**
   * Concatenate pdfs and write to output stream.
   * <p/>
   * Will insert a page break if needed between documents for double sided printing.
   */
  def concat(pdfs: Seq[InputStream], outputStream: OutputStream): Unit = {
    val document = new Document()
    val pdfCopy = new PdfCopy(document, outputStream)

    document.open()

    try {
      pdfs.foreach(
        inputStream => {
          val reader = new PdfReader(inputStream)

          (1 to reader.getNumberOfPages).foreach(
            i => pdfCopy.addPage(pdfCopy.getImportedPage(reader, i))
          )
        }
      )
    } finally {
      document.close()
    }
  }
}
