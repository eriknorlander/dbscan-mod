name := "DBSCAN"

version := "1.0"

scalaVersion := "2.11.8"

scalacOptions += "-target:jvm-1.8"

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.2.1"

libraryDependencies += "org.apache.spark" %% "spark-core" % "2.2.1" % "provided"

libraryDependencies += "org.apache.spark" %% "spark-mllib" % "2.2.1"

libraryDependencies += "org.apache.hadoop" % "hadoop-client" % "2.7.3"

libraryDependencies += "io.netty" % "netty" % "3.7.0.Final"

assemblyMergeStrategy in assembly := {
 case PathList("META-INF", xs @ _*) => MergeStrategy.discard
 case x => MergeStrategy.first
}
