servletSettings

libraryDependencies += "org.eclipse.jetty" % "jetty-webapp" % "9.2.19.v20160908" %
  (if(!(new File("jetty-conf") exists)) "container" else "test")

port in container.Configuration := 7121
