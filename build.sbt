import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.12.6",
      version      := "0.1.0-SNAPSHOT"
    )),
    addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.6"),
    name := "fastparse-cats",
    libraryDependencies ++= Seq(
      catsCore,
      fastparse,
      catsLaws % Test,
      catsTestkit % Test,
      scalaCheck % Test,
      scalaTest % Test)
  )
