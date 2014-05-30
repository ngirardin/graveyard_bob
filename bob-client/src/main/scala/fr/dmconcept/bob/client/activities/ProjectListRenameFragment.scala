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

  lazy val project = getArguments.getSerializable(Extra.PROJECT).asInstanceOf[Project]

  override def onCreateDialog(savedInstanceState: Bundle): Dialog = {

    // Create the dialog view
    val view         : View = getActivity.getLayoutInflater.inflate(R.layout.fragment_projectlist_rename, null)
    val editTextName : EditText = view.findViewById(R.id.editTextName).asInstanceOf[EditText]

    editTextName.setText(project.name)

    new AlertDialog.Builder(getActivity)
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
    .setView(view)
    .create()

  }

}
