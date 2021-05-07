import Dependencies._

lazy val root = (project in file("."))
  .settings(
    inThisBuild(
      List(
        organization := "zio",
        scalaVersion := "2.13.5",
        version := "0.1.2-SNAPSHOT"
      )
    ),
    name := "zio-entity"
  )
  .settings(noPublishSettings)

lazy val noPublishSettings = Seq(publish := (()), publishLocal := (()), publishArtifact := false)

val testDeps = Seq(
  "org.scalatest" %% "scalatest" % "3.1.4" % Test,
  "dev.zio" %% "zio-test" % zio % Test,
  "dev.zio" %% "zio-test-sbt" % zio % Test,
  "dev.zio" %% "zio-test-magnolia" % zio % Test
)

val allDeps = Seq(
  "dev.zio" %% "zio" % zio,
  "dev.zio" %% "zio-streams" % zio,
  "org.scodec" %% "scodec-bits" % "1.1.24",
  "org.scodec" %% "scodec-core" % "1.11.7",
  "io.suzaku" %% "boopickle" % "1.3.3",
  "io.github.kitlangton" %% "zio-magic" % "0.2.0"
) ++ testDeps

val postgresDeps = Seq(
  "org.postgresql" % "postgresql" % "42.2.8",
  "io.getquill" %% "quill-jdbc-zio" % "3.7.0"
)

val akkaDeps = Seq(
  "com.typesafe.akka" %% "akka-cluster-sharding" % "2.6.14",
  "com.typesafe.akka" %% "akka-cluster" % "2.6.14",
  "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"
) ++ testDeps

lazy val commonProtobufSettings = Seq(
  Compile / PB.targets := Seq(
    scalapb.gen() -> (Compile / sourceManaged).value / "scalapb"
  ),
  Compile / PB.protoSources := Seq(
    baseDirectory.value / "src/schemas/protobuf"
  )
)

def module(id: String, path: String, description: String): Project =
  Project(id, file(path))
    .settings(moduleName := id, name := description)
    .settings(testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"))

lazy val `core` = module("zio-entity-core", "core", "Core library")
  .settings(libraryDependencies ++= allDeps)

lazy val `postgres` = module("zio-entity-postgres", "postgres", "Postgres event sourcing stores")
  .dependsOn(`core`)
  .settings(libraryDependencies ++= postgresDeps)

lazy val `akka-runtime` = module("zio-entity-akkaruntime", "akka-runtime", "Akka runtime")
  .dependsOn(`core`)
  .settings(libraryDependencies ++= akkaDeps)
  .settings(commonProtobufSettings)

aggregateProjects(`core`, `akka-runtime`, `postgres`)

testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")