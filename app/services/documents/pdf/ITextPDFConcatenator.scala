package services.documents.pdf;

import java.io.{ByteArrayOutputStream, InputStream, OutputStream}

import com.itextpdf.text.Document
import com.itextpdf.text.pdf.{PdfCopy, PdfReader};

/**
 * Concatenation of multiple pdf documents
 */
class ITextPDFConcatenator extends PDFConcatenator {

  /**
   * Concatenate pdfs and write to output stream.
   * <p/>
   * Will insert a page break if needed between documents for double sided printing.
   */
  def concat(pdfs: Seq[Array[Byte]]): Array[Byte] = {
    val document = new Document()
    val outputStream = new ByteArrayOutputStream()
    val pdfCopy = new PdfCopy(document, outputStream)

    document.open()

    try {
      pdfs.foreach(
        bytes => {
          val reader = new PdfReader(bytes)

          (1 to reader.getNumberOfPages).foreach(
            i => pdfCopy.addPage(pdfCopy.getImportedPage(reader, i))
          )
        }
      )
    } finally {
      document.close()
    }

    outputStream.toByteArray
  }
}
