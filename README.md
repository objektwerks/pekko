Pekko
-----
>Pekko feature tests using Scala 3.

Note
----
>The transition from Akka to Pekko is a relatively smooth process, principally
>requiring only package name changes and implicit-to-given conversions. The
>GraphDSL proved most challenging. Pekko-Http has ***not*** yet been pubished.

Test
----
1. sbt clean test

Run
---
1. sbt run
```
Multiple main classes detected. Select one to run:
 [1] objektwerks.runChartApp
 [2] objektwerks.runFileApp
 [3] objektwerks.runFutureApp
 [4] objektwerks.stream.runActorApp
 [5] objektwerks.typed.EventSourceApp
 [6] objektwerks.typed.PingFpApp
 [7] objektwerks.typed.PingOoApp
 [8] objektwerks.typed.runFactorialApp
 [9] objektwerks.typed.runGreeterApp
 [10] objektwerks.typed.runNowApp

Enter number:
```