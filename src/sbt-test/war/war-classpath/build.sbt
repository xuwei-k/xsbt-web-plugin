servletSettings

libraryDependencies += "org.eclipse.jetty" % "jetty-webapp" % "9.2.19.v20160908" % "container"
fullClasspath in Runtime in packageWar += (baseDirectory.value / "extras")
port in container.Configuration := 7129
