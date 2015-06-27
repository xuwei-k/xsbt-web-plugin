servletSettings

libraryDependencies += "org.eclipse.jetty" % "jetty-webapp" % "9.2.11.v20150529" %
  (if(!(new File("jetty-conf") exists)) "container" else "test")

port in container.Configuration := 7121
