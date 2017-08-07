// general
organization  := "com.earldouglas"

name          := "xsbt-web-plugin"

sbtPlugin     := true

scalacOptions ++= Seq("-feature", "-deprecation")

// bintray-sbt
publishMavenStyle := false

licenses          += ("BSD New", url("http://opensource.org/licenses/BSD-3-Clause"))

// scripted-plugin
ScriptedPlugin.scriptedSettings

scriptedBufferLog  := false

scriptedLaunchOpts  += { "-Dplugin.version=" + version.value }

watchSources       ++= { (sourceDirectory.value ** "*").get }

libraryDependencies += "com.amazonaws" % "aws-java-sdk-elasticbeanstalk" % "1.11.105"

libraryDependencies += "com.amazonaws" % "aws-java-sdk-s3"               % "1.11.105"
