package lunatech.lunchplanner.modules

/**
  * Created by sandeep on 18/05/2017.
  */

import com.google.inject.AbstractModule
import lunatech.lunchplanner.utils.MailerUtils

  class Module extends AbstractModule {
  protected def configure(): Unit = {
    bind(classOf[MailerUtils]).asEagerSingleton()
  }
}
