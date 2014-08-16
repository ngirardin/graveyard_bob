package com.protogenefactory.ioiomaster.server.services

import android.util.Log
import com.protogenefactory.ioiomaster.client.models.{Project, ServoConfig}
import com.protogenefactory.ioiomaster.server.services.ProjectLock._

object ProjectLock {

  /**
   * The min and maximum servo positions in ms
   */
  final val MIN = 1000 // us
  final val MAX = 2500 // us

  /**
   * The duration of one percent of the servo amplitude in ms
   */
  final val PERCENT = ((MAX - MIN).toFloat / 100f).intValue()

}

class ProjectLock(
  // The slice duration in ms
  period: Int
) {

  /**
   * The project being played
   */
  private var project : Project = null

  /**
   * The pins index for the project
   */
  var pinsIndexes: Seq[Int] = Seq()

  /**
   * The project duration in ms
   */
  var duration   : Int = 0

  /**
   * The project slices count
   */
  var sliceCount : Int = 0

  /**
   * The project slices, one for each period
   */
  private var slices: Seq[Seq[Int]] = null

  var currentSlice = 0

  def hasProject = project != null

  def setProject(p: Project) {
    project = p
    pinsIndexes = p.boardConfig.servoConfigs.map(c => ServoConfig.PERIPHERAL_PORTS.indexOf(c.pin))
    duration = project.steps.map(_.duration).sum
    slices = sliceSteps()
    sliceCount = slices(0).length
    //Log.i("*******", slices.mkString("\n"))
    currentSlice = 0

    /**
     * Convet a percentage position to its timing
     *
     * @param position the position in percentage
     * @return the timing in ms
     */
    def toTiming(position: Int) = {
      MIN + position * PERCENT
    }

    def sliceSteps(): Seq[Seq[Int]] = {


      Log.i("Bob", s"ProjectLock.sliceSteps() Pins  : ${project.boardConfig.servoConfigs.map(_.pin)}")

      Log.i("Bob", s"ProjectLock.sliceSteps() Steps :")
      project.steps.foreach(step =>
        Log.i("Bob", s"ProjectLock.sliceSteps()   $step -> ${step.positions.map(toTiming)}")
      )

      val slicedSteps = project.steps.zip(project.steps.tail).map { case (step, nextStep) =>

        // Compute the slices count from the duration
        val slices = step.duration / period

        //Log.i("----------", s"${step.duration}ms ($slices slices) for $step -> $nextStep")

        step.positions.zip(nextStep.positions).map { case (position, nextPosition) =>

          // Convert the start and end position to their timing
          val start = toTiming(position)
          val end = toTiming(nextPosition)

          // Compute the position delta between each slice
          val delta = (end - start).toFloat / slices

          // Compute the value for each slice
          (0 until slices).map { slice =>
            //Log.i("---", s"start ($start) + slice ($slice) * delta ($delta) = ${start + slice * delta}")
            (start + slice * delta).toInt
          }

        }

      }

      // Group the sliced steps by servos
      val flattenSlicesSteps = (0 until project.boardConfig.servoConfigs.length).map { i =>

        slicedSteps.zipWithIndex.map { case (step, si) =>
          step(i)
        }.flatten
        // Append the last position

      }.zipWithIndex.map { case (steps, servo) =>
        steps :+ toTiming(project.steps.last.positions(servo))
      }

      Log.i("Bob", s"ProjectLock.sliceSteps() Slices length par servo: ${flattenSlicesSteps.map(_.length)}")

      flattenSlicesSteps

    }
  }

  /**
   * Get the next slice. When at the last slice call #clear().
   *
   * @return the slice positions for all the servos
   */
  def getSlice: Seq[Int] = {

    //Log.i("Bob", s"ProjectLock.getSlice() $currentSlice/$sliceCount")

    if (currentSlice < sliceCount) {

      currentSlice = currentSlice + 1

      slices.map { servo =>
        servo(currentSlice - 1)
      }

    } else {
      // All slices read
      clear()
      Seq()
    }

  }

  private def clear() {
    project      = null
    duration     = 0
    sliceCount   = 0
    slices       = null
    currentSlice = 0
  }

}

