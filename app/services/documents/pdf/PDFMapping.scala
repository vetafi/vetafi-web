package services.documents.pdf

import java.util.stream.Stream
import javax.annotation.Nullable

import com.google.common.base.{Function, Joiner}
import org.log4s.getLogger

object ConcatOrdering extends Ordering[PDFFieldLocator] {

  override def compare(left: PDFFieldLocator, right: PDFFieldLocator): Int = {
    left.concatOrder.compareTo(right.concatOrder)
  }
}

class PDFMapping {
  private[this] val logger = getLogger

  def mapBase64ImageBlogValues(input: Map[String, String], spec: Seq[PDFFieldLocator]): Map[String, String] = {
    val imageLocators = spec.filter(_.isBase64ImageBlog)
    val formElementSet = imageLocators.map(_.elementId).toSet
    val values = input.filter { case (k, _) => formElementSet.contains(k) }.toMap

    imageLocators.filter { locator => values.contains(locator.elementId) }.map(locator => (locator.pdfId, values(locator.elementId)))
  }

  /**
   * Map all checkbox values to their PDF fields and checkbox state
   */
  public static Map < String
  , Boolean > mapCheckboxValues(input: Seq[PDFField] ,
  final List < PDFFieldLocator > spec
  )
  {
    HashMap < String
    , Boolean > output = Maps.newHashMap();

    List < PDFFieldLocator > checkboxLocators =
      Lists.newArrayList(spec.stream().filter(locator -> (locator.hasIdMap())).iterator());

    Set < String > formElementSet = Sets.newHashSet(
      checkboxLocators.stream().map(locator -> (locator.elementId)).iterator());

    Stream < PDFField > filtered = input.stream().filter(pdfField -> (formElementSet.contains(pdfField.fieldName)));

    HashMap < String
    , String > formValueMap = Maps.newHashMap();

    filtered.forEach(pdfField -> {
      formValueMap.put(pdfField.getFieldName(), pdfField.getFieldValue());
    });

    for (PDFFieldLocator locator
    : checkboxLocators
    )
    {
      String pdfFieldId = null;
      for (Map.Entry < String
      , String > entry: locator.idMap.entrySet
      ()
      )
      {
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
  public static Map < String
  , String > mapStringValues(List < PDFField > input,
  final List < PDFFieldLocator > spec
  )
  {
    HashMap < String
    , String > output = Maps.newHashMap();

    List < PDFFieldLocator > nonCheckboxLocators =
      Lists.newArrayList(spec.stream().filter(locator -> (!locator.hasIdMap() && !locator.isBase64ImageBlob)).iterator());

    Set < String > formElementSet = Sets.newHashSet(
      nonCheckboxLocators.stream().map(locator -> (locator.elementId)).iterator());

    Stream < PDFField > filtered = input.stream().filter(pdfField -> (formElementSet.contains(pdfField.fieldName)));

    HashMap < String
    , String > formValueMap = Maps.newHashMap();

    filtered.forEach(pdfField -> {
      formValueMap.put(pdfField.getFieldName(), pdfField.getFieldValue());
    });

    ImmutableListMultimap < String
    , PDFFieldLocator > grouped = Multimaps.index(nonCheckboxLocators,
    new Function < PDFFieldLocator, String > () {
      @Nullable
      @Override
      public String apply(PDFFieldLocator locator) {
        return locator.pdfId;
      }
    });

    for (String key
    : grouped.keys()
    )
    {
      ImmutableList < PDFFieldLocator > pdfFieldLocators = grouped.get(key);
      List < PDFFieldLocator > pdfFieldLocatorsSorted = CONCAT_ORDERING.sortedCopy(pdfFieldLocators);

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
            pdfFieldLocator.substringStart > valueString.length() ? valueString.length(): pdfFieldLocator.substringStart,
            pdfFieldLocator.substringEnd > valueString.length() ? valueString.length(): pdfFieldLocator.substringEnd);
        }
      }

      output.put(key, valueString);
    }

    return output;
  }
}
