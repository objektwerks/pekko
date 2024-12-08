Pekko
-----
>Pekko feature tests using Swing, JFreeChart and Scala 3.

Note
----
>The transition from Akka to Pekko is a relatively smooth process, principally
>requiring only package name changes and implicit-to-given conversions.

>The GraphDSL proved problematic ( see StreamTest ). And Akka-Persistence fails
>to load the default journal plugin ( see PersistenceTest and test.conf ).

Test
----
1. sbt clean test

Run
---
1. sbt run
```
Multiple main classes detected. Select one to run:
 [1] objektwerks.http.NowApp
 [2] objektwerks.runChartApp
 [3] objektwerks.runFileApp
 [4] objektwerks.runFutureApp
 [5] objektwerks.stream.runActorApp
 [6] objektwerks.typed.EventSourceApp
 [7] objektwerks.typed.PingFpApp
 [8] objektwerks.typed.PingOoApp
 [9] objektwerks.typed.runFactorialApp
 [10] objektwerks.typed.runGreeterApp
 [11] objektwerks.typed.runNowApp

Enter number: 
```
