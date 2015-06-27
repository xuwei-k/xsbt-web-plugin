import sbt._
import skinny.sbt.servlet._
import ServletPlugin._
import PluginKeys._
import Keys._
import ContainerDep.containerDepSettings

object MyBuild extends Build {
  override def projects = Seq(root)

  lazy val root = Project("root", file("."), settings = servletSettings ++ rootSettings)

  def ContainerConf = config("container")
  
  def jettyPort = 7124

  lazy val rootSettings = containerDepSettings ++ Seq(
    port in ContainerConf := jettyPort,
    scanInterval in Compile := 60,
    libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided",
    getPage := getPageTask,
    checkPage <<= checkPageTask
  )

  def indexURL = new java.net.URL("http://localhost:" + jettyPort)
  def indexFile = new java.io.File("index.html")

  lazy val getPage = TaskKey[Unit]("get-page")
  
  def getPageTask {
    indexURL #> indexFile !
  }

  lazy val checkPage = InputKey[Unit]("check-page")
  
  def checkPageTask = InputTask(_ => complete.Parsers.spaceDelimited("<arg>")) { result =>
    (getPage, result) map {
      (gp, args) =>
      checkHelloWorld(args.mkString(" ")) foreach error
    }        
  }

  private def checkHelloWorld(checkString: String) = {
    val value = IO.read(indexFile)
    if(value.contains(checkString)) None else Some("index.html did not contain '" + checkString + "' :\n" +value)
  }

}
