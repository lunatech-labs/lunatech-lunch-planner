name := """lunatech-lunch-planner"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.12.4"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    javaOptions in Test += "-Dconfig.file=conf/application-test.conf",
    testOptions in Test += Tests.Argument(TestFrameworks.ScalaCheck,  "-verbosity", "1"),
    parallelExecution in Test := false,
    fork in Test := true
  )

libraryDependencies ++= Seq(
  ehcache,
  ws,
  evolutions,
  "com.typesafe.play" %% "play-json" % "2.6.8",
  "com.typesafe.play" %% "play-slick" % "3.0.3",
  "com.typesafe.play" %% "play-slick-evolutions" % "3.0.3",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.2.3",

  "org.postgresql" % "postgresql" % "42.2.5",
  "com.lunatech" %% "play-googleopenconnect" % "2.4.0",
  "com.adrianhurt" %% "play-bootstrap" % "1.2-P26-B3",
  "org.scalaz" %% "scalaz-core" % "7.2.10",
  "org.apache.poi" % "poi-ooxml" % "3.16",
  "com.enragedginger" %% "akka-quartz-scheduler" % "1.6.0-akka-2.4.x",
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
  "org.mockito" % "mockito-all" % "1.10.19" % Test,
  "org.scalacheck" %% "scalacheck" % "1.14.0" % Test,
  "org.typelevel" %% "shapeless-scalacheck" % "0.6.1" % Test,
  "wolfendale" %% "scalacheck-gen-regexp" % "0.1.0" % Test,
  "com.h2database" % "h2" % "1.4.197" % Test
)

resolvers ++= Seq(
  "Lunatech Artifactory" at "https://artifactory.lunatech.com/artifactory/releases-public",
  Resolver.bintrayRepo("wolfendale", "maven")
)

addCommandAlias("validate", ";scalafmt;coverage;test;dependencyCheck")
