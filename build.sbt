name := "builder-server"

scalaVersion := "2.9.0-1"

// The := method used in Name and Version is one of two fundamental methods.
// The other method is <<=
// All other initialization methods are implemented in terms of these.
version := "0.1"

// Add multiple dependencies
libraryDependencies ++= Seq(
    "junit" % "junit" % "4.8.1" % "test",
    "org.mortbay.jetty" % "jetty-embedded" % "6.1.25",
    "commons-io" % "commons-io" % "2.0.1"
)


seq(oneJarSettings: _*)