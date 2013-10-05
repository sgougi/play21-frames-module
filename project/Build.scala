import sbt._
import Keys._
import play.Project._


object ApplicationBuild extends Build {

	val appName         = "play21-frames-module"
	val appVersion      = "2.4.2"
	val tinkerpopVersion = "2.4.0"  

	val appDependencies = Seq(
//	    "com.tinkerpop.blueprints" % "blueprints-core" % {tinkerpopVersion},
//	    "com.tinkerpop" % "pipes" % {tinkerpopVersion},    
//	    "com.tinkerpop.blueprints" % "blueprints-test" % {tinkerpopVersion},
	    "com.tinkerpop" % "frames" % {tinkerpopVersion},	    
	    javaCore
	)

	val main = play.Project(appName, appVersion, appDependencies).settings(
	    publishArtifact in(Compile, packageDoc) := false,
	    organization := "com.wingnest.play2",
	    resolvers += "TinkerPop Snapshot" at "https://oss.sonatype.org/content/repositories/snapshots",
	    resolvers += "Oracle Releases" at "http://download.oracle.com/maven/"
	)
	
}
