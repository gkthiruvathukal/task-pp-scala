ThisBuild / scalaVersion := "3.3.5"

enablePlugins(AssemblyPlugin)

lazy val root = (project in file("."))
  .settings(
    name := "DistributedPi",
    version := "0.1.0",
    mainClass := Some("dpi.WorkSupplier"),
    assembly / assemblyJarName := "dpi.jar",
    assembly / test := {},
    libraryDependencies ++= Seq(
      "org.zeromq" % "jeromq" % "0.5.2",
      "com.lihaoyi" %% "upickle" % "3.1.3"
    )
  )
