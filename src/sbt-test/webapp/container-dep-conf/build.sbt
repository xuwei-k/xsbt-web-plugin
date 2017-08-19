enablePlugins(ServletPlugin)

ServletPlugin.projectSettings

libraryDependencies += "org.eclipse.jetty" % "jetty-webapp" % "9.2.19.v20160908" %
  (if(!(new File("jetty-conf") exists)) "container" else "test")

port in ServletPlugin.container.Configuration := 7121
