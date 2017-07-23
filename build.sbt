name := "crud-ex-backend-http4s"

version := "0.1"

scalaVersion := "2.12.2"

val http4sVersion = "0.15.13a"
val circeVersion = "0.6.1"
val doobieVersion = "0.4.1"

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,

  "org.http4s" %% "http4s-circe" % http4sVersion,
  // Optional for auto-derivation of JSON codecs
  "io.circe" %% "circe-generic" % circeVersion,
  // Optional for string interpolation to JSON model
  "io.circe" %% "circe-literal" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,

  "org.scala-stm" %% "scala-stm" % "0.8",
  "com.lihaoyi" %% "scalatags" % "0.6.5",


  "org.tpolecat" %% "doobie-core"  % doobieVersion,
  "org.tpolecat" %% "doobie-h2" % doobieVersion,

  "org.slf4j" % "slf4j-simple" % "1.7.21",

  "org.specs2" %% "specs2-core" % "3.8.6" % "test",
  "org.tpolecat" %% "doobie-specs2" % doobieVersion % "test",
  // "org.typelevel" %% "scalaz-specs2" % "0.5.0" % "test",
  "org.http4s" %% "http4s-testing" % http4sVersion % "test",
  "org.scalacheck" %% "scalacheck" % "1.13.4" % "test"
)
