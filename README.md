## sbt-servlet-plugin

An sbt plugin to build Servlet applications which is based on xsbt-web-plugin 0.9.1.
xsbt-web-plugin 0.9 is very suitable for our use case.
The version implementation allows to reload Scalate templates without restarting Servlet containers and Servlet invocation is faster than 1.x.

## Getting started 

### project/plugins.sbt

Add this sbt plugin to your `project/plugins.sbt`.

```scala
addSbtPlugin("org.skinny-framework" % "sbt-servlet-plugin" % "1.4.0.RC2")
```

### build.sbt

```scala
servletSettings
```

### project/Build.scala or build.sbt

Add Jetty dependencies into "container" scope.

```scala
libraryDependencies ++= Seq(
  "org.eclipse.jetty" % "jetty-webapp" % "9.1.0.v20131115" % "container",
  "org.eclipse.jetty" % "jetty-plus"   % "9.1.0.v20131115" % "container"
)
```


## License

the BSD 3-Clause license

