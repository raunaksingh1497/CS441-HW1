import sbt.*
import sbt.Keys.*
import sbtassembly.AssemblyPlugin.autoImport._

ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.12"

lazy val root = (project in file("."))
  .settings(
    name := "Part1",

    // Adding dependencies
    libraryDependencies ++= Seq(
      // Hadoop dependencies
      "org.apache.hadoop" % "hadoop-common" % "3.3.6" exclude("org.slf4j", "slf4j-reload4j"),
      "org.apache.hadoop" % "hadoop-hdfs" % "3.3.6" exclude("org.slf4j", "slf4j-reload4j"),
      "org.apache.hadoop" % "hadoop-mapreduce-client-core" % "3.3.6" exclude("org.slf4j", "slf4j-reload4j"),
      "org.apache.hadoop" % "hadoop-mapreduce-client-common" % "3.3.6" exclude("org.slf4j", "slf4j-reload4j"),
      "org.apache.hadoop" % "hadoop-mapreduce-client-jobclient" % "3.3.6" exclude("org.slf4j", "slf4j-reload4j"),
      "org.apache.hadoop" % "hadoop-yarn-common" % "3.3.6" exclude("org.slf4j", "slf4j-reload4j"),

      // Deeplearning4j and ND4J dependencies
      "org.deeplearning4j" % "deeplearning4j-nlp" % "1.0.0-M2.1",
      "org.deeplearning4j" % "deeplearning4j-core" % "1.0.0-M2.1",
      "org.nd4j" % "nd4j-native-platform" % "1.0.0-M2.1",
      "org.nd4j" % "nd4j-native" % "1.0.0-M2.1",
      "org.bytedeco" % "openblas" % "0.3.6-1.5.1",
      "org.bytedeco" % "openblas-platform" % "0.3.6-1.5.1",

      // OpenCSV for CSV parsing
      "com.opencsv" % "opencsv" % "5.7.1",

      // Tokenization utilities
      "com.knuddels" % "jtokkit" % "1.1.0",

      // JSON handling
      "com.typesafe.play" %% "play-json" % "2.9.2",

      "org.apache.hadoop" % "hadoop-mapreduce-client-core" % "3.3.6" % Test
    ),

    // sbt-assembly settings for creating a fat jar
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", xs @ _*) =>
        xs match {
          case "MANIFEST.MF" :: Nil => MergeStrategy.discard
          case "services" :: _      => MergeStrategy.concat
          case _                    => MergeStrategy.discard
        }
      case "reference.conf" => MergeStrategy.concat
      case x if x.endsWith(".proto") => MergeStrategy.discard
      case x if x.contains("hadoop") => MergeStrategy.first
      case _ => MergeStrategy.first
    },
    // Include Scala dependencies in the assembly jar
    assembly / assemblyOption := (assembly / assemblyOption).value.copy(includeScala = true)
  )