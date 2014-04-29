package fr.dmconcept.bob.client.activities

import org.scaloid.common._
import fr.dmconcept.bob.client.BobApplication
import org.scaloid.common.LoggerTag
import android.widget.{ArrayAdapter, TextView}
import fr.dmconcept.bob.client.models.Project
import android.view.{ViewGroup, View}

class ProjectListActivity extends SActivity {

  implicit override val loggerTag = LoggerTag("BobClient")

  onCreate {

    debug("ProjectListActivity.onCreate *********************")

    // Get the projects
    val projects = getApplication.asInstanceOf[BobApplication].projectsDao.findAll().toArray

    contentView = {

      val list = new SListView {

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

      }

      list.onItemClick { (parent, view, position: Int, id: Long) =>

        // Start the project activity by passing the project as intent
        startActivity(
          SIntent[ProjectActivity]
            .putExtra(ProjectActivity.EXTRA_PROJECT_ID, projects(position))
        )
      }

    }

  }
}

  /*

    // Enable the context menu on the list
    registerForContextMenu(getListView)

    setListAdapter(new ArrayAdapter[Project](this, android.R.layout.simple_list_item_2, android.R.id.text1, projects) {

      override def getView(position: Int, convertView: View, parent: ViewGroup) : View = {

        val view: View = super.getView(position, convertView, parent)

        val project = getItem(position)

        view.findViewById(android.R.id.text1).asInstanceOf[TextView].setText(project.name)
        view.findViewById(android.R.id.text2).asInstanceOf[TextView].setText(project.boardConfig.name)

        view
      }

    })


  }

  override def onListItemClick(listView: ListView, view: View, position: Int, id: Long) {

    val project = getListAdapter.getItem(position).asInstanceOf[Project]

    // Start the project details activity
    startActivity(
      new Intent(listView.getContext, classOf[ProjectActivity])
        .putExtra(ProjectActivity.EXTRA_PROJECT_ID, project.id)
    )

  }

  override def onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo) {

    super.onCreateContextMenu(menu, v, menuInfo)

    getMenuInflater().inflate(R.menu.project_list_context, menu)

    val info    : AdapterContextMenuInfo = menuInfo.asInstanceOf[AdapterContextMenuInfo]

    val project : Project = v.asInstanceOf[ListView].getItemAtPosition(info.position).asInstanceOf[Project]

    menu.setHeaderTitle(project.getName())

  }

  override def onContextItemSelected(item: MenuItem): Boolean = {

    super.onContextItemSelected(item)

    item.getItemId match {

      case R.id.action_rename => {
        Toast.makeText(getBaseContext, "rename", Toast.LENGTH_LONG).show()
      }

      case R.id.action_clone => {
        Toast.makeText(getBaseContext, "clone", Toast.LENGTH_LONG).show()
      }

      //TODO implement deletion
      case R.id.action_delete => {

        new AlertDialog.Builder(this)
          .setTitle(project.getName)
          .setIcon(android.R.drawable.ic_menu_delete)
          .setMessage("Delete the project?\n\nThis operation can't be undone.")
          .setPositiveButton(android.R.string.yes, new OnClickListener {
            override def onClick(dialog: DialogInterface, which: Int) {
              val p = project
              mProjectDao.delete(p)
              Toast.makeText(getBaseContext, s"${p.getName} has been deleted", Toast.LENGTH_LONG).show()
            }
          })

          .setNegativeButton(android.R.string.no, new OnClickListener {
            override def onClick(dialog: DialogInterface, which: Int) = dialog.dismiss()
          })
          .show()
      }

    }

    def project: Project = {
      val position = item.getMenuInfo.asInstanceOf[AdapterContextMenuInfo].position
      getListAdapter.getItem(position).asInstanceOf[Project]
    }

    // Menu consummed
    return true

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
  */
