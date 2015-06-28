import sbt._, Keys._
import skinny.servlet.ServletPlugin._
import skinny.servlet.ServletKeys._
import ContainerDep.containerDepSettings

object MyBuild extends Build {
  override def projects = Seq(root, sub)

  private val indexFile = SettingKey[File]("index-file")
  private val indexUrl = SettingKey[java.net.URL]("index-page")

  def jettyPort = 7122

  def containerSettings: Seq[Setting[_]] = container.deploy(
    "/root" -> root,
    "/sub" -> sub
  )
  
  lazy val root = Project("root", file("."), settings = webappSettings ++ sharedSettings ++ Seq(
    indexUrl := new java.net.URL("http://localhost:"+jettyPort+"/root/")
  ) ++ containerSettings ++ containerDepSettings ++ Seq(
    port in container.Configuration := jettyPort
  ))
  lazy val sub = Project("sub", file("sub"), settings = webappSettings ++ sharedSettings ++ Seq(
    indexUrl := new java.net.URL("http://localhost:"+jettyPort+"/sub/")
  ))

  lazy val sharedSettings = Seq(
    scanInterval in Compile := 60,
    libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided",
    indexFile <<= baseDirectory / "index.html",    
    getPage <<= getPageTask,
    checkPage <<= checkPageTask
  )

  lazy val getPage = TaskKey[File]("get-page")
  
  def getPageTask: Def.Initialize[Task[File]] = (indexUrl, indexFile) map {
    (indexUrl, indexFile) =>
    indexUrl #> indexFile !;
    indexFile
  }

  lazy val checkPage = InputKey[Unit]("check-page")
  
  def checkPageTask = InputTask.apply(_ => complete.Parsers.spaceDelimited("<arg>")) { result =>
    (getPage, result) map {
      (gp, args) => checkHelloWorld(gp, args.mkString(" ")) foreach error
    }        
  }

  private def checkHelloWorld(indexFile: File, checkString: String) = {
    val value = IO.read(indexFile)
    if (value.contains(checkString)) None
    else Some("index.html did not contain '" + checkString + "' :\n" +value)
  }

}
