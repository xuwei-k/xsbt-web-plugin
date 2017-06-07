name := "test"

scalaVersion := "2.10.6"

version := "0.1.0-SNAPSHOT"

libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided"

enablePlugins(WarPlugin)

TaskKey[Unit]("existsScalaLibraryJar") := {
  val dir = file("target") / "webapp" / "WEB-INF" / "lib"
  val v = scalaVersion.value
  if(sbtVersion.value.startsWith("0.13")) {
    assert((dir / "scala-library.jar").exists)
  } else {
    assert((dir / s"scala-library-${v}.jar").exists)
  }
}

webappPostProcess := {
  val s = streams.value.log
  webappDir =>
    def listFiles(level: Int)(f: File): Unit = {
      val indent = ((1 until level) map { _ => "  " }).mkString
      if (f.isDirectory) {
        s.info(indent + f.getName + "/")
        f.listFiles foreach { listFiles(level + 1) }
      } else s.info(indent + f.getName)
    }
    listFiles(1)(webappDir)
}
