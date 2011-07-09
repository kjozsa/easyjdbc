name := "My Project"

version := "1.0"

libraryDependencies ++= Seq(
    "junit" % "junit" % "4.8.2",
    "org.scalatest" % "scalatest_2.9.0" % "1.6.1",
    "org.mockito" % "mockito-all" % "1.8.5",
    "ch.qos.logback" % "logback-classic" % "0.9.26" % "compile->default"
    )

scalacOptions += "-deprecation"

scalaVersion := "2.9.0"

