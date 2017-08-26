package services.documents.pdf

import java.io.File

import com.google.common.collect.ComparisonChain
import com.itextpdf.text.pdf.{ AcroFields, PdfName, PdfReader, PdfStamper }
import org.apache.commons.io.FileUtils
import org.apache.commons.io.output.NullOutputStream

import scala.collection.JavaConverters._

object FieldPageAppearanceOrdering extends Ordering[AcroFields.Item] {
  override def compare(left: AcroFields.Item, right: AcroFields.Item): Int = {
    ComparisonChain.start()
      .compare(left.getPage(0).intValue(), right.getPage(0).intValue())
      .compare(right.getValue(0).getAsArray(PdfName.RECT).asDoubleArray()(1), left.getValue(0).getAsArray(PdfName.RECT).asDoubleArray()(1))
      .compare(left.getValue(0).getAsArray(PdfName.RECT).asDoubleArray()(0), right.getValue(0).getAsArray(PdfName.RECT).asDoubleArray()(0))
      .result()
  }
}

/**
 * Util for listing fields in a given PDF
 *
 * Will display fields in the order they appear physically in the document
 */
object PDFListFieldsUtil extends App {

  val pdfTemplate = FileUtils.openInputStream(new File(args(0)))

  val reader = new PdfReader(pdfTemplate)
  val stamper = new PdfStamper(reader, new NullOutputStream())

  val form = stamper.getAcroFields
  val fields: Map[String, AcroFields.Item] = form.getFields.asScala.toMap
  val items = fields.toSeq.sortBy(_._2)(FieldPageAppearanceOrdering)

  items.foreach {
    case (k, v) =>
      println(s"$k page: ${v.getPage(0).intValue} " +
        s"x: ${v.getValue(0).getAsArray(PdfName.RECT).asDoubleArray()(0)} " +
        s"y: ${v.getValue(0).getAsArray(PdfName.RECT).asDoubleArray()(1)}")
  }
}
