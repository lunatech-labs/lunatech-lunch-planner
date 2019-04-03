package lunatech.lunchplanner.modules

import com.google.inject.AbstractModule
import lunatech.lunchplanner.schedulers.{
  LunchBotScheduler,
  MonthlyReportScheduler
}

class Module extends AbstractModule {
  protected def configure(): Unit = {
    bind(classOf[LunchBotScheduler]).asEagerSingleton()
    bind(classOf[MonthlyReportScheduler]).asEagerSingleton()
  }
}
