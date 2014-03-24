package fr.dmconcept.bob.client.activities.activities

import android.app.ListActivity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.{MenuItem, Menu, ViewGroup, View}
import android.widget.{TextView, ListView, ArrayAdapter}
import fr.dmconcept.bob.client.activities.{NewProjectActivity, BoardConfigActivity, ProjectActivity}
import fr.dmconcept.bob.client.models.Project
import fr.dmconcept.bob.client.{R, BobApplication}
import java.util

object ProjectListActivity {

  val TAG = "activities.ProjctListActivity"

  def log(message: String) = Log.i(TAG, message)

}

class ProjectListActivity extends ListActivity {

  var mApplication: BobApplication = null

  var mProjects: util.List[Project] = null

  override def onCreate(savedInstanceState: Bundle) {

    super.onCreate(savedInstanceState)

    mApplication = getApplication.asInstanceOf[BobApplication]

    // Get the project list from the DB
    mProjects = mApplication.getProjectsDao.findAll()

    // Show the Server IP Selection activity
    startActivity(new Intent(this, classOf[ServerIPSelectionActivity]))

    setListAdapter(new ArrayAdapter[Project](this, android.R.layout.simple_list_item_2, android.R.id.text1, mProjects) {

      override def getView(position: Int, convertView: View, parent: ViewGroup) : View = {

        val view: View = super.getView(position, convertView, parent)

        val project = getItem(position)

        view.findViewById(android.R.id.text1).asInstanceOf[TextView].setText(project.getName)
        view.findViewById(android.R.id.text2).asInstanceOf[TextView].setText(project.getBoardConfig.getName)

        view
      }

    })

  }

  override def onListItemClick(listView: ListView, view: View, position: Int, id: Long) {

    val projectId = mProjects.get(position).getId

    // Start the project details activity
    startActivity(
      new Intent(listView.getContext, classOf[ProjectActivity])
        .putExtra(ProjectActivity.EXTRA_PROJECT_ID, projectId)
    )

  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {

    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater.inflate(R.menu.project_list, menu)

    super.onCreateOptionsMenu(menu)

  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {

    item.getItemId match {

      case R.id.action_boardConfig =>
        // Start the "board config" activity
        startActivity(new Intent(this, classOf[BoardConfigActivity]))
        true

      case R.id.action_newProject =>
        // Start the "new project" activity
        startActivity(new Intent(this, classOf[NewProjectActivity]))
        true

      case _ => super.onOptionsItemSelected(item)

    }

  }



}
