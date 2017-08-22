package services.documents.pdf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.documents.pdf.PDFField;
import services.documents.pdf.PDFFieldLocator;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class PDFMapping {
    private static final Logger logger = LoggerFactory.getLogger(PDFMapping.class);

    public static List<PDFFieldLocator> getSpec(InputStream inputStream) throws IOException {
        logger.info("Reading " + inputStream);
        ObjectMapper mapper = new ObjectMapper();

        return mapper.readValue(inputStream,
                mapper.getTypeFactory().constructCollectionType(List.class, PDFFieldLocator.class));
    }

    private static final Ordering<PDFFieldLocator> CONCAT_ORDERING = new Ordering<PDFFieldLocator>() {
        @Override
        public int compare(@Nullable PDFFieldLocator left, @Nullable PDFFieldLocator right) {
            Preconditions.checkNotNull(left);
            Preconditions.checkNotNull(right);
            return left.concatOrder.compareTo(right.concatOrder);
        }
    };

    public static Map<String, String> mapBase64ImageBlogValues(List<PDFField> input, final List<PDFFieldLocator> spec) {
        HashMap<String, String> output = Maps.newHashMap();
        HashMap<String, String> values = Maps.newHashMap();
        List<PDFFieldLocator> imageLocators =
                Lists.newArrayList(spec.stream().filter(locator -> (locator.isBase64ImageBlob)).iterator());

        Set<String> formElementSet = Sets.newHashSet(
                imageLocators.stream().map(locator -> (locator.elementId)).iterator());

        Stream<PDFField> filtered = input.stream().filter(pdfField -> (formElementSet.contains(pdfField.fieldName)));

        filtered.forEach(pdfField -> {

            values.put(pdfField.getFieldName(), pdfField.getFieldValue());
        });

        imageLocators.forEach(locator -> {
            if (values.containsKey(locator.elementId)) {
                output.put(locator.pdfId, values.get(locator.elementId));
            }
        });

        return output;
    }

    /**
     * Map all checkbox values to their PDF fields and checkbox state
     */
    public static Map<String, Boolean> mapCheckboxValues(List<PDFField> input, final List<PDFFieldLocator> spec) {
        HashMap<String, Boolean> output = Maps.newHashMap();

        List<PDFFieldLocator> checkboxLocators =
                Lists.newArrayList(spec.stream().filter(locator -> (locator.hasIdMap())).iterator());

        Set<String> formElementSet = Sets.newHashSet(
                checkboxLocators.stream().map(locator -> (locator.elementId)).iterator());

        Stream<PDFField> filtered = input.stream().filter(pdfField -> (formElementSet.contains(pdfField.fieldName)));

        HashMap<String, String> formValueMap = Maps.newHashMap();

        filtered.forEach(pdfField -> {
            formValueMap.put(pdfField.getFieldName(), pdfField.getFieldValue());
        });

        for (PDFFieldLocator locator : checkboxLocators) {
            String pdfFieldId = null;
            for (Map.Entry<String, String> entry : locator.idMap.entrySet()) {
                if (formValueMap.containsKey(locator.elementId) &&
                        formValueMap.get(locator.elementId).equals(entry.getKey())) {
                    pdfFieldId = entry.getValue();
                }
            }
            if (pdfFieldId == null) {
                logger.warn("Invalid checkbox value for " + locator + " : " + formValueMap.get(locator.elementId));
                continue;
            }
            output.put(pdfFieldId, true);
        }

        return output;
    }

    /**
     * Map all string (non checkbox values) from input to their PDF fields
     *
     * Perform all concatenation and substring operations.
     */
    public static Map<String, String> mapStringValues(List<PDFField> input, final List<PDFFieldLocator> spec) {
        HashMap<String, String> output = Maps.newHashMap();

        List<PDFFieldLocator> nonCheckboxLocators =
                Lists.newArrayList(spec.stream().filter(locator -> (!locator.hasIdMap() && !locator.isBase64ImageBlob)).iterator());

        Set<String> formElementSet = Sets.newHashSet(
                nonCheckboxLocators.stream().map(locator -> (locator.elementId)).iterator());

        Stream<PDFField> filtered = input.stream().filter(pdfField -> (formElementSet.contains(pdfField.fieldName)));

        HashMap<String, String> formValueMap = Maps.newHashMap();

        filtered.forEach(pdfField -> {
            formValueMap.put(pdfField.getFieldName(), pdfField.getFieldValue());
        });

        ImmutableListMultimap<String, PDFFieldLocator> grouped = Multimaps.index(nonCheckboxLocators,
                new Function<PDFFieldLocator, String>() {
                    @Nullable
                    @Override
                    public String apply(PDFFieldLocator locator) {
                        return locator.pdfId;
                    }
                });

        for (String key : grouped.keys()) {
            ImmutableList<PDFFieldLocator> pdfFieldLocators = grouped.get(key);
            List<PDFFieldLocator> pdfFieldLocatorsSorted = CONCAT_ORDERING.sortedCopy(pdfFieldLocators);

            String valueString = Joiner.on(" ").join(
                    pdfFieldLocatorsSorted.stream().map(
                            pdfFieldLocator -> {
                                if (formValueMap.containsKey(pdfFieldLocator.elementId)) {
                                    return formValueMap.get(pdfFieldLocator.elementId);
                                } else {
                                    return "";
                                }
                            }).iterator());

            if (pdfFieldLocators.size() == 1) {
                PDFFieldLocator pdfFieldLocator = Iterables.getOnlyElement(pdfFieldLocators);
                if (pdfFieldLocator.substringStart != null && pdfFieldLocator.substringEnd != null) {
                    valueString = valueString.substring(
                            pdfFieldLocator.substringStart > valueString.length() ? valueString.length() : pdfFieldLocator.substringStart,
                            pdfFieldLocator.substringEnd > valueString.length() ? valueString.length() : pdfFieldLocator.substringEnd);
                }
            }

            output.put(key, valueString);
        }

        return output;
    }
}
