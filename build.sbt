name := """janus"""

resolvers ++= Seq("Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/")

version := "1.0"

scalaVersion := "2.11.6"

libraryDependencies ++= {
  val akkaVersion = "2.4.9  "

  Seq(
    "com.typesafe.akka" %% "akka-actor" % "2.4.0",
    "com.typesafe.akka" %% "akka-persistence" % "2.4.0",
    "com.typesafe.akka" %% "akka-testkit" % "2.4.0" % "test",
    "org.typelevel" %% "cats" % "0.7.2",
    "org.scalatest" %% "scalatest" % "2.2.4" % "test"
  )
}


fork in run := true