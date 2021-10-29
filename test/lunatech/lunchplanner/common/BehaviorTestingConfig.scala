package lunatech.lunchplanner.common

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers

trait BehaviorTestingConfig
    extends MockFactory
    with Matchers
    with ScalaFutures
    with DBConnectionProvider
    with TestDatabaseProvider
