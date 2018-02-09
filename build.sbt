name := """lunatech-lunch-planner"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.8"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    javaOptions in Test += "-Dconfig.file=conf/application-test.conf"
  )
  .enablePlugins(DockerComposePlugin)

dockerImageCreationTask := (publishLocal in Docker).value

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
  "com.lunatech" %% "play-googleopenconnect" % "1.3-SNAPSHOT",
  "com.adrianhurt" %% "play-bootstrap" % "1.1.1-P25-B3",
  "com.enragedginger" %% "akka-quartz-scheduler" % "1.6.0-akka-2.4.x",
  "org.scalaz" %% "scalaz-core" % "7.2.10",
  "com.typesafe.play" %% "play-mailer" % "5.0.0",
  "com.github.nscala-time" %% "nscala-time" % "2.16.0",
  "org.apache.poi" % "poi-ooxml" % "3.16",
  "com.enragedginger" %% "akka-quartz-scheduler" % "1.6.0-akka-2.4.x",
  "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test,
  "org.mockito" % "mockito-all" % "1.10.19" % Test
)

resolvers += "Lunatech Artifactory" at "http://artifactory.lunatech.com/artifactory/snapshots-public"
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
