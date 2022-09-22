name := """lunatech-lunch-planner"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.13.9"

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
  "com.lunatech"            %% "play-googleopenconnect" % "2.8.0",
  "com.typesafe.play"       %% "play-json"              % "2.9.3",
  "com.typesafe.play"       %% "play-slick"             % "5.0.2", // 2019
  "com.typesafe.play"       %% "play-slick-evolutions"  % "5.0.2", // 2019
  "com.typesafe.slick"      %% "slick-hikaricp"         % "3.3.3", // 2020
  "org.postgresql"           % "postgresql"             % "42.5.0",
  "com.adrianhurt"          %% "play-bootstrap"         % "1.6.1-P28-B4",
  "org.scalaz"              %% "scalaz-core"            % "7.4.0-M12",
  "org.apache.poi"           % "poi-ooxml"              % "5.2.3",
  "com.enragedginger"       %% "akka-quartz-scheduler"  % "1.9.3-akka-2.6.x",
  "com.typesafe.play"       %% "play-mailer"            % "8.0.1",
  "com.typesafe.play"       %% "play-mailer-guice"      % "8.0.1",
  "com.newrelic.logging"     % "logback"                % "2.5.0",
  "org.apache.logging.log4j" % "log4j-core"             % "2.19.0",
  "org.scalamock"           %% "scalamock"              % "5.2.0" % Test,
  "org.scalatestplus.play"  %% "scalatestplus-play"     % "5.1.0" % Test,
  "org.scalatestplus"       %% "scalacheck-1-15"        % "3.2.11.0" % Test,
  "org.scalacheck"          %% "scalacheck"             % "1.17.0" % Test,
  "io.github.wolfendale"    %% "scalacheck-gen-regexp"  % "1.0.0" % Test,
  "com.h2database"           % "h2"                     % "2.1.214" % Test
)

githubTokenSource := TokenSource.Environment("GITHUB_TOKEN") || TokenSource
  .GitConfig("github.token")
resolvers += Resolver.githubPackages("lunatech-labs")

addCommandAlias("validate", ";scalafmt;coverage;test;dependencyCheck")
addCommandAlias("testCoverage", ";coverage;test;coverageReport")

Global / onChangedBuildSource := IgnoreSourceChanges
