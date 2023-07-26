package objektwerks

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.scaladsl._

import com.formdev.flatlaf.FlatLightLaf
import com.typesafe.config.ConfigFactory

import java.awt.{BorderLayout, EventQueue}

import javax.swing.{BorderFactory, JFrame, WindowConstants}
import javax.swing.UIManager

import org.jfree.chart.ChartPanel
import org.jfree.data.time.{TimeSeries, TimeSeriesDataItem}
import org.jfree.data.time.Millisecond

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

object ChartApp {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem.create("chart-app", ConfigFactory.load("app.conf"))
    implicit val dispatcher = system.dispatcher
    val timeSeries = new TimeSeries("Time")

    EventQueue.invokeLater(
      () => {
        UIManager.setLookAndFeel( new FlatLightLaf() )

        val chart = Chart(timeSeries)
        val chartPanel = new ChartPanel( chart.jFreeChart )
        chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15))

        val frame = new JFrame()
        frame.setTitle("Streaming Chart App")
        frame.setSize(900, 600)
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
        frame.setLocationRelativeTo(null)
        frame.add(chartPanel, BorderLayout.CENTER)
        frame.setVisible(true)
      }
    )

    def addOrUpdate(timeSeries: TimeSeries): Unit =
      timeSeries.addOrUpdate( new TimeSeriesDataItem( new Millisecond(), Random.nextDouble() ) )

    def asyncAddOrUpdate(timeSeries: TimeSeries): Runnable =
      () => timeSeries.addOrUpdate( new TimeSeriesDataItem( new Millisecond(), Random.nextDouble() ) )

    // 1. Update time series with akka stream.
    Source
      .tick(2 second, 2 second, ())
      .map( _ => addOrUpdate(timeSeries) )
      .runWith(Sink.ignore)

    // 2. Update time series with akka scheduler.
    system
      .scheduler
      .scheduleWithFixedDelay(4 seconds, 4 seconds)( asyncAddOrUpdate(timeSeries) )

    sys.addShutdownHook {
      system.terminate()
      println("*** App terminated.")
    }

    Thread.currentThread().join() // Prevents Akka System from terminating!
  }
}