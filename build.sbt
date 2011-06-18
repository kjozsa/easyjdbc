name := "My Project"

version := "1.0"

libraryDependencies ++= Seq(
    "junit" % "junit" % "4.5" % "test",
    "ch.qos.logback" % "logback-classic" % "0.9.26" % "compile->default"
)

scalacOptions += "-deprecation"

scalaVersion := "2.9.0"

