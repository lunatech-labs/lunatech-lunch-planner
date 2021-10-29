package lunatech.lunchplanner.common

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}

@Singleton
class DBConnection @Inject() (dbConfigProvider: DatabaseConfigProvider) {
  val db = dbConfigProvider.get[JdbcProfile].db
}
