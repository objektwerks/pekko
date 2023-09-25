package objektwerks.stream

import com.typesafe.config.ConfigFactory

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.*
import org.apache.pekko.stream.scaladsl.*

import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Await
import scala.concurrent.duration.*
import scala.language.postfixOps
import scala.concurrent.ExecutionContextExecutor

final class StreamTest extends AnyFunSuite with BeforeAndAfterAll with Matchers:
  given system: ActorSystem = ActorSystem.create("streams", ConfigFactory.load("test.conf"))
  given dispatcher: ExecutionContextExecutor = system.dispatcher

  override protected def afterAll(): Unit =
    Await.result(system.terminate(), 3 seconds)
    ()

  test("source"):
    val source = Source(1 to 10)
    Await.result( source.runFold(0)(_ + _), 1 second ) shouldBe 55
    Await.result( source.runReduce(_ + _), 1 second ) shouldBe 55

  test("source ~ sink"):
    val source = Source(1 to 10)
    val sink = Sink.fold[Int, Int](0)(_ + _)
    Await.result( source.toMat(sink)(Keep.right).run(), 1 second ) shouldBe 55
    Await.result( source.runWith(sink), 1 second ) shouldBe 55

  test("source ~ flow ~ sink"):
    val source = Source(1 to 10)
    val flow = Flow[Int].filter(_ % 2 == 0).map(_ * 2)
    val sink = Sink.fold[Int, Int](0)(_ + _)
    Await.result( source.via(flow).toMat(sink)(Keep.right).run(), 1 second ) shouldBe 60
    Await.result( source.via(flow).runWith(sink), 1 second ) shouldBe 60
    Await.result( flow.runWith(source, sink)._2, 1 second ) shouldBe 60

  test("source graph"):
    val sourceGraph = Source.fromGraph(
      GraphDSL.create() { implicit builder =>
        import GraphDSL.Implicits._

        val source1 = Source(1 to 10)
        val source2 = Source(1 to 10)

        val merge = builder.add( ZipWith( (a: Int, b: Int) => { a + b } ) )

        source1 ~> merge.in0
        source2 ~> merge.in1

        SourceShape(merge.out)
      }
    )
    val sink = Sink.reduce[Int](_ + _)
    Await.result( sourceGraph.runWith(sink), 1 second ) shouldBe 110

  test("flow graph"):
    val flowGraph = Flow.fromGraph(
      GraphDSL.create() { implicit builder =>
        import GraphDSL.Implicits._

        val incrementer = Flow[Int].map(_ + 1)
        val multiplier = Flow[Int].map(_ * 2)

        val incrementerShape = builder.add(incrementer)
        val multiplierShape = builder.add(multiplier)
        incrementerShape ~> multiplierShape

        FlowShape(incrementerShape.in, multiplierShape.out)
      }
    )
    val source = Source(1 to 10)
    val sink = Sink.reduce[Int](_ + _)
    Await.result( source.via(flowGraph).toMat(sink)(Keep.right).run(), 1 second ) shouldBe 130

/* See embedded comments!
  test("graph"):
    val source = Source(1 to 10)
    val incrementer = Flow[Int].map(_ + 1)
    val multiplier = Flow[Int].map(_ * 2)
    val sink = Sink.reduce[(Int, Int)]( (a, b) => (a._1 + a._2, b._1 + b._2) )

    val graph = RunnableGraph.fromGraph(
      GraphDSL.create(sink) { implicit builder => sink =>
        import GraphDSL.Implicits._

        val broadcast = builder.add(Broadcast[Int](2))
        val zip = builder.add(Zip[Int, Int]())

        source ~> broadcast
        broadcast.out(0) ~> incrementer ~> zip.in0
        broadcast.out(1) ~> multiplier ~> zip.in1
        // Code breaks here: zip.out ~> sink

        ClosedShape
      }
    )
    Await.result( graph.run(), 1 second ) shouldEqual( (144,31) )

  test("sink graph"):
    val sink1 = Sink.reduce[Int](_ + _)
    val sink2 = Sink.reduce[Int](_ + _)

    val sinkGraph = Sink.fromGraph(
      GraphDSL.create(sink1, sink2)((_, _)) { implicit builder => (sink1, sink2) =>
        import GraphDSL.Implicits._

        val broadcast = builder.add(Broadcast[Int](2))
        // Code breaks here: broadcast ~> sink1
        // Code breaks here: broadcast ~> sink2

        SinkShape(broadcast.in)
      }
    )
    val source = Source(1 to 10)
    val futures = source.runWith(sinkGraph)
    Await.result( Future.sequence(List(futures._1, futures._2)), 1 second ).sum shouldBe 110
*/
