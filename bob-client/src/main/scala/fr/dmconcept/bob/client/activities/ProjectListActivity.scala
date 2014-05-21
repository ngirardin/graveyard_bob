package fr.dmconcept.bob.client.activities

import android.view.ContextMenu.ContextMenuInfo
import android.view.{MenuItem, ContextMenu, ViewGroup, View}
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget._
import fr.dmconcept.bob.client.models.Project
import fr.dmconcept.bob.client.{R, BobApplication}
import java.util.UUID
import org.scaloid.common._
import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

class ProjectListActivity extends SActivity {

  implicit override val loggerTag = LoggerTag("BobClient")

  lazy val projectDao = getApplication.asInstanceOf[BobApplication].projectsDao

  lazy val list = new SListView {

    // Store the project as a mutable array to avoid UnsupportedOperationException
    val projects = ListBuffer[Project]()
    projects.appendAll(projectDao.findAll())

    // Set the projects adapter
    adapter = new ArrayAdapter[Project](context, android.R.layout.simple_list_item_2, android.R.id.text1, projects) {

      override def getView(position: Int, convertView: View, parent: ViewGroup): View = {

        val view = super.getView(position, convertView, parent)
        val project = getItem(position)

        view.findViewById(android.R.id.text1).asInstanceOf[TextView].setText(project.name)
        view.findViewById(android.R.id.text2).asInstanceOf[TextView].setText(project.boardConfig.name)

        view
      }
    }

    onItemClick { (parent: AdapterView[_], view: View, position: Int, id: Long) =>

      val project = adapter.asInstanceOf[ArrayAdapter[Project]].getItem(position)

      info(s"ProjectListActivity.list.onItemClick() Starting project activity for project ${project.id}")

      // Start the project activity by passing the project as intent
      startActivity(
        SIntent[ProjectActivity]
          .putExtra(ProjectActivity.Extras.PROJECT_ID, project)
      )

    }

    // Enable the context menu on the list
    registerForContextMenu(this)

  }

  onCreate {

    debug("ProjectListActivity.onCreate()")

    contentView = {
      list
    }

  }

  override def onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo) {

    super.onCreateContextMenu(menu, v, menuInfo)

    getMenuInflater.inflate(R.menu.project_list_context, menu)

    val info: AdapterContextMenuInfo = menuInfo.asInstanceOf[AdapterContextMenuInfo]

    val project: Project = v.asInstanceOf[ListView]
      .getItemAtPosition(info.position).asInstanceOf[Project]

    menu.setHeaderTitle(project.name)

  }

  lazy val adapter: ArrayAdapter[Project] = list.adapter.asInstanceOf[ArrayAdapter[Project]]

  def updateAdapter() {
    // Notify the adapter to update
    adapter.clear()
    adapter.addAll(projectDao.findAll())
  }

  override def onContextItemSelected(item: MenuItem): Boolean = {

    super.onContextItemSelected(item)


    val project = {
      val position = item.getMenuInfo.asInstanceOf[AdapterContextMenuInfo].position
      list.getItemAtPosition(position).asInstanceOf[Project]
    }

    item.getItemId match {

      case R.id.action_rename =>

        val ft = getFragmentManager.beginTransaction()

        // Remove any previous dialog
        val prev = getFragmentManager.findFragmentByTag("dialog")
        if (prev != null)
          ft.remove(prev)

        ft.addToBackStack(null)

        val newFragment = ProjectListRenameFragment.newInstance(project)
        newFragment.show(ft, "dialog")

      case R.id.action_clone =>

        val newProject = project.copy(
          id   = UUID.randomUUID().toString,
          name = s"Copy of ${project.name}"
        )

        info(s"onContextItemSelected() Clone project ${project.name} [${project.id} -> ${newProject.id}]")

        projectDao.create(newProject)

        updateAdapter()

      case R.id.action_delete =>

        new AlertDialogBuilder("Delete the project", "Are you sure to delete the project")
          .negativeButton(android.R.string.no)
          .positiveButton(android.R.string.yes, {
            projectDao.delete(project)
            updateAdapter()
            toast(s"${project.name} has been deleted")
          })
        .show()

    }

    // Menu consumed
    true

  }

  def renameProject(project: Project, newName: String) {

    info(s"onContextItemSelected() Rename project ${project.id} [${project.name} -> $newName]")

    projectDao.updateName(project, newName)

    toast("The project has been renamed")

    updateAdapter()

  }

}

  /*
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
  */
