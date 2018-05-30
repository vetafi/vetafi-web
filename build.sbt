import scalariform.formatter.preferences._
import com.typesafe.sbt.packager.debian._

// TODO https://github.com/yohangz/java-play-angular-seed/blob/master/ui/src/app/app.module.ts

name := "vetafi-web"

version := "0.0.5"

scalaVersion := "2.11.8"
autoScalaLibrary := false

version in Debian := version.toString

maintainer in Debian := "Jeff Quinn jeff@vetafi.org"

packageSummary in Debian := "Vetafi.org web application."

unmanagedClasspath in Runtime += baseDirectory.value / "conf"

resolvers += Resolver.jcenterRepo
resolvers += Resolver.url("Typesafe Ivy releases", url("https://repo.typesafe.com/typesafe/ivy-releases"))(Resolver.ivyStylePatterns)
resolvers += "jitpack" at "https://jitpack.io" // Used to resolve com.github.* projects
resolvers += "softprops-maven" at "http://dl.bintray.com/content/softprops/maven"

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "play2-reactivemongo" % "0.12.1",
  "com.mohiva" %% "play-silhouette" % "5.0.0",
  "com.mohiva" %% "play-silhouette-password-bcrypt" % "5.0.0",
  "com.mohiva" %% "play-silhouette-persistence" % "5.0.0",
  "com.mohiva" %% "play-silhouette-crypto-jca" % "5.0.0",
  "com.mohiva" %% "play-silhouette-persistence-reactivemongo" % "5.0.1",
  "net.codingwell" %% "scala-guice" % "4.0.1",
  "com.iheart" %% "ficus" % "1.2.6",
  "com.typesafe.play" %% "play-mailer" % "5.0.0",
  "com.enragedginger" %% "akka-quartz-scheduler" % "1.5.0-akka-2.4.x",
  "com.mohiva" %% "play-silhouette-testkit" % "5.0.0" % "test",
  "com.digitaltangible" %% "play-guard" % "2.1.0",
  "me.lessis" %% "retry" % "0.2.0",
  ("com.github.dcoker" % "biscuit-java" % "ebed4b3a238a45c007da138175f1132a6bf26b71").exclude("com.github.emstlk", "nacl4s_2.10"),
  "net.logstash.logback" % "logstash-logback-encoder" % "4.11",
  "ch.qos.logback" % "logback-core" % "1.2.3",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.log4s" %% "log4s" % "1.3.6",
  "com.github.emstlk" %% "nacl4s" % "1.0.0",
  "com.itextpdf" % "itextpdf" % "5.5.12",
  "commons-io" % "commons-io" % "2.5",
  "commons-codec" % "commons-codec" % "1.10",
  "org.apache.commons" % "commons-lang3" % "3.6",
  "org.apache.pdfbox" % "pdfbox" % "2.0.7",
  "com.twilio.sdk" % "twilio" % "7.14.5",
  "com.amazonaws" % "aws-java-sdk-ses" % "1.11.244",
  "com.amazonaws" % "aws-java-sdk" % "1.11.316",
  "com.typesafe.play" %% "play-json-joda" % "2.6.8",
  specs2 % Test,
  filters,
  guice
)

lazy val root = (project in file("."))
    .enablePlugins(PlayScala, DebianPlugin).settings(
      watchSources ++= (baseDirectory.value / "public/ui" ** "*").get
    )

PB.targets in Compile := Seq(
  scalapb.gen() -> file("./protos")
)

routesGenerator := InjectedRoutesGenerator

routesImport += "utils.route.Binders._"

//********************************************************
// Scalariform settings
//********************************************************

scalariformPreferences := scalariformPreferences.value

fork in run := false
