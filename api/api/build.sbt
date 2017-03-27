import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.12.1",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "API",
    resolvers += Resolver.bintrayRepo("cakesolutions", "maven"),
    libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.0.0",
    libraryDependencies += "net.cakesolutions" %% "scala-kafka-client" % "0.10.2.0"

  )
