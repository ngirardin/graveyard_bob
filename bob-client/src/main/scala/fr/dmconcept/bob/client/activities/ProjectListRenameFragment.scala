package fr.dmconcept.bob.client.activities

import android.app.{AlertDialog, Dialog, DialogFragment}
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.os.Bundle
import android.view.View
import android.widget.EditText
import fr.dmconcept.bob.client.R
import fr.dmconcept.bob.client.activities.ProjectListRenameFragment.Extra
import fr.dmconcept.bob.client.models.Project

object ProjectListRenameFragment {

  object Extra {
    final val PROJECT = "project"
  }

  //TODO Use inter-fragment communication
  def newInstance(project: Project): ProjectListRenameFragment = {

    val fragment = new ProjectListRenameFragment()

    val bundle = new Bundle()
    bundle.putSerializable(Extra.PROJECT, project)

    fragment.setArguments(bundle)

    fragment

  }

}

class ProjectListRenameFragment extends DialogFragment {

  //TODO use lazy val
  var project: Project = null

  override def onCreate(savedInstanceState: Bundle): Unit = {

    super.onCreate(savedInstanceState)

    project = getArguments.getSerializable(Extra.PROJECT).asInstanceOf[Project]

    /*
    info(s"onContextItemSelected() Rename project ${project.id} [${project.name} -> ${newName}]")

    projectDao.updateName(project, newName)

    updateAdapter
    */

  }

  /*
      adapter.clear()
      adapter.addAll(projectDao.findAll())
   */

  override def onCreateDialog(savedInstanceState: Bundle): Dialog = {

    // Create the dialog view
    val v  : View = getActivity.getLayoutInflater.inflate(R.layout.fragment_projectlist_rename, null)
    val editTextName : EditText = v.findViewById(R.id.editTextName).asInstanceOf[EditText]

    editTextName.setText(project.name)

    val builder = new AlertDialog.Builder(getActivity)
      .setTitle("Rename the project")
      .setNegativeButton(android.R.string.cancel, new OnClickListener {
        override def onClick(dialog: DialogInterface, which: Int) {
          dialog.dismiss()
        }
      })
      .setPositiveButton(android.R.string.ok, new OnClickListener {
        override def onClick(dialog: DialogInterface, which: Int) {
          //TODO add check for non empty
          val newName = editTextName.getText.toString
          getActivity.asInstanceOf[ProjectListActivity].renameProject(project, newName)
        }
      })

    builder.setView(v)

    builder.create()
  }

}
