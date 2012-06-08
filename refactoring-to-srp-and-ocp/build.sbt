name := "refactoring-to-srp-and-ocp"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.9.1"

libraryDependencies ++= Seq(
    "org.scalaz" %% "scalaz-core" % "6.0.4",
    "com.weiglewilczek.slf4s" %% "slf4s" % "1.0.7",
    "ch.qos.logback" % "logback-classic" % "0.9.28",
    "org.specs2" %% "specs2" % "1.9" % "test",
    "org.mockito" % "mockito-all" % "1.9.0" % "test"
)

resolvers += "releases" at "http://oss.sonatype.org/content/repositories/releases"

