package lunatech.lunchplanner.services

import javax.inject.Inject
import lunatech.lunchplanner.configuration.MonthlyReportEmailConfiguration
import org.apache.commons.mail.EmailAttachment
import play.api.Logger
import play.api.libs.mailer.{AttachmentData, Email, MailerClient}

class EmailService @Inject()(mailerClient: MailerClient) {

  def sendMessageWithAttachment(
      emailConfiguration: MonthlyReportEmailConfiguration,
      messageBody: String,
      attachmentName: String,
      attachmentData: Array[Byte]): String = {
    val message =
      Email(
        emailConfiguration.subject,
        emailConfiguration.from,
        emailConfiguration.to,
        Some(messageBody),
        attachments = Seq(
          AttachmentData(attachmentName,
                         attachmentData,
                         "application/vnd.ms-excel",
                         None,
                         Some(EmailAttachment.ATTACHMENT))
        )
      )

    val result = mailerClient.send(message)
    Logger.info(
      s"Monthly report sent to [${emailConfiguration.to.mkString(", ")}]")
    result
  }
}
