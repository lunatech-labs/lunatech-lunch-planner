addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.19")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.8")

addSbtPlugin("net.vonbuchholtz" % "sbt-dependency-check" % "5.1.0")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.0")

addSbtPlugin("com.codecommit" % "sbt-github-packages" % "0.5.3")

ThisBuild / libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
)
