package lunatech.lunchplanner.filters

import akka.stream.Materializer
import com.google.inject.Inject
import play.api.Environment
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class TLSFilter @Inject() (implicit
    val mat: Materializer,
    ec: ExecutionContext,
    env: Environment
) extends Filter {
  def apply(
      nextFilter: RequestHeader => Future[Result]
  )(requestHeader: RequestHeader): Future[Result] =
    if (
      requestHeader.headers
        .get("X-Forwarded-Proto")
        .getOrElse("http") != "https" && env.mode == play.api.Mode.Prod
    ) {
      Future.successful(
        Results.MovedPermanently(
          "https://" + requestHeader.host + requestHeader.uri
        )
      )
    } else {
      nextFilter(requestHeader).map(
        _.withHeaders("Strict-Transport-Security" -> "max-age=31536000")
      )
    }
}
