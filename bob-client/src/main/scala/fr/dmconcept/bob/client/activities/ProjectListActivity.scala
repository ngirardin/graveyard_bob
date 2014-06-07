package fr.dmconcept.bob.client.activities

import android.view.ContextMenu.ContextMenuInfo
import android.view._
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget._
import fr.dmconcept.bob.client.BobApplication
import fr.dmconcept.bob.client.models.{BoardConfig, Project}
import java.util.UUID
import org.scaloid.common._
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

class ProjectListActivity extends SActivity {

  implicit override val loggerTag = LoggerTag("BobClient")

  lazy val projectDao = getApplication.asInstanceOf[BobApplication].projectsDao

  // Use Java list to avoid immutability issues
  lazy val projects = (ListBuffer[Project]() ++= projectDao.findAll).asJava

  lazy val projectAdapter = new ArrayAdapter[Project](this, android.R.layout.simple_list_item_2, android.R.id.text1, projects) {

    override def getView(position: Int, convertView: View, parent: ViewGroup): View = {

      val view = super.getView(position, convertView, parent)
      val project = getItem(position)

      view.findViewById(android.R.id.text1).asInstanceOf[TextView].setText(project.name)
      view.findViewById(android.R.id.text2).asInstanceOf[TextView].setText(project.boardConfig.name)

      view
    }
  }

  lazy val list = new SListView() {

    adapter = projectAdapter

    onItemClick { (parent: AdapterView[_], view: View, position: Int, id: Long) =>

      val project = adapter.asInstanceOf[ArrayAdapter[Project]].getItem(position)

      info(s"ProjectListActivity.list.onItemClick() Starting project activity for project ${project.id}")

      startProjectActivity(project)

    }

    // Enable the context menu on the list
    registerForContextMenu(this)

  }

  onCreate {

    info("ProjectListActivity.onCreate()")

    contentView = {
      list
    }

  }

  onResume {
    info("ProjectListActivity.onResume()")
    updateAdapter()
  }

  override def onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo) {

    super.onCreateContextMenu(menu, v, menuInfo)

    getMenuInflater.inflate(R.menu.project_list_context, menu)

    val info    = menuInfo.asInstanceOf[AdapterContextMenuInfo]
    val project = v.asInstanceOf[ListView].getItemAtPosition(info.position).asInstanceOf[Project]

    menu.setHeaderTitle(project.name)

  }

  private def updateAdapter() {
    info("ProjectListActivity.updateAdapter()")
    projectAdapter.clear()
    projectAdapter.addAll(projectDao.findAll().asJava)
  }

  override def onContextItemSelected(item: MenuItem): Boolean = {

    super.onContextItemSelected(item)

    implicit val project = {
      val position = item.getMenuInfo.asInstanceOf[AdapterContextMenuInfo].position
      list.getItemAtPosition(position).asInstanceOf[Project]
    }

    item.getItemId match {
      case R.id.action_rename => showContextRenameDialog
      case R.id.action_clone  => cloneProject
      case R.id.action_delete => showContextDeleteDialog
    }

    // Menu consumed
    true

  }

  private def showContextRenameDialog(implicit project: Project) {

    val ft = getFragmentManager.beginTransaction()

    // Remove any previous dialog
    val prev = getFragmentManager.findFragmentByTag("dialog")
    if (prev != null)
      ft.remove(prev)

    ft.addToBackStack(null)

    ProjectListRenameFragment.newInstance(project).show(ft, "dialog")

  }

  private def showContextDeleteDialog(implicit project: Project) {

    new AlertDialogBuilder("Delete the project", "Are you sure to delete the project")
      .negativeButton(android.R.string.no)
      .positiveButton(android.R.string.yes, {
      projectDao.delete(project)
      updateAdapter()
      toast(s"${project.name} has been deleted")
    })
      .show()

  }

  def cloneProject(implicit project: Project) {

    val newProject = project.copy(
      id = UUID.randomUUID().toString,
      name = s"Copy of ${project.name}"
    )

    info(s"onContextItemSelected() Clone project ${project.name} [${project.id} -> ${newProject.id}]")

    projectDao.create(newProject)

    updateAdapter()

  }

  def renameProject(implicit project: Project, newName: String) {

    info(s"onContextItemSelected() Rename project ${project.id} [${project.name} -> $newName]")

    projectDao.updateName(project, newName)

    toast("The project has been renamed")

    updateAdapter()

  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {

    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater.inflate(R.menu.project_list, menu)

    super.onCreateOptionsMenu(menu)

  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {

    item.getItemId match {

      /*
      case R.id.action_boardConfig =>
        // Start the "board config" activity
        startActivity(new Intent(this, classOf[BoardConfigActivity]))
        true
        */

      case R.id.action_newProject =>
        startActivity(SIntent[NewProjectActivity])
        true

      case _ =>
        super.onOptionsItemSelected(item)

    }

  }

  def newProject(name: String, boardConfig: BoardConfig) {

    info(s"ProjectListActivity.newProject($name, ${boardConfig.name}})")

    val project = Project(name, boardConfig)

    projectDao.create(project)

    startProjectActivity(project)

  }

  private def startProjectActivity(project: Project) {
    startActivity (
      SIntent[ProjectActivity].putExtra(ProjectActivity.Extras.PROJECT_ID, project.id)
    )
  }

}
