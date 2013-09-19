name := "worker"

organization := "net.hamnaberg.restfest"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.10.2"

//libraryDependencies += "org.json4s" %% "json4s-core" % "4.2.5"

libraryDependencies ++= { 
  val path = file(util.Properties.javaHome) / "lib" / "jfxrt.jar"
  Seq("javafx" % "javafx" % "7.0" from "file:" + path)
}

libraryDependencies += "net.hamnaberg.rest" %% "scala-json-collection" % "2.2"

libraryDependencies += "net.databinder.dispatch" % "dispatch-core_2.10" % "0.11.0"

libraryDependencies += "net.databinder.dispatch" % "dispatch-json4s-native_2.10" % "0.11.0"

libraryDependencies += "net.hamnaberg.json" %% "work-order-scala" % "0.1.0-SNAPSHOT"

//libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.2.1"

libraryDependencies += "net.databinder" %% "unfiltered-directives" % "0.7.0"

libraryDependencies += "net.databinder" %% "unfiltered-jetty" % "0.7.0"

libraryDependencies += "net.databinder" %% "unfiltered-filter" % "0.7.0"
