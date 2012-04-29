import sbt._
import Keys._

object SoftwareJockeyBuild extends Build {

  lazy val root = Project(id = "softwarejockey", base = file(".")) aggregate(tasteOfMonadicDesign)

  val tasteOfMonadicDesign = Project(id = "tasete-of-monadic-design", base = file("taste-of-monadic-design"))
}

