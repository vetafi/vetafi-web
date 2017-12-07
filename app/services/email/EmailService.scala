package services.email

trait EmailService {

  def sendEmail(recipient: String, message: String): Unit
}
