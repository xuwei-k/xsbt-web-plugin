servletSettings

name := "war-contents"
version := "1.2.3"
libraryDependencies += "org.eclipse.jetty" % "jetty-webapp" % "9.2.19.v20160908" % "container"
port in container.Configuration := 7130
