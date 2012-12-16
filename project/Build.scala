import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "divx-manager"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "com.github.scala-incubator.io" % "scala-io_2.10.0-RC1" % "0.4.1",
    "com.github.scala-incubator.io" % "scala-io-file_2.10.0-RC1" % "0.4.1",
    "joda-time" % "joda-time" % "2.1"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
  )

}
