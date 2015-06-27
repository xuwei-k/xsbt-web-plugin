package skinny.sbt.servlet

import sbt._
import Keys._
import PluginKeys._

object ServletPlugin extends Plugin {

  lazy val container = Container("container")

  def servletSettings: Seq[Setting[_]] = servletSettings(DefaultConf)

  def servletSettings(conf: Configuration): Seq[Setting[_]] = {
    container.settings ++
      inConfig(conf)(WebappPlugin.webappSettings0) ++
      Seq(
        apps in container.Configuration <<= (deployment in conf) map {
          d => Seq("/" -> d)
        }
      ) ++ WarPlugin.globalWarSettings
  }

}