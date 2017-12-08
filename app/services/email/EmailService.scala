package services.email

import scala.concurrent.Future

trait EmailService {

  def sendEmail(recipient: String,
                subject: String,
                message: String): Future[Boolean]

  def sendMailWithPdfAttachment(recipient: String,
                                subject: String,
                                message: String,
                                filename: String,
                                attachment: Array[Byte]): Future[Boolean]
}
