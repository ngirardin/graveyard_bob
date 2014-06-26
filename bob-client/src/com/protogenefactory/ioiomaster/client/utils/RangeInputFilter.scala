package com.protogenefactory.ioiomaster.client.utils

import android.text.{InputFilter, Spanned}
import android.util.Log
import com.protogenefactory.ioiomaster.client.models.Step

trait RangeInputFilter extends InputFilter {

  val range: Range

  //TODO port to Scala
  override def filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence = {

    // Remove the string out of destination that is to be replaced
    var newVal = dest.toString.substring(0, dstart) + dest.toString.substring(dend, dest.toString.length())

    // Add the new string in
    newVal = newVal.substring(0, dstart) + source.toString + newVal.substring(dstart, newVal.length())

    try {

      val input: Int = Integer.parseInt(newVal)

      if (range contains input)
        return null

    } catch {
      case e: Throwable => Log.d("Bob", s"RangeInputFilter.filter() - Value $newVal is not an integer")
    }

    ""

  }

}

object PercentageInputFilter extends RangeInputFilter {

  val range = 0 to 100

}

object StepDurationInputFilter extends RangeInputFilter {

  val range = 0 to Step.MAX_STEP_DURATION

}
