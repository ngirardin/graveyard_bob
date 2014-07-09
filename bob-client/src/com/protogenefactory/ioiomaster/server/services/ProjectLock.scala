package com.protogenefactory.ioiomaster.server.services

import android.util.Log
import com.protogenefactory.ioiomaster.client.models.Project
import com.protogenefactory.ioiomaster.server.services.ProjectLock._

import scala.collection.mutable.ArrayBuffer

object ProjectLock {

  /**
   * The period duration in ms
   */
  final val PERIOD_MILLI = ServerService.PERIOD / 1000

  /**
   * @return a new empty ProjectLock
   */
  def empty: ProjectLock = new ProjectLock

}

class ProjectLock {

  /**
   * The project being played
   */
  private var project    : Project = null

  var duration   : Int = 0
  var sliceCount : Int = 0

  var slices: Seq[Seq[Int]] = null

  def setProject(p: Project) {
    project    = p
    duration   = projectDuration
    sliceCount = this.duration / PERIOD_MILLI
    slices     = sliceSteps()
  }

  private def projectDuration: Int =  {

    var duration = 0

    for (step <- project.steps)
      duration = duration + step.duration

    duration

  }

  private def sliceSteps() : Seq[Seq[Int]] = {

    val s = ArrayBuffer[Seq[Int]]()

    Log.i("Bob", s"ProjectLock.sliceSteps() Steps:")
    project.steps.foreach(step =>
      Log.i("Bob", s"ProjectLock.sliceSteps()   $step")
    )

    for (i <-  0 until project.steps.length - 1) {

      val step     = project.steps(i    )
      val nextStep = project.steps(i + 1)

      val stepSliceCount: Int = step.duration / PERIOD_MILLI

      Log.i("Bob", s"ProjectLock.sliceSteps() Step $i/${project.steps.length}: duration=${step.duration} ms, slices=$stepSliceCount")

      for (sliceIndex <- 0 to stepSliceCount) {

//        Log.i("~~~~~~~~~~~~~~~~", s"  Slice $sliceIndex/$stepSliceCount")

        //!!!!!
        /*
        val interpolatedPositions = (0 until BoardConfig.MAX_SERVOS).map { i =>
        */

        val interpolatedPositions = step.positions.zipWithIndex.map { case (initialPosition, positionIndex) =>

//          Log.i("~~~~~~~~~~~~~~~~", s"    Position $positionIndex/${step.positions.length}")

          /*
          val initialPosition = step.positions(positionIndex)
          */
          val endPosition     = nextStep.positions(positionIndex)

          // Interpolate the position
          val deltaPosition : Float = endPosition - initialPosition
          val deltaPerSlice : Float = deltaPosition / stepSliceCount

          //                    if (sliceIndex == 0)
          //                        Log.i("~~~~~~~~~~~~~~~~", "S" + positionIndex + " [" + initialPosition + " -> " + endPosition + "] delta: " + deltaPosition + ", deltaPerSlice: " + deltaPerSlice);

          val interpolatedPercentage : Float = initialPosition + deltaPerSlice * sliceIndex
          val interpolatedPosition   : Float = ServerService.MIN + interpolatedPercentage * ServerService.PERCENT

          /*
          interpolatedPositions.add(interpolatedPosition.intValue())
          */
          interpolatedPosition.intValue()

        }

        if (sliceIndex % 10 == 0)
          Log.i("Bob", s"ProjectLock.sliceSteps()   slice $sliceIndex: $interpolatedPositions")

        s.+=(interpolatedPositions)
      }

      Log.i("Bob", "ProjectLock.sliceSteps() ----------------------")

    }

    //TODO ********** check slice count difference!!!
    /*
    if (s.length != sliceCount)
      throw new RuntimeException("Expecting " + sliceCount + " slices but got " + s.length)
    */

    s.toSeq

  }

}


