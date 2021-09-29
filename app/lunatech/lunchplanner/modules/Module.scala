package lunatech.lunchplanner.modules

import com.google.inject.AbstractModule
import lunatech.lunchplanner.schedulers.{
  LunchBotScheduler,
  MonthlyReportScheduler
}

class Module extends AbstractModule {
  protected override def configure(): Unit = {
    bind(classOf[LunchBotScheduler]).asEagerSingleton()
    bind(classOf[MonthlyReportScheduler]).asEagerSingleton()
  }
}
