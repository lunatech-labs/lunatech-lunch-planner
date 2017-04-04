name := """lunatech-lunch-planner"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    javaOptions in Test += "-Dconfig.file=conf/application-test.conf"
  )

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  cache,
  ws,
  evolutions,
  "com.typesafe.play" %% "play-slick" % "2.0.2",
  "com.typesafe.play" %% "play-slick-evolutions" % "2.0.2",
  "org.postgresql" % "postgresql" % "9.3-1102-jdbc41",
  "com.typesafe.slick" %% "slick" % "3.1.1",
  "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test
)


