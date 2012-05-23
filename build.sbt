name := "finagle-gntp"

organization := "com.github.okapies"

version := "0.1-SNAPSHOT"

scalaVersion := "2.9.2"

scalacOptions ++= Seq("-deprecation", "-unchecked")

resolvers ++= Seq(
  "twitter-repo" at "http://maven.twttr.com",
  "sonatype-snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "sonatype-releases"  at "http://oss.sonatype.org/content/repositories/releases"
)

libraryDependencies ++= Seq(
  "com.twitter" % "finagle-core_2.9.1" % "4.0.2",
  "com.twitter" % "finagle-http_2.9.1" % "4.0.2",
  "com.twitter" % "finagle-stream_2.9.1" % "4.0.2",
  "com.twitter" % "naggati_2.9.1" % "3.0.0",
  "org.slf4j" % "slf4j-api" % "1.6.4",
  "org.slf4j" % "slf4j-log4j12" % "1.6.4"
)

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2" % "1.9" % "test"
)

initialCommands in console := """import com.github.okapies.finagle.gntp._
import org.apache.log4j._
import org.jboss.netty.logging._
BasicConfigurator.configure(new ConsoleAppender(
  new PatternLayout("%d{yyyy/MM/dd,HH:mm:ss,SSS} [%p](%t) - %m%n")))
InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory)
"""
