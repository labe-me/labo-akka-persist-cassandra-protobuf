// import com.trueaccord.scalapb.{ScalaPbPlugin => PB}

scalaVersion := "2.11.8"

libraryDependencies += "com.typesafe.akka" %% "akka-persistence-cassandra" % "0.22"

libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % "2.4.11"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.3"


PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)

// If you need scalapb/scalapb.proto or anything from
// google/protobuf/*.proto
libraryDependencies += "com.trueaccord.scalapb" %% "scalapb-runtime" % com.trueaccord.scalapb.compiler.Version.scalapbVersion % "protobuf"

// fork in run := true
