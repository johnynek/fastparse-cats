import ReleaseTransformations._
import com.typesafe.tools.mima.plugin.MimaPlugin.mimaDefaultSettings

val typelevel = "org.typelevel"
val catsVersion = "1.1.0"
val fastparseVersion = "1.0.0"
val scalaCheckVersion = "0.13.6"
val scalaTestVersion = "3.0.5"

lazy val noPublish = Seq(publish := {}, publishLocal := {}, publishArtifact := false)

lazy val fastparseCatsSettings = Seq(
  organization := "org.bykn",
  scalaVersion := "2.12.6",
  crossScalaVersions := Seq("2.11.11", "2.12.6"),
  libraryDependencies ++= Seq(compilerPlugin("org.spire-math" %% "kind-projector" % "0.9.6"),
    typelevel %%% "cats-core" % catsVersion,
    typelevel %%% "cats-laws" % catsVersion,
    typelevel %%% "cats-testkit" % catsVersion,
    "com.lihaoyi" %%% "fastparse" % fastparseVersion,
    "org.scalacheck" %%% "scalacheck" % scalaCheckVersion,
    "org.scalatest" %%% "scalatest" % scalaTestVersion),
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-language:experimental.macros",
    "-unchecked",
    "-Xfatal-warnings",
    "-Xlint",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-Xfuture"
  ),
  // HACK: without these lines, the console is basically unusable,
  // since all imports are reported as being unused (and then become
  // fatal errors).
  scalacOptions in (Compile, console) ~= { _.filterNot("-Xlint" == _) },
  scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value,
  // release stuff
  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := Function.const(false),
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    releaseStepCommand("validate"),
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    publishArtifacts,
    setNextVersion,
    commitNextVersion,
    releaseStepCommand("sonatypeReleaseAll"),
    pushChanges),
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")),
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("Snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("Releases" at nexus + "service/local/staging/deploy/maven2")
  },
  pomExtra := (
    <url>https://github.com/johnynek/fastparse-cats</url>
    <licenses>
      <license>
        <name>Apache 2</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
        <distribution>repo</distribution>
        <comments>A business-friendly OSS license</comments>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:johnynek/fastparse-cats.git</url>
      <connection>scm:git:git@github.com:johnynek/fastparse-cats.git</connection>
    </scm>
    <developers>
      <developer>
        <id>johnynek</id>
        <name>Oscar Boykin</name>
        <url>http://github.com/johnynek/</url>
      </developer>
    </developers>
  ),
  coverageMinimum := 60,
  coverageFailOnMinimum := false
) ++ mimaDefaultSettings

/* def previousArtifact(proj: String) = */
/*   "com.stripe" %% s"dagon-$proj" % "0.2.4" */

lazy val commonJvmSettings = Seq(
  testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oDF"))

lazy val commonJsSettings = Seq(
  scalaJSStage in Global := FastOptStage,
  parallelExecution := false,
  jsEnv := new org.scalajs.jsenv.nodejs.NodeJSEnv(),
  // batch mode decreases the amount of memory needed to compile scala.js code
  scalaJSOptimizerOptions := scalaJSOptimizerOptions.value.withBatchMode(
    scala.sys.env.get("TRAVIS").isDefined)
)

lazy val fastparseCats = project
  .in(file("."))
  .settings(name := "root")
  .settings(fastparseCatsSettings: _*)
  .settings(noPublish: _*)
  .aggregate(fastparseCatsJVM, fastparseCatsJS)
  .dependsOn(fastparseCatsJVM, fastparseCatsJS)

lazy val fastparseCatsJVM = project
  .in(file(".fastparseCatsJVM"))
  .settings(moduleName := "fastparseCats")
  .settings(fastparseCatsSettings)
  .settings(commonJvmSettings)
  .aggregate(coreJVM)
  .dependsOn(coreJVM)

lazy val fastparseCatsJS = project
  .in(file(".fastparseCatsJS"))
  .settings(moduleName := "fastparseCats")
  .settings(fastparseCatsSettings)
  .settings(commonJsSettings)
  .aggregate(coreJS)
  .dependsOn(coreJS)
  .enablePlugins(ScalaJSPlugin)

lazy val core = crossProject
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(name := "fastparse-cats-core")
  .settings(moduleName := "fastparse-cats-core")
  .settings(fastparseCatsSettings: _*)
  //.settings(mimaPreviousArtifacts := Set(previousArtifact("core")))
  .disablePlugins(JmhPlugin)
  .jsSettings(commonJsSettings: _*)
  .jsSettings(coverageEnabled := false)
  .jvmSettings(commonJvmSettings: _*)

lazy val coreJVM = core.jvm
lazy val coreJS = core.js

lazy val byte = crossProject
  .crossType(CrossType.Pure)
  .in(file("byte"))
  .settings(name := "fastparse-cats-byte")
  .settings(moduleName := "fastparse-cats-byte")
  .settings(fastparseCatsSettings: _*)
  .settings(libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "fastparse-byte" % fastparseVersion))
  //.settings(mimaPreviousArtifacts := Set(previousArtifact("core")))
  .disablePlugins(JmhPlugin)
  .jsSettings(commonJsSettings: _*)
  .jsSettings(coverageEnabled := false)
  .jvmSettings(commonJvmSettings: _*)
  .dependsOn(core)


lazy val byteJVM = byte.jvm
lazy val byteJS = byte.js

