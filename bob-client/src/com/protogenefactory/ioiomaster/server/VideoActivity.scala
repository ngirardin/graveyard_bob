package com.protogenefactory.ioiomaster.server

import net.majorkernelpanic.streaming.SessionBuilder
import net.majorkernelpanic.streaming.gl.SurfaceView
import net.majorkernelpanic.streaming.rtsp.RtspServer
import net.majorkernelpanic.streaming.video.VideoQuality
import org.scaloid.common._

class VideoActivity extends SActivity {

  implicit override val loggerTag = LoggerTag("Bob")

  lazy val mSurfaceView = new SurfaceView(this, null)

  onCreate {

    info("onCreate")

    //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    contentView = new SVerticalLayout {

      STextView("First").wrap

      this += mSurfaceView.wrap.>>

      STextView("Second").wrap

    }

    /*
    lazy val cb = new Session.Callback {

      override def onBitrareUpdate(bitrate: Long) {
        // Informs you of the bandwidth consumption of the streams
        info(s"-------- onBitrateUpdate(bitrate: $bitrate)")
      }

      override def onSessionError(reason: Int, streamType: Int, e: Exception) {
        // Might happen if the streaming at the requested resolution is not supported
        // or if the preview surface is not ready...
        // Check the Session class for a list of the possible errors.
        info(s"-------- onSessionError An error occured $e")
      }

      override def onSessionConfigured() {
        info(s"-------- onSessionConfigured Preview configured.");
        // Once the stream is configured, you can get a SDP formated session description
        // that you can send to the receiver of the stream.
        // For example, to receive the stream in VLC, store the session description in a .sdp file
        // and open it with VLC while streming.
        info(s"--------  session descriptor: ${mSession.getSessionDescription()}")
        mSession.start()
      }

      override def onSessionStarted() {
        info(s"S-------- Streaming session started.");
      }

      override def onSessionStopped() {
        info(s"-------- Streaming session stopped.");
      }

      override def onPreviewStarted() {
        info("-------- onPreviewStarted")
      }

    }
    */

    /*lazy val mSession =*/ SessionBuilder.getInstance()
//      .setCallback(cb)
      .setSurfaceView(mSurfaceView)
      .setPreviewOrientation(90)
      .setContext(getApplicationContext())
      .setAudioEncoder(SessionBuilder.AUDIO_NONE)
      .setVideoEncoder(SessionBuilder.VIDEO_H264)
      .setVideoQuality(new VideoQuality(640, 480, 30,4 * 500 * 1000))
//      .setVideoQuality(new VideoQuality(320, 240, 20,4 * 500 * 1000))
//      .build()

    /*
    mSurfaceView.getHolder().addCallback(new Callback {

      override def surfaceCreated(holder: SurfaceHolder) {
        info(s"-------- surfaceCreated")
        // Starts the preview of the Camera
        mSession.startPreview();
      }

      override def surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        info(s"-------- surfaceChanged")

      }

      override def surfaceDestroyed(holder: SurfaceHolder) {
        info(s"-------- surfaceDestroyed")
        // Stops the streaming session
        mSession.stop();
      }

    })
    */

    // Starts the RTSP server
    startService(SIntent[RtspServer])

  }

  onStop {

    info("onStop")

    // Stops the RTSP server
    stopService(SIntent[RtspServer])
  }

}
