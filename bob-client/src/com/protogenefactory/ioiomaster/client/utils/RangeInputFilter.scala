package com.protogenefactory.ioiomaster.client.utils

import android.text.{Spanned, InputFilter}
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

      if (range contains input) {
        //Log.d("BobClient", s"RangeFilter - Value $input between ${range.start} and ${range.end}")
        return null
      }

    } catch {
      case e: Throwable => Log.d("BobClient", s"RangeFilter - Value $newVal is not an integer")
    }

    //Log.d("BobClient", s"RangeFilter - Value $newVal outside ${range.start} and ${range.end}")
    /* return */ ""

  }

}

object PercentageInputFilter extends RangeInputFilter {

  val range = 0 to 100

}

object StepDurationInputFilter extends RangeInputFilter {

  val range = 0 to Step.MAX_STEP_DURATION

}
