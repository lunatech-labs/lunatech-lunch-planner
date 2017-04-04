package lunatech.lunchplanner.common

import slick.jdbc.JdbcBackend

trait DBConnectionProvider {
  def db: JdbcBackend#DatabaseDef
}
