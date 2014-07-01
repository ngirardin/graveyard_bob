package com.protogenefactory.ioiomaster.client.activities

import java.util.{Timer, TimerTask}

import android.media.MediaPlayer
import android.media.MediaPlayer.{OnErrorListener, OnPreparedListener}
import android.support.v4.widget.DrawerLayout
import android.view.ViewGroup.LayoutParams
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

  final val BUTTON_TEXT_SIZE = /* tablet 20 */ 16/* phone */
  final val HEADER_PADDING   = 8
  final val VIDEO_WIDTH      = /* tablet 320 */ 240

  val joysticks = Seq("Wheels", "Head", "Arm")

  val mp3s = Seq("Hello!", "Bye-bye!", "Hi, I'm Bob!")

  lazy val application = getApplication.asInstanceOf[BobApplication]

  lazy val buttonsJoystick: Seq[SToggleButton] = joysticks.map(j => new SToggleButton(j) {
    /*
    onClick({t: SToggleButton =>
      buttonsJoystick.foreach(b => b.checked(b == t))
    })
    */
    textOff(j)
    textOn(j)
  })

  lazy val videoView       = new SVideoView()
  lazy val videoViewStatus = new STextView("Connecting...")

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
        this += new SScrollView {
          new SVerticalLayout {

            STextView("Sequences")
              .gravity(Gravity.CENTER)
              .padding(0, HEADER_PADDING, 0, HEADER_PADDING)

            projects.foreach(p =>

              this += new SButton(p.name) {
                onClick({
                  val project = tag.asInstanceOf[Project]
                  PlayProgressDialog.show(context, project)
                  application.connection.playProject(project)
                })
              }
                .tag(p)
                .textSize(BUTTON_TEXT_SIZE.sp)

            )
          }.<<.wrap.Weight(1.0f).>>
        }

        /**
         * Joysticks column
         */
        this += new SVerticalLayout {

          STextView("Joysticks")
            .gravity(Gravity.CENTER)
            .padding(0, HEADER_PADDING, 0, HEADER_PADDING)

          buttonsJoystick.foreach(t =>
            this += t
              .textSize(BUTTON_TEXT_SIZE.sp)
              .fill.>>
          )

        }.<<.wrap.Weight(1.0f).>>

        /**
         * Sounds column
         */
        this += new SScrollView {
          new SVerticalLayout {

            STextView("Sounds")
              .gravity(Gravity.CENTER)
              .padding(0, HEADER_PADDING, 0, HEADER_PADDING)

            mp3s.map(m =>
              SButton(m, toast(s"Play sound $m"))
                .textSize(BUTTON_TEXT_SIZE.sp)
            )
          }.<<.wrap.Weight(1.0f).>>
        }

        /**
         * Video column
         */
        this += new SScrollView {
          new SVerticalLayout {

            this += videoViewStatus
              .padding(0, HEADER_PADDING, 0, HEADER_PADDING)
              .gravity(Gravity.CENTER_HORIZONTAL)
              .wrap.>>

            this += videoView
              .<<(VIDEO_WIDTH.dip, LayoutParams.WRAP_CONTENT).>>

          }
            .gravity(Gravity.CENTER_HORIZONTAL)
            .<<(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT).Weight(2.0f).>>
        }

      }

    )

    val leftDrawer = drawerLayout.find[ListView](R.id.left_drawer)

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

    if (application.connection.hasVideo) {

      // Display the remote stream if any
      def whatToString(what: Int) = what match {
        case MediaPlayer.MEDIA_INFO_UNKNOWN               => "Unknown error"
        case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING   => "Video track lagging"
        case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START => "Rendering start"
        case MediaPlayer.MEDIA_INFO_BUFFERING_START       => "Buffering start"
        case MediaPlayer.MEDIA_INFO_BUFFERING_END         => "Buffering end"
        case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING      => "Bad interleaving"
        case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE          => "Not seekable"
        case MediaPlayer.MEDIA_INFO_METADATA_UPDATE => "Metadata update"
        case MediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE => "Unsupported subtitle"
        case MediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT => "Timed out"
      }

      videoView.setOnErrorListener(new OnErrorListener {
        override def onError(mp: MediaPlayer, what: Int, extra: Int): Boolean = {
          val whatString = whatToString(what)
          videoViewStatus.text(s"Streaming error: $whatString")
          toast(s"Player error: what=$whatString, extra=$extra")
          error(s"PlayActivity.videoView.onInfoListener() what=$whatString, extra=$extra")
          true // The method has handled the error
        }
      })

      /*
      videoView.setOnInfoListener(new OnInfoListener {
        override def onInfo(mp: MediaPlayer, what: Int, extra: Int): Boolean = {
          toast(s"Play info: what=$what, extra=$extra")
          info(s"PlayActivity.videoView.onInfoListener() what=$what, extra=$extra")
          true // The method has handled the info
        }
      })
      */

      videoView.setOnPreparedListener(new OnPreparedListener {
        override def onPrepared(mp: MediaPlayer) {
          //TODO remove
          toast("Starting streaming")

          new Timer().schedule(new TimerTask() {
            override def run() {
              info(s"-------------------------- Position: ${videoView.getCurrentPosition}/${videoView.getDuration} [${videoView.getBufferPercentage}%]")
            }
          }, 0, 200)

          videoViewStatus.text("Remote video")
          info(s"PlayActivity.videoView.onPreparedListener() Ready for playback")
        }
      })


        videoView.setVideoURI(s"rtsp://${application.serverIP}:8086/")
//      videoView.setVideoURI("rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov")
      //    videoView.setVideoURI("/sdcard/DCIM/Camera/VID_20140630_110902.mp4")

      videoView.start()

    } else {
      videoViewStatus.text("Remote video only available\nduring remote connection")
    }

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
