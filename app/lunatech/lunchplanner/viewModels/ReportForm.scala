package lunatech.lunchplanner.viewModels

import lunatech.lunchplanner.models.ReportDate
import play.api.data.Form
import play.api.data.Forms._

object ReportForm {

  val reportForm: Form[ReportDate] = Form(
    mapping(
      "month" -> number,
      "year" -> number
    )(ReportDate.apply)(ReportDate.unapply)
  )
}
