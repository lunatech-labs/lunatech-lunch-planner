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
  "com.google.api-client" % "google-api-client" % "1.22.0",
  "com.google.http-client" % "google-http-client-jackson" % "1.22.0",
  "com.lunatech" %% "play-googleopenconnect" % "1.2",
  "com.google.gdata" % "gdata-appsforyourdomain" % "1.0-1.26",
  "com.google.gdata" % "gdata-client" % "1.0-1.26",
  "com.google.gdata" % "gdata-client-meta" % "1.0-1.26",
  "com.google.gdata" % "gdata-core" % "1.0-1.26",
  "com.google.gdata" % "gdata-base" % "1.0-1.26",
  "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test
)

resolvers += "Lunatech Artifactory" at "http://artifactory.lunatech.com/artifactory/releases-public"
