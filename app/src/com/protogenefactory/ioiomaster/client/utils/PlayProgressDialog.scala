package com.protogenefactory.ioiomaster.client.utils

import android.app.ProgressDialog
import android.content.Context
import android.os.SystemClock
import com.protogenefactory.ioiomaster.client.models.Project

object PlayProgressDialog {

  def show(context: Context, project: Project) {

    val progress = new android.app.ProgressDialog(context)
    progress.setTitle(project.name)
    progress.setMessage("Playing the project...")
    progress.setMax(project.duration / 1000)
    progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
    progress.setCancelable(false)
    progress.show()

    val start = System.currentTimeMillis()

    new java.util.Timer().schedule(new java.util.TimerTask() {
      override def run() {

        val elapsed = System.currentTimeMillis() - start

        if (elapsed < project.duration)
          progress.setProgress(elapsed.toInt / 1000)
        else
          progress.dismiss()
      }
    }, 0, 200)

  }

}
