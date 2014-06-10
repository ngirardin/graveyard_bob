package fr.dmconcept.bob.server.ioio;

import android.util.Log
import fr.dmconcept.bob.server.activities.MainIOIOActivity

import java.util.ArrayList
import fr.dmconcept.bob.client.models.Project

class ProjectLock {

  var project: Project = null
  var duration : Int = 0
  var sliceCount : Int = 0
  var slices = new java.util.ArrayList[java.util.ArrayList[Int]]()

  def setProject(p: Project) {
    project    = p
    duration   = projectDuration
    sliceCount = this.duration / (MainIOIOActivity.PERIOD / 1000)
    slices     = sliceSteps()
  }

  private def projectDuration: Int =  {

    var duration = 0

    for (step <-  project.steps)
      duration = duration + step.duration

    duration

  }

  private def sliceSteps() : java.util.ArrayList[java.util.ArrayList[Int]] = {

    val s = new java.util.ArrayList[java.util.ArrayList[Int]]

    for (i <-  0 to (project.steps.length - 1)) {

      val step     = project.steps(i    )
      val nextStep = project.steps(i + 1)

      val stepSliceCount: Int = step.duration / (MainIOIOActivity.PERIOD / 1000)

      Log.i("~~~~~~~~~~~~~~~~", s"Step duration=${step.duration} ms, slices=$stepSliceCount")

      for (sliceIndex <- 0 to stepSliceCount) {

        val interpolatedPositions = new ArrayList[Int]()

        for (positionIndex <-  0 to step.positions.length) {

          val initialPosition = step.positions(positionIndex)
          val endPosition     = nextStep.positions(positionIndex)

          // Interpolate the position
          val deltaPosition : Float = endPosition - initialPosition
          val deltaPerSlice : Float = deltaPosition / stepSliceCount

          //                    if (sliceIndex == 0)
          //                        Log.i("~~~~~~~~~~~~~~~~", "S" + positionIndex + " [" + initialPosition + " -> " + endPosition + "] delta: " + deltaPosition + ", deltaPerSlice: " + deltaPerSlice);

          val interpolatedPercentage : Float = initialPosition + deltaPerSlice * sliceIndex
          val interpolatedPosition   : Float = MainIOIOActivity.MIN + interpolatedPercentage * MainIOIOActivity.PERCENT

          interpolatedPositions.add(interpolatedPosition.intValue())

        }

        if (sliceIndex % 10 == 0)
          Log.i("~~~~~~~~~~~~~~~~", s"slice $sliceIndex: $interpolatedPositions")

        s.add(interpolatedPositions)
      }

      Log.i("~~~~~~~~~~~~~~~~", "")

    }

    if (s.size() != sliceCount)
      throw new RuntimeException("Expecting " + sliceCount + " slices but got " + s.size())

    return s

  }

}


