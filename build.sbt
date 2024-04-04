val scala3Version = "3.4.0"
val circeVersion = "0.14.1"
val scalatestVersion = "3.2.9"
val fs2Version = "3.8.0"
val jqVersion = "0.1.0"

ThisBuild / organization := "io.github.jqscala"
ThisBuild / homepage := Some(url("https://github.com/jqscala/jq-fs2"))
ThisBuild / licenses := List("Creative Commons" -> url("https://creativecommons.org/licenses/by-nc-sa/4.0/"))
ThisBuild / developers := List(
  Developer(
    "Juan Manuel Serrano Hidalgo",
    "juanmanuel.serrano@hablapps.com",
    "info@hablapps.com",
    url("https://hablapps.com")
  )
  // add more devs here
)

ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"
ThisBuild / sonatypeRepository := "https://s01.oss.sonatype.org/service/local"


lazy val root = project
  .in(file("."))
  .settings(
    name := "jq-fs2",

    scalaVersion := scala3Version,

    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core" % circeVersion,
      "co.fs2" %% "fs2-core" % fs2Version,
      "io.github.jqscala" %% "jqscala" % jqVersion,
      "io.github.jqscala" %% "jqscala" % jqVersion classifier "tests",
      "org.scalatest" %% "scalatest" % scalatestVersion % Test
    )
  )
