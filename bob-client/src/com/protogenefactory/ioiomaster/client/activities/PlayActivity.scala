package com.protogenefactory.ioiomaster.client.activities

import android.view.Gravity
import com.protogenefactory.ioiomaster.client.BobApplication
import org.scaloid.common._

class PlayActivity extends SActivity {

  implicit override val loggerTag = LoggerTag("Bob")

  val joysticks = Seq("Wheels", "Head", "Arm")

  val mp3s = Seq("Hello!", "Bye-bye!", "Hi, I'm Bob!")

  lazy val application = getApplication.asInstanceOf[BobApplication]

  lazy val serverIP = application.serverIP

  lazy val projects = application.projectsDao.findAll()

  onCreate {

    info("PlayActivity.onCreate()")

    setTitle(s"$getTitle connected to $serverIP")

    contentView = new SLinearLayout {

      /**
       * Projects column
       */
      this += new SVerticalLayout {

        STextView("Sequences")
          .gravity(Gravity.CENTER)

        projects.map(p =>

          SButton(p.name, {
            toast(s"Play project ${p.name}")
          })
            .textSize(12.dip)

        )
      }.<<.wrap.Weight(1.0f).>>

      /**
       * Joysticks column
       */
      /*
      this += new SVerticalLayout {

        STextView("Joysticks")
          .gravity(Gravity.CENTER)

        joysticks.map(j =>
          SToggleButton(j, toast("TODO joysticks"))
            .textOff(j)
            .textOn(j)
            .fill
        )
      }.<<.wrap.Weight(1.0f).>>
      */

      /**
       * Sounds column
       */
      this += new SVerticalLayout {

        STextView("Sounds")
          .gravity(Gravity.CENTER)

        mp3s.map(m =>
          SButton(m, toast(s"Play sound $m"))
            .textSize(12.dip)
        )
      }.<<.wrap.Weight(1.0f).>>

      /**
       * Video column
       */
      this += new SVerticalLayout {

        SButton("VIDEO")
          .<<(MATCH_PARENT, 100.dip).>>

        STextView("Touch to disable video")

      }.<<.wrap.Weight(2.0f).>>

    }

  }

}
