import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

	val appName         = "play21-frames-module"
	val appVersion      = "1.0-SNAPSHOT"
	val tinkerpopVersion = "2.2.0"  

	val appDependencies = Seq(
//	    "com.tinkerpop.blueprints" % "blueprints-core" % {tinkerpopVersion},
//	    "com.tinkerpop" % "pipes" % {tinkerpopVersion},    
//	    "com.tinkerpop.blueprints" % "blueprints-test" % {tinkerpopVersion},
	    "com.tinkerpop" % "frames" % {tinkerpopVersion},
	    javaCore
	)

	val main = play.Project(appName, appVersion, appDependencies).settings(	
	    organization := "com.wingnest.play2",
	    resolvers += "Oracle Releases" at "http://download.oracle.com/maven/"
	)
	
}
