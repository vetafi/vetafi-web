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

object PDFMapping {
  private[this] val logger = getLogger

  def mapBase64ImageBlogValues(input: Map[String, String], spec: Seq[PDFFieldLocator]): Map[String, String] = {
    val imageLocators: Seq[PDFFieldLocator] = spec.filter(_.isBase64ImageBlob)
    val formElementSet: Set[String] = imageLocators.map(_.elementId).toSet
    val values: Map[String, String] = input.filter { case (k, _) => formElementSet.contains(k) }

    imageLocators.filter {
      locator => values.contains(locator.elementId)
    }.map(locator => (locator.pdfId, values(locator.elementId))).toMap
  }

  /**
   * Map all checkbox values to their PDF fields and checkbox state
   */
  def mapRadioValues(input: Map[String, String], spec: Seq[PDFFieldLocator]): Map[String, Boolean] = {

    val checkboxLocators: Map[String, PDFFieldLocator] = spec.filter(_.idMap.nonEmpty).map(
      locator =>
        (locator.elementId, locator)).toMap

    val filtered: Map[String, String] = input.filter {
      case (k, _) => checkboxLocators.contains(k)
    }

    filtered.map {
      case (k, v) if checkboxLocators.contains(k) && checkboxLocators(k).idMap.get.contains(v) =>
        (checkboxLocators(k).idMap.get(v), true)
      case (k, v) =>
        logger.error("Invalid checkbox value for " + k + " : " + v)
        throw new RuntimeException("Invalid checkbox value for " + k + " : " + v)
    }
  }

  def mapStringValues(input: Map[String, String], spec: Seq[PDFFieldLocator]): Map[String, String] = {
    val stringLocators: Map[String, PDFFieldLocator] = spec.filter(locator => locator.idMap.isEmpty && !locator.isBase64ImageBlob).map(
      locator =>
        (locator.elementId, locator)).toMap

    val filtered: Map[String, String] = input.filter {
      case (k, _) => stringLocators.contains(k)
    }

    val grouped: Map[String, Iterable[PDFFieldLocator]] = stringLocators.values.groupBy(_.pdfId)

    val concatenatedValues: Map[String, String] = grouped.map {
      case (k, v) =>
        (k, v.toList.sorted(ConcatOrdering))
    }.map {
      case (k, v) =>
        (k, v.map(locator => filtered.getOrElse(locator.elementId, "")).reduce(_ + " " + _))
    }

    val substringAppliedValues = grouped.filter {
      case (_, v) => v.size == 1
    }.map {
      case (k, v) => (k, v.head)
    }.filter {
      case (_, pdfFieldLocator) =>
        pdfFieldLocator.substringStart.nonEmpty && pdfFieldLocator.substringEnd.nonEmpty
    }.map {
      case (k, pdfFieldLocator) =>
        (k, concatenatedValues(k).substring(
          if (pdfFieldLocator.substringStart.get > concatenatedValues(k).length()) concatenatedValues(k).length() else pdfFieldLocator.substringStart.get,
          if (pdfFieldLocator.substringEnd.get > concatenatedValues(k).length()) concatenatedValues(k).length() else pdfFieldLocator.substringEnd.get
        ))
    }

    concatenatedValues ++ substringAppliedValues
  }

}
