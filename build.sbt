ThisBuild / scalaVersion := "2.13.12"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := "scala-todolist",
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      guice,
      "org.mongodb.scala" %% "mongo-scala-driver" % "4.11.0",
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,
      "com.typesafe.akka" %% "akka-stream" % "2.6.20",
      "com.typesafe.akka" %% "akka-actor-typed" % "2.6.20",
      "com.typesafe.akka" %% "akka-serialization-jackson" % "2.6.20",
      "com.typesafe.akka" %% "akka-slf4j" % "2.6.20"
    )
  )

evictionErrorLevel := Level.Warn

Test / javaOptions += "-Dplay.ws.enabled=false"
