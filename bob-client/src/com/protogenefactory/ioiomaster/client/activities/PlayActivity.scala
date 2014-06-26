package com.protogenefactory.ioiomaster.client.activities

import android.support.v4.widget.DrawerLayout
import android.view.{Gravity, View}
import android.widget.AdapterView.OnItemClickListener
import android.widget.{AdapterView, ArrayAdapter, FrameLayout, ListView}
import com.protogenefactory.ioiomaster.R
import com.protogenefactory.ioiomaster.client.BobApplication
import com.protogenefactory.ioiomaster.client.models.Project
import com.protogenefactory.ioiomaster.client.utils.PlayProgressDialog
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

    val drawerLayout = layoutInflater.inflate(R.layout.drawerlayout, null).asInstanceOf[DrawerLayout]

    val contentFrame = drawerLayout.find[FrameLayout](R.id.content_frame).addView(

      new SLinearLayout {

        /**
         * Projects column
         */
        this += new SVerticalLayout {

          STextView("Sequences")
            .gravity(Gravity.CENTER)

          projects.foreach(p =>

            this += new SButton(p.name) {
              onClick({
                val project = tag.asInstanceOf[Project]
                PlayProgressDialog.show(context, project)
                application.connection.playProject(project)
              })
            }
            .tag(p)
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

    )

    val leftDrawer   = drawerLayout.find[ListView](R.id.left_drawer)

    leftDrawer.setAdapter(
      new ArrayAdapter[String](this, R.layout.drawer_list_item, Array("Home", "Projects", "News", "Special offers"))
    )

    leftDrawer.setOnItemClickListener(new OnItemClickListener {
      override def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long) {
        position match {
          // case 0 /* Home */ => do nothing
          case 1 /* Project */ => startActivity[ProjectListActivity]
          case 2 /* News    */ => startActivity[NewsActivity       ]
          case 3 /* Offers  */ => startActivity[OffersActivity     ]
          case _ =>
        }
      }
    })

    contentView = drawerLayout

  }

  private def createNavigationDrawer() {

    val mPlanetTitles = Array("one", "two", "three", "four")
    val mDrawerLayout = find[DrawerLayout](R.id.drawer_layout)
    val mDrawerList = find[ListView](R.id.left_drawer)

    // Set the adapter for the list view
    mDrawerList.setAdapter(new ArrayAdapter[String](this, R.layout.drawer_list_item, mPlanetTitles))
    // Set the list's click listener
    mDrawerList.setOnItemClickListener(new OnItemClickListener {
      override def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long) {
        toast("Click on $position")
      }
    })

  }

}
