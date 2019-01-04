package lunatech.lunchplanner.common

import org.scalatest.MustMatchers
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar

trait BehaviorTestingConfig
  extends MockitoSugar
    with MustMatchers
    with ScalaFutures
    with DBConnectionProvider
    with TestDatabaseProvider
