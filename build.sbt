name := "builder"

organization := "org.builder"

scalaVersion := "2.9.1"

// The := method used in Name and Version is one of two fundamental methods.
// The other method is <<=
// All other initialization methods are implemented in terms of these.
version := "0.2.0"

publishTo := Some("Triforks Nexus" at "http://nexus.ci82.trifork.com/content/repositories/trifork-internal/")

transitiveClassifiers := Seq("sources")

resolvers += {
  val triforkRepoUrl = new java.net.URL("http://nexus.ci82.trifork.com/content/groups/public/")
  Resolver.url("Trifork repository", triforkRepoUrl)
}

// Add multiple dependencies
libraryDependencies ++= Seq(
    "junit" % "junit" % "4.8.1" % "test",
    "org.mortbay.jetty" % "jetty-embedded" % "6.1.25",
    "commons-io" % "commons-io" % "2.0.1",
    "commons-codec" % "commons-codec" % "1.5",
    "org.scala-tools.testing" % "scalacheck_2.9.0" % "1.9",
    "com.novocode" % "junit-interface" % "0.7" % "test->default",
    //"org.tmatesoft.svnkit" % "svnkit" % "1.3.6-v1"
    "org.apache.httpcomponents" % "httpclient" % "4.1.2",
    "org.apache.httpcomponents" % "httpmime" % "4.0.1",
    "log4j" % "log4j" % "1.2.16"
)

seq(oneJarSettings: _*)

//mainClass in oneJar := Some("org.builder.Main")