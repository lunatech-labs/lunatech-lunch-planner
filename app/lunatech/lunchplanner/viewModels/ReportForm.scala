package lunatech.lunchplanner.viewModels

import play.api.data.Form
import play.api.data.Forms._

object ReportForm {
  val reportForm: Form[String] = Form(
    single(
      "month" -> text
    )
  )
}
