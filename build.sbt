val scala3Version = "3.4.0"
val circeVersion = "0.14.1"
val scalatestVersion = "3.2.9"
val fs2Version = "3.8.0"
val jqVersion = "0.1.0-SNAPSHOT"

lazy val root = project
  .in(file("."))
  .settings(
    organization := "jqscala",
    name := "jq-fs2",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core" % circeVersion,
      "co.fs2" %% "fs2-core" % fs2Version,
      "jqscala" %% "jqscala" % jqVersion,
      "jqscala" %% "jqscala" % jqVersion classifier "tests",
      "org.scalatest" %% "scalatest" % scalatestVersion % Test
    )
  )
