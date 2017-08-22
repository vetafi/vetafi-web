package services.documents.pdf;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Concatenation of multiple pdf documents
 */
public class PDFConcatenation {

    private final List<InputStream> pdfs;

    public PDFConcatenation(List<InputStream> pdfs) {
        this.pdfs = pdfs;
    }

    /**
     * Concatenate pdfs and write to output stream.
     * <p/>
     * Will insert a page break if needed between documents for double sided printing.
     */
    public void concat(OutputStream outputStream) throws DocumentException, IOException {
        Document document = new Document();
        PdfCopy pdfCopy = new PdfCopy(document, outputStream);

        document.open();

        try {
            for (InputStream inputStream : pdfs) {
                PdfReader reader;
                reader = new PdfReader(inputStream);

                int numberOfPages = reader.getNumberOfPages();

                for (int i = 0; i < numberOfPages; i++) {
                    pdfCopy.addPage(pdfCopy.getImportedPage(reader, i + 1));
                }
            }
        } finally {
            document.close();
        }
    }
}
