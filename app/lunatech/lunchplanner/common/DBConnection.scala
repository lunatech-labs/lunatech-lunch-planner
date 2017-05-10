package lunatech.lunchplanner.common

import javax.inject.{ Inject, Singleton }

import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

@Singleton
class DBConnection @Inject() (dbConfigProvider: DatabaseConfigProvider) {
  val db = dbConfigProvider.get[JdbcProfile].db
}
