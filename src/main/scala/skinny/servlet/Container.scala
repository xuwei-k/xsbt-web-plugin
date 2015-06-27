package skinny.servlet

import java.net.InetSocketAddress

import sbinary.DefaultProtocol.StringFormat
import sbt.Cache.seqFormat
import sbt.Classpaths.managedJars
import sbt.Keys._
import sbt.Scoped._
import sbt._
import sbt.complete._
import skinny.servlet.ServletKeys._

import scala.xml.NodeSeq

case class Container(name: String) {

  def Configuration = config(name).hide
  def attribute = AttributeKey[Runner](name)
  def runner = attribute

  private implicit def attributeToRunner[Runner](
    key: AttributeKey[Runner])(implicit state: State): Runner = {
    state.get(key).get
  }

  private implicit def stateToRunner(state: State): Runner = {
    state.get(attribute).get
  }

  object Impl {

    private implicit def keyToResult[T](key: TaskKey[T])(implicit state: State): T = eval(key)

    def eval[T](key: ScopedKey[sbt.Task[T]])(implicit state: State): T = {
      EvaluateTask.processResult(
        Project.runTask(key, state).map(_._2).getOrElse {
          sys.error("Error getting " + key)
        },
        state.log
      )
    }

    def newRunner(ref: ProjectRef, state: State) = {
      implicit val s = state
      val classpath = Attributed.data(fullClasspath in (ref, Configuration))
      state.put(attribute, Runner(classpath))
    }

  }

  def globalSettings = Seq(
    ivyConfigurations += Configuration
  )

  def containerSettings = Seq(
    managedClasspath <<= (classpathTypes, update) map { (ct, up) => managedJars(Configuration, ct, up) },
    fullClasspath <<= managedClasspath,
    onLoad in Global <<= (onLoad in Global, thisProjectRef) { (onLoad, containerProject) =>
      (state) => Impl.newRunner(containerProject, onLoad(state))
    },
    onUnload in Global <<= (onUnload in Global) { (onUnload) =>
      (state) => {
        state.stop()
        onUnload(state)
      }
    },
    host := "0.0.0.0",
    port := 8080,
    ssl := None,
    launch <<= (state) map { (state) => state.join() } dependsOn (start in Configuration),
    start <<= (state, host, port, ssl, apps, customConfiguration, configurationFiles, configurationXml) map {
      (state, host, port, ssl, apps, cc, cf, cx) =>
        {
          val addr = new InetSocketAddress(host, port)
          state.start(addr, toSslSettings(ssl), state.log.asInstanceOf[AbstractLogger], apps, cc, cf, cx)
        }
    },
    discoveredContexts <<= apps map discoverContexts storeAs discoveredContexts triggeredBy start,
    reload <<= reloadTask(state),
    stop <<= (state) map { (state) => state.stop() },
    restart <<= (state, host, port, ssl, apps, customConfiguration, configurationFiles, configurationXml) map {
      (state, host, port, ssl, apps, cc, cf, cx) =>
        {
          state.stop()
          val addr = new InetSocketAddress(host, port)
          state.start(addr, toSslSettings(ssl), state.log.asInstanceOf[AbstractLogger], apps, cc, cf, cx)
        }
    },
    ServletKeys.test <<= (stop in Configuration) dependsOn (Keys.test in Test) dependsOn (start in Configuration),
    customConfiguration := false,
    configurationFiles := Seq(),
    configurationXml := NodeSeq.Empty
  )

  def settings = globalSettings ++ inConfig(Configuration)(containerSettings)

  def pairToTask(conf: Configuration)(p: (String, ProjectReference)): Def.Initialize[Task[(String, Deployment)]] = {
    (deployment in (p._2, conf)) map { (d) => (p._1, d) }
  }

  type SettingSeq = Seq[Setting[_]]

  def deploy(map: (String, ProjectReference)*): SettingSeq = deploy(DefaultConf)(map: _*)

  def deploy(conf: Configuration)(map: (String, ProjectReference)*): SettingSeq = {
    settings ++ inConfig(Configuration)(Seq(
      apps <<= map.map(pairToTask(conf)).join
    ))
  }

  def toSslSettings(sslConfig: Option[(String, Int, String, String, String)]): Option[SslSettings] = {
    sslConfig.map {
      case (sslHost, sslPort, keystore, password, keyPassword) =>
        SslSettings(new InetSocketAddress(sslHost, sslPort), keystore, password, keyPassword)
    }
  }

  def discoverContexts(apps: Seq[(String, Deployment)]) = apps.map(_._1)

  def reloadParser: (State, Option[Seq[String]]) => Parser[String] = {
    import sbt.complete.DefaultParsers._
    (state, contexts) => Space ~> token(NotSpace examples contexts.getOrElse(Seq.empty).toSet)
  }

  def reloadTask(state: TaskKey[State]): Def.Initialize[InputTask[Unit]] = {
    // TODO: method apply in object InputTask is deprecated: Use another InputTask constructor or the `Def.inputTask` macro.
    InputTask.apply(loadForParser(discoveredContexts)(reloadParser)) { (result: TaskKey[String]) =>
      (state, result) map { (state: Types.Id[State], context: Types.Id[String]) => state.reload(context) }
    }
  }

}
