servletSettings

libraryDependencies += "org.eclipse.jetty" % "jetty-webapp" % "9.2.11.v20150529" % "container"
fullClasspath in Runtime in packageWar <+= baseDirectory.map(bd => bd / "extras")
port in container.Configuration := 7129
