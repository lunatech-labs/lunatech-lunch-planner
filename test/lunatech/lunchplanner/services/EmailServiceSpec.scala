package lunatech.lunchplanner.services

import lunatech.lunchplanner.common.BehaviorTestingConfig
import lunatech.lunchplanner.configuration.MonthlyReportEmailConfiguration
import org.scalatest.BeforeAndAfterEach
import play.api.libs.mailer.MailerClient

class EmailServiceSpec extends BehaviorTestingConfig with BeforeAndAfterEach {

  private val mailerClient = mock[MailerClient]

  private val emailService = new EmailService(mailerClient)

  val emailConfiguration = MonthlyReportEmailConfiguration(
    "email subject",
    "from@emailLunatech.nl",
    Seq("to@reportClien.com")
  )
  val messageBody    = "This is the message body!"
  val attachmentName = "attachmentName.txt"
  val attachmentData = "This is the attachment data".getBytes

  "EmailService" should {
//    "send email messages with Attachment" in {
//      val returnedValue = "email sent"
//
//      when(mailerClient.send(any())).thenReturn(returnedValue)
//
//      val result = emailService.sendMessageWithAttachment(emailConfiguration,
//                                                          messageBody,
//                                                          attachmentName,
//                                                          attachmentData)
//      verify(mailerClient, times(1)).send(any())
//      result mustBe returnedValue
//    }
  }
}
