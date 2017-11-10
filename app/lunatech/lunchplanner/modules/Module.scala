package lunatech.lunchplanner.modules

import com.google.inject.AbstractModule
import lunatech.lunchplanner.slack.LunchBotScheduler

class Module extends AbstractModule {
  protected def configure(): Unit = {
    bind(classOf[LunchBotScheduler]).asEagerSingleton()
  }
}
