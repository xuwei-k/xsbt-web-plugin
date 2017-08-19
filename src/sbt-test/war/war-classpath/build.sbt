enablePlugins(ServletPlugin)

ServletPlugin.projectSettings

libraryDependencies += "org.eclipse.jetty" % "jetty-webapp" % "9.2.19.v20160908" % "container"
fullClasspath in Runtime in packageWar += baseDirectory.map(bd => bd / "extras").value
port in ServletPlugin.container.Configuration := 7129
