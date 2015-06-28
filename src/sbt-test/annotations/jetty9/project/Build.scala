import sbt._, Keys._
import skinny.servlet.ServletPlugin._
import skinny.servlet.ServletKeys._

object MyBuild extends Build {

  override def projects = Seq(root)

  lazy val root = Project("root", file("."), settings = servletSettings ++ rootSettings)

  def Conf = config("container")

  def jettyPort = 7127

  lazy val rootSettings =  Seq(
    port in Conf := jettyPort,
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
  
  def checkPageTask = InputTask.apply(_ => complete.Parsers.spaceDelimited("<arg>")) { result =>
    (getPage, result) map {
      (gp, args) => checkHelloWorld(args.mkString(" ")) foreach sys.error
    }        
  }

  private def checkHelloWorld(checkString: String) = {
    val value = IO.read(indexFile)
    if (value.contains(checkString)) None
    else Some("index.html did not contain '" + checkString + "' :\n" +value)
  }

}