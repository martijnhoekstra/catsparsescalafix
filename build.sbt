lazy val V = _root_.scalafix.sbt.BuildInfo
inThisBuild(
  List(
    scalaVersion := V.scala212,
    crossScalaVersions := List(V.scala213, V.scala212, V.scala211),
    organization := "org.heroestool",
    homepage := Some(url("https://github.com/com/example")),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer(
        "martijnhoekstra",
        "Martijn Hoekstra",
        "martijnhoekstra@gmail.com",
        url("https://example.com")
      )
    ),
    addCompilerPlugin(scalafixSemanticdb),
    scalacOptions ++= List(
      "-Yrangepos",
      "-P:semanticdb:synthetics:on"
    )
  )
)

skip in publish := true

lazy val rules = project.settings(
  moduleName := "scalafix",
  libraryDependencies += "ch.epfl.scala" %% "scalafix-core" % V.scalafixVersion
)

lazy val input = project.settings(
  skip in publish := true,
  libraryDependencies += "org.typelevel" %% "cats-parse" % "0.2.0"
)

lazy val output = project.settings(
  skip in publish := true,
  libraryDependencies += "org.typelevel" %% "cats-parse" % "0.3-50-0eae46e-SNAPSHOT"
)

lazy val tests = project
  .settings(
    skip in publish := true,
    libraryDependencies += "ch.epfl.scala" % "scalafix-testkit" % V.scalafixVersion % Test cross CrossVersion.full,
    compile.in(Compile) := 
      compile.in(Compile).dependsOn(compile.in(input, Compile)).value,
    scalafixTestkitOutputSourceDirectories :=
      unmanagedSourceDirectories.in(output, Compile).value,
    scalafixTestkitInputSourceDirectories :=
      unmanagedSourceDirectories.in(input, Compile).value,
    scalafixTestkitInputClasspath :=
      fullClasspath.in(input, Compile).value,
  )
  .dependsOn(rules)
  .enablePlugins(ScalafixTestkitPlugin)
