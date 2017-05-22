package lunatech.lunchplanner.utils

import java.util.concurrent.TimeUnit
import javax.inject.Inject

import akka.actor.ActorSystem
import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.persistence.UserTable
import play.api.libs.mailer.{Email, MailerClient}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration


class MailerUtils @Inject()(mailerClient: MailerClient, implicit val connection: DBConnection) {

  import java.util.Calendar

  private def timeToNextMonday = {
    val now = Calendar.getInstance
    now.set(Calendar.HOUR, 23)
    now.set(Calendar.MINUTE, 50)
    val weekday = now.get(Calendar.DAY_OF_WEEK)
    if (weekday != Calendar.MONDAY) { // calculate how much to add
      // the 2 is the difference between Saturday and Monday
      val days = (Calendar.SATURDAY - weekday + 2) % 7
      now.add(Calendar.DAY_OF_YEAR, days)
    }
    val date = now.getTime
    (now.getTime.getTime - System.currentTimeMillis) / (1000 * 60)
  }

  def send(sendTo: List[String], body: String): String = {
    val email: Email = Email("Friday Lunch", "@Lunatech <hrm@lunatech.com>", sendTo, bodyHtml = Some(views.html.mail.render.body))
    mailerClient.send(email)
  }

  val system = ActorSystem("mailer-system")
  system.scheduler.schedule(
    Duration.create(timeToNextMonday, TimeUnit.MINUTES),
    Duration.create(7, TimeUnit.DAYS))(sendMail())

  def sendMail() = {
    val users = UserTable.getAll.map(user => user.map(_.emailAddress).toList)
    users.map(usersList => send(usersList, views.html.mail.render.body))
  }
}
