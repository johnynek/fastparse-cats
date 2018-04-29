import sbt._

object Dependencies {
  val typelevel = "org.typelevel"
  val catsVersion = "1.1.0"
  val fastparseVersion = "1.0.0"

  lazy val catsCore = typelevel %% "cats-core" % catsVersion
  lazy val catsLaws = typelevel %% "cats-laws" % catsVersion
  lazy val catsTestkit = typelevel %% "cats-testkit" % catsVersion
  lazy val fastparse = "com.lihaoyi" %% "fastparse" % fastparseVersion
  lazy val scalaCheck = "org.scalacheck" %% "scalacheck" % "0.13.6"
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5"
}
