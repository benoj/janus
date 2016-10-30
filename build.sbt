name := """janus"""


version := "1.0"

scalaVersion := "2.11.8"


val akkaVersion = "2.4.9"


lazy val root = project.in(file(".")).aggregate(core, `rest-api`)

lazy val core = (project in file("core")).settings(
  scalaVersion := "2.11.8",

  resolvers ++= Seq("Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"),

  libraryDependencies ++= {
    Seq(
      "com.typesafe.akka" %% "akka-actor" % "2.4.11",
      "com.typesafe.akka" %% "akka-persistence" % "2.4.11",
      "org.typelevel" %% "cats" % "0.7.2",
      "org.scalatest" %% "scalatest" % "2.2.4" % "test"
    )
  }
)

lazy val `rest-api` = (project in file("rest-api")).settings(
  scalaVersion := "2.11.8",
  resolvers ++= Seq("Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"),
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-http-experimental" % "2.4.11"
  )
)

fork in run := true
