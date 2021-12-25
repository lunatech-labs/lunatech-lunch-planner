name := """lunatech-lunch-planner"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.13.7"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    Test / javaOptions += "-Dconfig.file=conf/application-test.conf",
    Test / testOptions += Tests
      .Argument(TestFrameworks.ScalaCheck, "-verbosity", "1"),
    Test / parallelExecution := false,
    Test / fork              := true
  )

libraryDependencies ++= Seq(
  ehcache,
  ws,
  evolutions,
  "com.lunatech"           %% "play-googleopenconnect" % "2.8.0",
  "com.typesafe.play"      %% "play-json"              % "2.9.2",
  "com.typesafe.play"      %% "play-slick"             % "5.0.0", // 2019
  "com.typesafe.play"      %% "play-slick-evolutions"  % "5.0.0", // 2019
  "com.typesafe.slick"     %% "slick-hikaricp"         % "3.3.3", // 2020
  "org.postgresql"          % "postgresql"             % "42.3.1",
  "com.adrianhurt"         %% "play-bootstrap"         % "1.6.1-P28-B4",
  "org.scalaz"             %% "scalaz-core"            % "7.4.0-M9",
  "org.apache.poi"          % "poi-ooxml"              % "5.1.0",
  "com.enragedginger"      %% "akka-quartz-scheduler"  % "1.9.2-akka-2.6.x",
  "com.typesafe.play"      %% "play-mailer"            % "8.0.1",
  "com.typesafe.play"      %% "play-mailer-guice"      % "8.0.1",
  "com.newrelic.logging"    % "logback"                % "2.3.1",
  "org.scalamock"          %% "scalamock"              % "5.2.0" % Test,
  "org.scalatestplus.play" %% "scalatestplus-play"     % "5.1.0" % Test,
  "org.scalatestplus"      %% "scalacheck-1-15"        % "3.2.10.0" % Test,
  "org.scalacheck"         %% "scalacheck"             % "1.15.4" % Test,
  "io.github.wolfendale"   %% "scalacheck-gen-regexp"  % "0.1.3" % Test,
  "com.h2database"          % "h2"                     % "2.0.204" % Test
)

resolvers ++= Seq(
  "Lunatech Artifactory".at(
    "https://artifactory.lunatech.com/artifactory/releases-public"
  )
)

addCommandAlias("validate", ";scalafmt;coverage;test;dependencyCheck")
addCommandAlias("testCoverage", ";coverage;test;coverageReport")
