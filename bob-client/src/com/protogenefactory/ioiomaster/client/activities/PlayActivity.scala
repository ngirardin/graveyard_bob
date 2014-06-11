package com.protogenefactory.ioiomaster.client.activities

import android.content.pm.ActivityInfo
import org.scaloid.common._

class PlayActivity extends SActivity {

  val projects = Seq("Hello short", "Hello long", "Open hand", "Close hand", "Head left", "Head right")

  val joysticks = Seq("Wheels", "Head", "Arm")

  val mp3s = Seq("Hello!", "Bye-bye!", "Hi, I'm Bob!")

  onCreate {

    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)

    contentView = new SLinearLayout {

      this += new SVerticalLayout {

        STextView("Sequences")

        projects.map(p =>
          SButton(p, toast(s"Play project $p"))
            .fill
        )
      }.<<.wrap.Weight(1.0f).>>

      this += new SVerticalLayout {

        STextView("Joysticks")

        joysticks.map(j =>
          SToggleButton(j)
            .textOff(j)
            .textOn(j)
            .fill
        )
      }.<<.wrap.Weight(1.0f).>>

      this += new SVerticalLayout {

        STextView("Sounds")

        mp3s.map(m =>
          SButton(m, toast(s"Play sound $m"))
            .fill
        )
      }.<<.wrap.Weight(1.0f).>>

      this += new SVerticalLayout {

        SButton("VIDEO")
          .<<(MATCH_PARENT, 100.dip).>>

        STextView("Touch to disable video")

      }.<<.wrap.Weight(2.0f).>>

    }

  }

}
