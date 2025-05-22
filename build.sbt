name := """todolist"""
organization := "com.timothyfisher"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.6"

libraryDependencies += guice

// MongoDB dependencies
libraryDependencies += "org.mongodb.scala" %% "mongo-scala-driver" % "4.4.0"
libraryDependencies += "org.mongodb.scala" %% "mongo-scala-bson" % "4.4.0"

// For JSON handling
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.10"

libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.timothyfisher.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.timothyfisher.binders._"
