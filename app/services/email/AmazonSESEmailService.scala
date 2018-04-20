package services.email

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util
import java.util.Properties
import javax.activation.DataHandler
import javax.inject.Inject
import javax.mail.internet.{ InternetAddress, MimeBodyPart, MimeMessage, MimeMultipart }
import javax.mail.util.ByteArrayDataSource

import com.amazonaws.regions.Regions
import com.amazonaws.services.simpleemail.model._
import com.amazonaws.services.simpleemail.{ AmazonSimpleEmailServiceAsync, AmazonSimpleEmailServiceAsyncClientBuilder }
import org.log4s.getLogger
import play.api.Configuration
import play.api.http.MimeTypes

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmazonSESEmailService @Inject() (configuration: Configuration) extends EmailService {

  private[this] val logger = getLogger
  lazy val fromEmail: String = configuration.getString("email.fromAddress").get

  def getClient: AmazonSimpleEmailServiceAsync = {
    AmazonSimpleEmailServiceAsyncClientBuilder
      .standard()
      .withRegion(Regions.US_WEST_2)
      .build()
  }

  override def sendEmail(
    recipient: String,
    subject: String,
    message: String): Future[Boolean] = {
    val client: AmazonSimpleEmailServiceAsync = getClient
    val request = new SendEmailRequest()
      .withDestination(
        new Destination().withToAddresses(recipient))
      .withMessage(new Message()
        .withBody(new Body()
          .withText(new Content()
            .withCharset("UTF-8").withData(message)))
        .withSubject(new Content()
          .withCharset("UTF-8").withData(subject)))
      .withSource(fromEmail)

    logger.info("Sending email: " + request.toString)
    val resultFuture = client.sendEmailAsync(request)

    Future {
      val result = resultFuture.get()
      logger.info("Got result from API: " + result.toString)
      result.getSdkHttpMetadata.getHttpStatusCode == play.api.http.Status.OK
    }
  }

  def sendMailWithPdfAttachment(
    recipient: String,
    subject: String,
    message: String,
    filename: String,
    attachment: Array[Byte]): Future[Boolean] = {
    val session = javax.mail.Session.getInstance(new Properties(), null)
    val mimeMessage = new MimeMessage(session)

    // Sender and recipient
    mimeMessage.setFrom(new InternetAddress(fromEmail))

    mimeMessage.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(recipient))

    // Subject
    mimeMessage.setSubject(subject)

    // Add a MIME part to the message
    val mimeBodyPart = new MimeMultipart()
    val messagePart = new MimeBodyPart()
    messagePart.setContent(message, MimeTypes.HTML)
    mimeBodyPart.addBodyPart(messagePart)

    // Add a attachment to the message
    val attachmentPart = new MimeBodyPart()
    val source = new ByteArrayDataSource(attachment, "application/pdf")
    attachmentPart.setDataHandler(new DataHandler(source))
    attachmentPart.setFileName(filename)
    mimeBodyPart.addBodyPart(attachmentPart)

    mimeMessage.setContent(mimeBodyPart)

    // Create Raw message
    val outputStream = new ByteArrayOutputStream()
    mimeMessage.writeTo(outputStream)
    val rawMessage = new RawMessage(ByteBuffer.wrap(outputStream.toByteArray))

    val client: AmazonSimpleEmailServiceAsync = getClient

    // Send Mail
    val rawEmailRequest = new SendRawEmailRequest(rawMessage)
    rawEmailRequest.setDestinations(List(recipient))
    rawEmailRequest.setSource(fromEmail)

    logger.info("Sending email with pdf attachment: " + rawEmailRequest.toString)
    val resultFuture: util.concurrent.Future[SendRawEmailResult] = client.sendRawEmailAsync(rawEmailRequest)

    Future {
      val result = resultFuture.get()
      logger.info("Got result from API: " + result.toString)
      result.getSdkHttpMetadata.getHttpStatusCode == play.api.http.Status.OK
    }
  }
}
