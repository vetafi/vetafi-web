package services.documents.pdf

import org.log4s.getLogger
import play.api.libs.json.{ JsBoolean, JsString, JsValue }

object ConcatOrdering extends Ordering[PDFFieldLocator] {

  override def compare(left: PDFFieldLocator, right: PDFFieldLocator): Int = {
    val rightOrder: Int = right.concatOrder.getOrElse(0)
    val leftOrder: Int = left.concatOrder.getOrElse(0)
    leftOrder.compareTo(rightOrder)
  }
}

object PDFMapping {
  private[this] val logger = getLogger

  def mapBase64ImageBlogValues(input: Map[String, JsValue], spec: Seq[PDFFieldLocator]): Map[String, String] = {
    val stringInputs = input.filter {
      case (_, _: JsString) => true
      case _ => false
    }.map {
      case (k, v: JsString) => (k, v.value)
    }

    val imageLocators: Seq[PDFFieldLocator] = spec.filter {
      _.isBase64ImageBlob match {
        case None => false
        case Some(bool) => bool
      }
    }
    val formElementSet: Set[String] = imageLocators.map(_.elementId).toSet
    val values: Map[String, String] = stringInputs.filter { case (k, _) => formElementSet.contains(k) }

    imageLocators.filter {
      locator => values.contains(locator.elementId)
    }.map(locator => (locator.pdfId.get, values(locator.elementId))).toMap
  }

  def mapCheckBoxValues(input: Map[String, JsValue], spec: Seq[PDFFieldLocator]): Map[String, Boolean] = {
    val checkboxResponses: Map[String, Boolean] = input.filter {
      case (_, _: JsBoolean) => true
      case _ => false
    }.map {
      case (k, v) => (k, v.as[JsBoolean].value)
    }

    logger.info("Got checkbox responses:" + checkboxResponses.toString())

    spec.filter(
      locator =>
        checkboxResponses.contains(locator.elementId)
    ).map(
        locator =>
          (locator.pdfId.get, checkboxResponses(locator.elementId))
      ).toMap
  }

  /**
   * Map all checkbox values to their PDF fields and checkbox state
   */
  def mapRadioValues(input: Map[String, JsValue], spec: Seq[PDFFieldLocator]): Map[String, Boolean] = {
    val stringInputs = input.filter {
      case (_, _: JsString) => true
      case _ => false
    }.map {
      case (k, v: JsString) => (k, v.value)
    }

    val checkboxLocators: Map[String, PDFFieldLocator] = spec.filter(_.idMap.nonEmpty).map(
      locator =>
        (locator.elementId, locator)
    ).toMap

    val filtered: Map[String, String] = stringInputs.filter {
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

  def mapStringValues(input: Map[String, JsValue], spec: Seq[PDFFieldLocator]): Map[String, String] = {
    val stringInputs = input.filter {
      case (_, _: JsString) => true
      case _ => false
    }.map {
      case (k, v: JsString) => (k, v.value)
    }

    val stringLocators: Map[String, Seq[PDFFieldLocator]] = spec.filter(
      locator => locator.idMap.isEmpty && !locator.isBase64ImageBlob.getOrElse(false)
    ).groupBy(_.elementId)

    val filtered: Map[String, String] = stringInputs.filter {
      case (k, _) => stringLocators.contains(k)
    }

    val grouped: Map[String, Iterable[PDFFieldLocator]] = stringLocators.values.flatten.groupBy(_.pdfId.get)

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
