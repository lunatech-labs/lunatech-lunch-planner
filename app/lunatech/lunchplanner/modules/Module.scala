package lunatech.lunchplanner.modules

import com.google.inject.AbstractModule
import lunatech.lunchplanner.utils.MailerUtils

  class Module extends AbstractModule {
  protected def configure(): Unit = {
    bind(classOf[MailerUtils]).asEagerSingleton()
  }
}
