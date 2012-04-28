import sbt._
import Keys._

object SoftwareJockeyBuild extends Build {

  lazy val root = Project(id = "softwarejockey", base = file(".")) aggregate(tasteOfScalazValidation)

  val tasteOfScalazValidation = Project(id = "tasete-of-scalaz-validation", base = file("taste-of-scalaz-validation"))
}

