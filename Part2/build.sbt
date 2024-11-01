import scala.collection.Seq

ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.12.18"

lazy val root = (project in file("."))
  .settings(
    name := "Part2",

    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core" % "3.3.0",
      "org.apache.spark" %% "spark-mllib" % "3.3.0",
      "org.apache.spark" %% "spark-sql" % "3.3.0",
      // DL4J dependency
      "org.deeplearning4j" % "deeplearning4j-core" % "1.0.0-M2.1",
      "org.deeplearning4j" %% "dl4j-spark" % "1.0.0-M2.1",
      "org.deeplearning4j" % "deeplearning4j-nn" % "1.0.0-M2.1",
      "org.deeplearning4j" %% "dl4j-spark-parameterserver" % "1.0.0-M2.1",
      // ND4J dependencies
      "org.nd4j" % "nd4j-native" % "1.0.0-M2.1",
      "org.nd4j" % "nd4j-api" % "1.0.0-M2.1",
      "org.nd4j" % "nd4j-native-platform" % "1.0.0-M2.1",
      // Hadoop JAR
      "org.apache.hadoop" % "hadoop-client-api" % "3.3.6",
      "org.slf4j" % "slf4j-api" % "1.7.36"
    ),

//    assemblyMergeStrategy in assembly := {
//      case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.first
//      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
//      case x => MergeStrategy.first
//    }
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", xs @ _*) =>    xs match {
        case "MANIFEST.MF" :: Nil => MergeStrategy.discard
        case "services" :: _ => MergeStrategy.concat
        case _ => MergeStrategy.discard
      }
      case "reference.conf" => MergeStrategy.concat
      case x if x.endsWith(".proto") => MergeStrategy.rename
      case _ => MergeStrategy.first
    }
  )


