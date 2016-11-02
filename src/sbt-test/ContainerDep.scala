import sbt._
import sbt.Keys._

/**
 * Used by the scripted tests in the group 'webapp-common'.  It is used to
 * specify container dependencies based on a system property.  This allows
 * our custom scripted configuration to run the same tests for all of the
 * supported servlet containers.
 */
object ContainerDep {

  private val jettyVersion = "9.2.19.v20160908"

  def containerDepSettings = {
    Seq {
      libraryDependencies ++= Seq(
        "org.eclipse.jetty" % "jetty-webapp" % jettyVersion % "container",
        "org.eclipse.jetty" % "jetty-jsp"    % jettyVersion % "container",
        "org.eclipse.jetty" % "jetty-plus"   % jettyVersion % "container"
      )
    }
  }

}
