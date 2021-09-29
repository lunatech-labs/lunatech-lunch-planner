name := """lunatech-lunch-planner"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.12.4"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    Test / javaOptions += "-Dconfig.file=conf/application-test.conf",
    Test / testOptions += Tests.Argument(TestFrameworks.ScalaCheck,  "-verbosity", "1"),
    Test / parallelExecution := false,
    Test / fork := true
  )

libraryDependencies ++= Seq(
  ehcache,
  ws,
  evolutions,

  "com.lunatech" %% "play-googleopenconnect" % "2.7.0",

  "com.typesafe.play" %% "play-json" % "2.8.1",
  "com.typesafe.play" %% "play-slick" % "4.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "4.0.0",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3",
  "org.postgresql" % "postgresql" % "42.2.24",

  "com.adrianhurt" %% "play-bootstrap" % "1.2-P26-B3",
  "org.scalaz" %% "scalaz-core" % "7.2.10",
  "org.apache.poi" % "poi-ooxml" % "3.16",
  "com.enragedginger" %% "akka-quartz-scheduler" % "1.8.2-akka-2.6.x",
  "com.typesafe.play" %% "play-mailer" % "6.0.1",
  "com.typesafe.play" %% "play-mailer-guice" % "6.0.1",
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
  "org.mockito" % "mockito-all" % "1.10.19" % Test,
  "org.scalacheck" %% "scalacheck" % "1.14.0" % Test,
  "org.typelevel" %% "shapeless-scalacheck" % "0.6.1" % Test,
  "io.github.wolfendale" %% "scalacheck-gen-regexp" % "0.1.3" % Test,
  "com.h2database" % "h2" % "1.4.197" % Test
)

resolvers ++= Seq(
  "Lunatech Artifactory" at "https://artifactory.lunatech.com/artifactory/releases-public"
)

addCommandAlias("validate", ";scalafmt;coverage;test;dependencyCheck")
