import sbt._
import play.Play.autoImport._
import PlayKeys._

object ApplicationBuild extends Build {

  val appName         = "dentaise-web"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    javaCore,
    javaJpa,
    "postgresql" % "postgresql" % "9.1-901.jdbc4",
    "org.hibernate" % "hibernate-ehcache" % "4.1.8.Final",
    "org.hibernate" % "hibernate-entitymanager" % "4.1.9.Final"
  )

  val main = Project(appName, file(".")).enablePlugins(play.PlayJava).settings(
  	ebeanEnabled := false 
    // Add your own project settings here      
  )

}
