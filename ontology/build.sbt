name := """ontology"""
organization := "edu.ncsu"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.12.6"

libraryDependencies += guice
libraryDependencies += "com.github.galigator.openllet" % "openllet-jena" % "2.6.4"
libraryDependencies += javaJdbc
libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.23.1"
resolvers += "public-jboss" at "http://repository.jboss.org/nexus/content/groups/public-jboss/"
libraryDependencies ++= Seq(
 "org.drools" % "drools-core" % "7.3.0.Final",
 "org.drools" % "drools-compiler" % "7.3.0.Final"
)