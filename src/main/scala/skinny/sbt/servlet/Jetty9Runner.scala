package skinny.sbt.servlet

import java.net.InetSocketAddress

import org.eclipse.jetty.annotations.AnnotationConfiguration
import org.eclipse.jetty.plus.webapp.{ PlusConfiguration, EnvConfiguration }
import org.eclipse.jetty.server._
import org.eclipse.jetty.server.handler.ContextHandlerCollection
import org.eclipse.jetty.util.Scanner.BulkListener
import org.eclipse.jetty.util.resource.ResourceCollection
import org.eclipse.jetty.util.ssl.SslContextFactory
import org.eclipse.jetty.util.log.{ Logger => JLogger }
import org.eclipse.jetty.util.{ Scanner => JScanner }
import org.eclipse.jetty.webapp._
import org.eclipse.jetty.webapp.Configuration
import org.eclipse.jetty.xml.XmlConfiguration
import sbt._
import sbt.classpath.ClasspathUtilities.toLoader
import scala.collection.JavaConverters._

import scala.xml.NodeSeq

class Jetty9Runner extends Runner {

  private[this] val forceJettyLoad: Class[Server] = classOf[Server]
  private[this] val forceJettyLoad2: Class[NetworkTrafficServerConnector] = classOf[NetworkTrafficServerConnector]

  private var server: Server = null
  private var contexts: Map[String, (WebAppContext, Deployment)] = Map()

  private def setContextLoader(context: WebAppContext, classpath: Seq[File]) {
    val appLoader = toLoader(classpath, loader)
    context.setClassLoader(appLoader)
  }

  private lazy val annotationConfigs: Seq[Configuration] =
    try {
      Seq(
        new EnvConfiguration,
        new PlusConfiguration,
        new AnnotationConfiguration
      )
    } catch {
      case e: NoClassDefFoundError => Nil
    }

  private lazy val configs: Seq[Configuration] =
    Seq(
      new WebInfConfiguration,
      new WebXmlConfiguration,
      new MetaInfConfiguration,
      new FragmentConfiguration,
      new JettyWebXmlConfiguration
    ) ++ annotationConfigs

  private def setEnvConfiguration(context: WebAppContext, file: File) {
    val config = new EnvConfiguration { setJettyEnvXml(file.toURI.toURL) }
    val array: Array[Configuration] = (configs ++ Seq(config)).toArray
    context.setConfigurations(array)
  }

  private def deploy(contextPath: String, deployment: Deployment) = {
    import deployment._
    val context = new WebAppContext()
    context.setContextPath(contextPath)

    if (Option(System.getProperty("os.name")) exists { x => x.toLowerCase.startsWith("windows") }) {
      context.getInitParams().put("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false")
    }
    context.setBaseResource(new ResourceCollection(
      webappResources.map(_.getPath).toArray
    ))
    setContextLoader(context, classpath)

    context.setExtraClasspath(classpath.map(_.getAbsolutePath).mkString(";"))
    env match {
      case Some(e) => setEnvConfiguration(context, e)
      case None => context.setConfigurations(configs.toArray)
    }
    webInfIncludeJarPattern.foreach(context.setAttribute("org.eclipse.jetty.server.webapp.WebInfIncludeJarPattern", _))

    if (!scanDirectories.isEmpty) {
      new Scanner(scanDirectories, scanInterval, () => reload(contextPath))
    }
    contexts += contextPath -> (context, deployment)
    context
  }

  private def configureContexts(apps: Seq[(String, Deployment)]) {
    val contexts: Seq[WebAppContext] = apps.map {
      case (contextPath, deployment) =>
        deploy(contextPath, deployment)
    }
    val coll = new ContextHandlerCollection()
    coll.setHandlers(contexts.toArray)
    server.setHandler(coll)
  }

  private def configureCustom(confFiles: Seq[File], confXml: NodeSeq) {
    confXml.foreach(x => new XmlConfiguration(x.toString).configure(server))
    confFiles.foreach(f => new XmlConfiguration(f.toURI.toURL).configure(server))
  }

  private def configureConnector(addr: InetSocketAddress) {
    val conn = new ServerConnector(server)
    conn.setHost(addr.getAddress.getHostAddress)
    conn.setPort(addr.getPort)
    server.addConnector(conn)
  }

  private def configureSecureConnector(ssl: SslSettings) {
    val context = new SslContextFactory()
    context.setKeyStorePath(ssl.keystore)
    context.setKeyStorePassword(ssl.password)
    val conn = new ServerConnector(server, context)
    conn.setHost(ssl.addr.getAddress.getHostAddress)
    conn.setPort(ssl.addr.getPort)
    server.addConnector(conn)
  }

  def start(
    addr: InetSocketAddress,
    ssl: Option[SslSettings],
    logger: AbstractLogger,
    apps: Seq[(String, Deployment)],
    customConf: Boolean,
    confFiles: Seq[File],
    confXml: NodeSeq) {

    if (server != null) {
      return
    }
    try {
      server = new Server
      if (customConf) {
        configureCustom(confFiles, confXml)
      } else {
        configureConnector(addr)
        ssl match {
          case Some(s) => configureSecureConnector(s)
          case _ =>
        }
        configureContexts(apps)
      }
      server.start()
    } catch {
      case t: Throwable =>
        server = null
        throw t
    }
  }

  def reload(contextPath: String) {
    val (context, deployment) = contexts(contextPath)
    context.stop()
    setContextLoader(context, deployment.classpath)
    context.start()
  }

  def join(): Unit = {
    if (server != null) {
      server.join()
    }
  }

  def stop(): Unit = {
    if (server != null) {
      server.stop()
    }
    server = null
  }

  class DelegatingLogger(delegate: AbstractLogger) extends LoggerBase(delegate) with JLogger {
    def getLogger(name: String) = this
    def debug(x: String, y: Long): Unit = ()
  }

  class Scanner(scanDirs: Seq[File], scanInterval: Int, thunk: () => Unit) extends JScanner {
    setScanDirs(scanDirs.asJava)
    setRecursive(true)
    setScanInterval(scanInterval)
    setReportExistingFilesOnStartup(false)
    addListener(new BulkListener {
      def filesChanged(files: java.util.List[String]) { thunk() }
    })
    start()
  }

}

