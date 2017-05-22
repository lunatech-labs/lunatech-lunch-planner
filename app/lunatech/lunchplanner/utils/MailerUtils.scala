package lunatech.lunchplanner.utils

import java.util.concurrent.TimeUnit
import javax.inject.Inject

import akka.actor.ActorSystem
import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.persistence.UserTable
import play.api.Configuration
import play.api.libs.mailer.{Email, MailerClient}
import com.github.nscala_time.time.Imports._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

/**
  * This is utility to send mail to all the users on every monday
  *
  * @param mailerClient is used for send mail
  * @param connection   is the used for connection to db for retrieve all users
  *
  *
  */
class MailerUtils @Inject()(mailerClient: MailerClient, configuration: Configuration, implicit val connection: DBConnection) {

  private def timeToNextMonday = {

    val today = LocalDate.now
    val dayOfWeek = today.getDayOfWeek
    val nextMonday = today.plusDays(8 - dayOfWeek)
    val nextMondayTime = nextMonday.toDateTime(new LocalTime(15, 30))
    (nextMondayTime.getMillis - System.currentTimeMillis) / (1000 * 60)
  }

  def send(sendTo: List[String], body: String): String = {
    val email: Email = Email("Friday Lunch", "@Lunatech <hrm@lunatech.com>", sendTo, bodyHtml = Some(body))
    mailerClient.send(email)
  }

  val system = ActorSystem("mailer-system")
  system.scheduler.schedule(
    Duration.create(timeToNextMonday, TimeUnit.MINUTES),
    Duration.create(7, TimeUnit.DAYS))(sendMail())

  def sendMail() = {
    val users = UserTable.getAll.map(user => user.map(_.emailAddress).toList)
    users.map(usersList => send(usersList, views.html.mail.render(configuration.getString("lunatech.email").getOrElse("info@lunatech.com")).body))
  }
}
