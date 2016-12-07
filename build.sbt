scalaVersion := "2.11.8"

libraryDependencies += "com.typesafe.akka" %% "akka-persistence-cassandra" % "0.22"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-slf4j" % "2.4.11",
  "ch.qos.logback" % "logback-classic" % "1.1.3"
)

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)

// We use "extends" option in our proto file hence we need scalapb/scalapb.proto
libraryDependencies += "com.trueaccord.scalapb" %% "scalapb-runtime" % com.trueaccord.scalapb.compiler.Version.scalapbVersion % "protobuf"
