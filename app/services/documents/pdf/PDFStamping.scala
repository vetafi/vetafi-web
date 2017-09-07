package services.documents.pdf

import java.io.{ InputStream, OutputStream }
import java.util.Base64

import com.itextpdf.text._
import com.itextpdf.text.pdf._
import org.apache.commons.io.IOUtils
import org.log4s.getLogger
import play.api.libs.json.JsValue

object PDFStamping {
  private[this] val logger = getLogger

  val ACRO_FORM_CHECKED = "1"

  lazy val CHECK: Array[Byte] = {
    IOUtils.toByteArray(getClass.getClassLoader.getResourceAsStream("forms/check.png"))
  }

  private[this] def getRectangleForField(fields: AcroFields, key: String): Rectangle = {
    val rectVals: Array[Double] = fields
      .getFieldItem(key)
      .getValue(0)
      .getAsArray(PdfName.RECT)
      .asDoubleArray()

    new Rectangle(
      rectVals(0).toFloat,
      rectVals(1).toFloat,
      rectVals(2).toFloat,
      rectVals(3).toFloat
    )
  }

  private[this] def get2xRectangleForField(fields: AcroFields, key: String): Rectangle = {
    val rectVals: Array[Double] = fields
      .getFieldItem(key)
      .getValue(0)
      .getAsArray(PdfName.RECT)
      .asDoubleArray()

    new Rectangle(
      rectVals(0).toFloat,
      (rectVals(1) - ((rectVals(3) - rectVals(1)) / 2)).toFloat,
      rectVals(2).toFloat,
      (rectVals(3) + ((rectVals(3) - rectVals(1)) / 2)).toFloat
    )
  }

  private[this] def placeImageInRectangle(image: Image, rectangle: Rectangle): Unit = {
    val x = rectangle.getLeft()
    val y = rectangle.getBottom()
    image.setAbsolutePosition(x, y)
    image.scaleToFit(rectangle)
  }

  private[this] def getPageForField(form: AcroFields, key: String): Integer = {
    form.getFieldItem(key).getPage(0)
  }

  private[this] def stampCheckbox(
    key: String,
    value: Boolean,
    form: AcroFields,
    stamper: PdfStamper,
    reader: PdfReader
  ): Unit = {
    if (value) {
      form.setField(key, ACRO_FORM_CHECKED)

      // Overlay with custom check image in addition to setting to "On"
      val img: Image = Image.getInstance(CHECK)
      val linkLocation: Rectangle = getRectangleForField(form, key)
      placeImageInRectangle(img, linkLocation)
      val pageIdx: Integer = getPageForField(form, key)
      stamper.getOverContent(pageIdx).addImage(img)
      val destination: PdfDestination = new PdfDestination(PdfDestination.FIT)
      val link: PdfAnnotation = PdfAnnotation.createLink(
        stamper.getWriter,
        linkLocation,
        PdfAnnotation.HIGHLIGHT_INVERT,
        reader.getNumberOfPages,
        destination
      )
      link.setBorder(new PdfBorderArray(0, 0, 0))
      stamper.addAnnotation(link, pageIdx)
    }
  }

  val MAX_FONT_SIZE = 32

  private[this] def stampText(key: String, value: String, form: AcroFields, pdfStamper: PdfStamper): Unit = {
    val rectangle = getRectangleForField(form, key)
    val pageIdx = getPageForField(form, key)
    val pdfContentByte = pdfStamper.getOverContent(pageIdx)
    val fontSize = ColumnText.fitText(
      new Font(Font.FontFamily.COURIER),
      value,
      rectangle,
      MAX_FONT_SIZE,
      PdfWriter.RUN_DIRECTION_DEFAULT
    )

    // A litte bit smaller than the exact height of the box is easier to read
    val font = new Font(Font.FontFamily.COURIER, fontSize * 0.90f)
    logger.info("Stamping " + value + " to rectangle " + rectangle + " with size " + fontSize)
    val text = new Chunk(value, font)
    text.setBackground(BaseColor.WHITE)
    val paragraph = new Paragraph(text)
    // How text is positioned is unclear, but 25% from bottom of box seems ideal
    ColumnText.showTextAligned(pdfContentByte, Element.ALIGN_LEFT,
      paragraph, rectangle.getLeft(),
      rectangle.getBottom() + ((rectangle.getTop() - rectangle.getBottom()) / 4),
      0)
    pdfContentByte.saveState()
  }

  private[this] def stampSignature(
    stamper: PdfStamper,
    acroFields: AcroFields,
    key: String,
    base64Image: String
  ): Unit = {

    val imageBytes: Array[Byte] = Base64.getDecoder.decode(base64Image.split(",")(1))
    val image = Image.getInstance(imageBytes)
    val rectangle = get2xRectangleForField(acroFields, key)
    placeImageInRectangle(image, rectangle)
    stamper.getOverContent(getPageForField(acroFields, key)).addImage(image)
  }

  def stampPdf(
    pdfTemplate: InputStream,
    responses: Map[String, JsValue],
    pdfFieldLocators: Seq[PDFFieldLocator],
    outputStream: OutputStream
  ): Unit = {
    val reader = new PdfReader(pdfTemplate)
    val stamper = new PdfStamper(reader, outputStream)

    try {
      val form = stamper.getAcroFields

      val checkBoxResponses: Map[String, Boolean] = PDFMapping.mapCheckBoxValues(responses, pdfFieldLocators)

      checkBoxResponses.foreach {
        case (k, v) =>
          logger.info("Stamping checkbox: " + k + " with " + v)
          stampCheckbox(k, v, form, stamper, reader);
      }

      val stringStringMap = PDFMapping.mapStringValues(responses, pdfFieldLocators)

      stringStringMap.foreach {
        case (k, v) =>
          logger.info("Stamping text: " + k + " with " + v)
          stampText(k, v, form, stamper);
      }

      val mappedRadioResponses = PDFMapping.mapRadioValues(responses, pdfFieldLocators)

      mappedRadioResponses.foreach {
        case (k, v) =>
          logger.info("Stamping radio: " + k + " with " + v)
          stampCheckbox(k, v, form, stamper, reader);
      }

      val imageMap = PDFMapping.mapBase64ImageBlogValues(responses, pdfFieldLocators)

      imageMap.foreach {
        case (k, v) =>
          logger.info("Stamping image: " + k + " with " + v)
          stampSignature(stamper, form, k, v);
      }

      (1 to reader.getNumberOfPages).foreach(
        i => form.removeFieldsFromPage(i)
      )
      form.removeXfa()
      stamper.setFormFlattening(false)
    } finally {
      stamper.close()
      reader.close()
    }
  }
}
