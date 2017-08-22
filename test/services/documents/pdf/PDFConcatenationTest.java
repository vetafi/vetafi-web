package services.documents.pdf;

import com.google.common.collect.Lists;
import com.itextpdf.text.pdf.PdfReader;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

public class PDFConcatenationTest {

    @Test
    public void testConcatForDoubleSidedPrintingHasCorrectNumberOfPages() throws Exception {
        InputStream pdf1 =
                PDFConcatenation.class.getClassLoader().getResourceAsStream("forms/VBA-21-0966-ARE.pdf");
        InputStream pdf2 =
                PDFConcatenation.class.getClassLoader().getResourceAsStream("forms/VBA-21-526EZ-ARE.pdf");

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        new PDFConcatenation(Lists.newArrayList(pdf1, pdf2)).concat(byteArrayOutputStream);

        PdfReader concatReader = new PdfReader(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));

        PdfReader pdf1Reader =
                new PdfReader(PDFConcatenation.class.getClassLoader().getResourceAsStream("forms/VBA-21-0966-ARE.pdf"));
        PdfReader pdf2Reader =
                new PdfReader(PDFConcatenation.class.getClassLoader().getResourceAsStream("forms/VBA-21-526EZ-ARE.pdf"));

        assertEquals(pdf1Reader.getNumberOfPages() + pdf2Reader.getNumberOfPages(), concatReader.getNumberOfPages());
    }
}
