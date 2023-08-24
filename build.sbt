name := "pekko"
version := "1.0"
scalaVersion := "3.3.1-RC6"
libraryDependencies ++= {
  val pekkoVersion = "1.0.1"
  Seq(
    "org.apache.pekko" %% "pekko-actor" % pekkoVersion,
    "org.apache.pekko" %% "pekko-http" % "1.0.0",
    "org.apache.pekko" %% "pekko-http-spray-json" % "1.0.0",
    "org.apache.pekko" %% "pekko-persistence" % pekkoVersion,
    "org.apache.pekko" %% "pekko-stream" % pekkoVersion,
    "org.apache.pekko" %% "pekko-actor-typed" % pekkoVersion,
    "org.apache.pekko" %% "pekko-persistence-typed" % pekkoVersion,
    "org.apache.pekko" %% "pekko-stream-typed" % pekkoVersion,
    "org.apache.pekko" %% "pekko-slf4j" % pekkoVersion,
    "io.spray" %% "spray-json" % "1.3.6",
    "org.jfree" % "jfreechart" % "1.5.4",
    "com.formdev" % "flatlaf" % "3.1.1",
    "ch.qos.logback" % "logback-classic" % "1.4.11",
    "org.iq80.leveldb" % "leveldb" % "0.12" % Test,
    "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8" % Test,
    "org.apache.pekko" %% "pekko-actor-testkit-typed" % pekkoVersion % Test,
    "org.apache.pekko" %% "pekko-http-testkit" % "1.0.0" % Test,
    "org.scalatest" %% "scalatest" % "3.2.16" % Test
  )
}
scalacOptions ++= Seq(
  "-Wunused:all"
)
