package services.documents.pdf;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class PDFStampingTest {

  private static String SIGNATURE = null;

  static {
    try {
      SIGNATURE = IOUtils.toString(PDFStampingTest.class.getClassLoader().getResourceAsStream("test_image"));
    } catch (IOException e) {
      Throwables.propagate(e);
    }
  }

  @Rule
  public TemporaryFolder TMP = new TemporaryFolder();

  @Ignore
  @Test
  public void testStampPdfWithCheck() throws Exception {
    InputStream pdfTemplate =
        PDFStamping.class.getClassLoader().getResourceAsStream("forms/VBA-21-0966-ARE.pdf");
    File tmpFile = TMP.newFile("test.pdf");

    HashMap<String, String> idMap = Maps.newHashMap();
    idMap.put("Yes", "F[0].Page_1[0].Compensation[3]");
    idMap.put("No", "F[0].Page_1[0].Compensation[2]");
    PDFStamping.stampPdf(pdfTemplate,
        Lists.newArrayList(
            new PDFField("veteran_previous_claim_with_va_y_n", "Yes")
        ),
        Lists.newArrayList(
            new PDFFieldLocator(null, "veteran_previous_claim_with_va_y_n", 0, idMap, null, null, false)
        ),
        new FileOutputStream(tmpFile));
  }

  @Test
  public void testStampWithSignature() throws Exception {
    InputStream pdfTemplate =
        PDFStamping.class.getClassLoader().getResourceAsStream("forms/VBA-21-0966-ARE.pdf");
    File tmpFile = TMP.newFile("test.pdf");

    HashMap<String, String> idMap = Maps.newHashMap();
    idMap.put("Army", "ARMY[0]");
    PDFStamping.stampPdf(pdfTemplate,
        Lists.newArrayList(
            new PDFField("signature", SIGNATURE)
        ),
        Lists.newArrayList(
            new PDFFieldLocator("F[0].Page_1[0].SignatureField1[0]",
                "signature", 0, null, null, null, true)
        ),
        new FileOutputStream(tmpFile));
  }

  @Test
  public void testReadAcroForm() throws Exception {
    InputStream pdfTemplate =
        PDFStamping.class.getClassLoader().getResourceAsStream("forms/VBA-21-0966-ARE.pdf");
    PdfReader reader = new PdfReader(pdfTemplate);
    PdfStamper stamper;
    try {
      stamper = new PdfStamper(reader, new NullOutputStream());
    } catch (DocumentException e) {
      throw Throwables.propagate(e);
    }
    AcroFields form = stamper.getAcroFields();
    Map<String, AcroFields.Item> fields = form.getFields();
  }
}
