package services.documents.pdf;

import com.google.common.collect.Lists;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class PDFMappingTest {

    @Test
    public void testMapStringValuesWithSubstringAndConcat() throws Exception {
        Map<String, String> stringStringMap = PDFMapping.mapStringValues(
                Lists.newArrayList(
                        new PDFField("field1", "a"),
                        new PDFField("field2", "b"),
                        new PDFField("field3", "c"),
                        new PDFField("field4", "abc")
                ),
                Lists.newArrayList(
                        new PDFFieldLocator("pdfField", "field1", 0, null, null, null, false),
                        new PDFFieldLocator("pdfField", "field2", 1, null, null, null, false),
                        new PDFFieldLocator("pdfField", "field3", 2, null, null, null, false),
                        new PDFFieldLocator("pdfSubSField", "field4", 0, null, 0, 1, false)
                ));

        assertEquals("a b c", stringStringMap.get("pdfField"));
        assertEquals("a", stringStringMap.get("pdfSubSField"));
    }

    @Test
    public void testReadLocators() throws Exception {
        PDFMapping.getSpec(PDFMapping.class.getClassLoader().getResourceAsStream("forms/VBA-21-0966-ARE.locators.json"));
    }
}
