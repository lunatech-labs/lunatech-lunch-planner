addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.17")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.5")

addSbtPlugin("net.vonbuchholtz" % "sbt-dependency-check" % "4.1.0")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.6")

addSbtPlugin("com.codecommit" % "sbt-github-packages" % "0.5.3")

ThisBuild / libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
)
