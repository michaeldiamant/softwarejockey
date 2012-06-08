import sbt._
import Keys._

object SoftwareJockeyBuild extends Build {

  lazy val root = Project(id = "softwarejockey", base = file(".")) aggregate(tasteOfMonadicDesign, refactoringToSrpAndOcp)

  val tasteOfMonadicDesign = Project(id = "taste-of-monadic-design", base = file("taste-of-monadic-design"))

  val refactoringToSrpAndOcp = Project(id = "refactoring-to-srp-and-ocp", base = file("refactoring-to-srp-and-ocp"))
}

