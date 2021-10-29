package lunatech.lunchplanner.services

import lunatech.lunchplanner.configuration.MonthlyReportEmailConfiguration
import org.apache.commons.mail.EmailAttachment
import play.api.Logging
import play.api.libs.mailer.{AttachmentData, Email, MailerClient}

import javax.inject.Inject

class EmailService @Inject() (mailerClient: MailerClient) extends Logging {

  def sendMessageWithAttachment(
      emailConfiguration: MonthlyReportEmailConfiguration,
      messageBody: String,
      attachmentName: String,
      attachmentData: Array[Byte]
  ): String = {
    val message =
      Email(
        emailConfiguration.subject,
        emailConfiguration.from,
        emailConfiguration.to,
        Some(messageBody),
        attachments = Seq(
          AttachmentData(
            attachmentName,
            attachmentData,
            "application/vnd.ms-excel",
            None,
            Some(EmailAttachment.ATTACHMENT)
          )
        )
      )

    val result = mailerClient.send(message)
    logger.info(
      s"Monthly report sent to [${emailConfiguration.to.mkString(", ")}]"
    )
    result
  }
}
