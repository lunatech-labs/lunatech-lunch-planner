package lunatech.lunchplanner

package object models {

  implicit class StringExtension(val text: String) {
    def normalize: String = text.replaceAll("\u0000", "")
  }

}
