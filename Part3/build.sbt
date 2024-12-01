ThisBuild / version := "1.0"
ThisBuild / scalaVersion := "2.13.14"

lazy val scalapbVersion = "0.11.14"  // Ensure we use a consistent version

lazy val root = (project in file("."))
  .settings(
    name := "Part3",

    // General dependencies
    libraryDependencies ++= Seq(
      // AWS SDK dependencies
      "software.amazon.awssdk" % "lambda" % "2.20.66", // AWS Lambda SDK
      "software.amazon.awssdk" % "bedrock" % "2.25.27", // AWS Bedrock SDK
      "software.amazon.awssdk" % "aws-core" % "2.25.27", // Core AWS SDK

      // Akka dependencies
      "com.typesafe.akka" %% "akka-stream" % "2.8.6", // Akka Streams
      "com.typesafe.akka" %% "akka-http" % "10.2.7", // Akka HTTP
      "com.typesafe.akka" %% "akka-http-testkit" % "10.5.3" % Test, // Akka HTTP Testkit
      "com.typesafe.akka" %% "akka-http-spray-json" % "10.5.3", // Akka HTTP Spray JSON
      "io.spray" %% "spray-json" % "1.3.6", // JSON handling for Akka

      // Protobuf and gRPC dependencies
      "com.thesamet.scalapb" %% "compilerplugin" % scalapbVersion, // ScalaPB plugin
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapbVersion, // ScalaPB runtime
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapbVersion, // ScalaPB gRPC runtime
      "com.thesamet.scalapb" %% "scalapb-json4s" % "0.11.1", // ScalaPB JSON support

      // JSON processing libraries
      "com.typesafe.play" %% "play-json" % "2.10.6", // Play JSON library
      "io.circe" %% "circe-core" % "0.14.3", // Circe core
      "io.circe" %% "circe-parser" % "0.14.3", // Circe parser

      // Logging dependencies
      "org.slf4j" % "slf4j-api" % "2.0.16", // SLF4J API
      "org.slf4j" % "slf4j-simple" % "2.0.16", // SLF4J simple logging
      "ch.qos.logback" % "logback-classic" % "1.5.6", // Logback Classic

      // Configuration management
      "com.typesafe" % "config" % "1.4.3", // Typesafe Config

      // Miscellaneous utility libraries
      "org.apache.commons" % "commons-lang3" % "3.12.0", // Apache Commons Lang
      "com.typesafe.play" %% "play-json" % "2.10.0-RC1" // Play JSON (specific version)
    ),

    // Testing dependencies
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.19" % Test, // ScalaTest core
      "org.scalatestplus" %% "scalatestplus-mockito" % "1.0.0-M2" % Test, // ScalaTest + Mockito
      "org.mockito" %% "mockito-scala" % "1.17.37" % Test, // Mockito for Scala
      "com.typesafe.akka" %% "akka-http-testkit" % "10.2.9" % Test, // Akka HTTP Testkit
      "com.typesafe.akka" %% "akka-stream" % "2.6.18", // Akka Streams (test version)
      "org.mockito" %% "mockito-scala" % "1.16.46" % Test, // Another Mockito version
      "org.scalatest" %% "scalatest" % "3.2.12" % Test, // ScalaTest (another version)
      "org.scalatest" %% "scalatest" % "3.2.15" % Test,
      "com.typesafe.akka" %% "akka-testkit" % "2.8.6" % Test,// ScalaTest (yet another version)
      "io.github.ollama4j" % "ollama4j" % "1.0.79"
    ),

    // Dependency overrides (ensure compatibility)
    dependencyOverrides += "com.google.protobuf" % "protoc" % "3.21.7", // Ensure matching Protoc version

    // Resolvers
    resolvers ++= Seq(
      "Maven Central" at "https://repo1.maven.org/maven2/" // Default Maven Central resolver
    ),

    // Protobuf settings
    Compile / PB.protocVersion := "3.21.7", // Set Protoc version
    Compile / PB.targets := Seq(
      scalapb.gen(flatPackage = true) -> (Compile / sourceManaged).value / "protobuf" // Set Protobuf generation target
    ),

    // sbt-assembly settings for creating a fat JAR
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", xs @ _*) =>
        xs match {
          case "MANIFEST.MF" :: Nil => MergeStrategy.discard // Discard duplicate MANIFEST.MF
          case "services" :: _ => MergeStrategy.concat // Concatenate service files
          case _ => MergeStrategy.discard // Discard other META-INF files
        }
      case "reference.conf" => MergeStrategy.concat // Concatenate reference.conf
      case x if x.endsWith(".proto") => MergeStrategy.rename // Rename duplicate proto files
      case _ => MergeStrategy.first // Use the first file if no specific rule applies
    }
  )

// Enable the ProtocPlugin for ScalaPB
enablePlugins(ProtocPlugin)