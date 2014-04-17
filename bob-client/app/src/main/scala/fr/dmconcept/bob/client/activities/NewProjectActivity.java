package fr.dmconcept.bob.client.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import fr.dmconcept.bob.client.BobApplication;
import fr.dmconcept.bob.client.R;
import fr.dmconcept.bob.client.models.BoardConfig;
import fr.dmconcept.bob.client.models.Project;
import fr.dmconcept.bob.client.models.dao.ProjectDao;
import scala.collection.immutable.Vector;

public class NewProjectActivity extends Activity {

    private static final String TAG = "activities.NewProjectActivity";

    private Vector<BoardConfig> mBoardConfigs;
    private RadioGroup mBoardConfigRadioGroup;
    private ProjectDao mProjectsDao;
    private EditText mEditTextName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate()");

        setContentView(R.layout.activity_newproject);

        BobApplication app = (BobApplication)getApplication();

        mBoardConfigRadioGroup = (RadioGroup) findViewById(R.id.boardConfigRadioGroup);
        mEditTextName          = (EditText)findViewById(R.id.editTextName);
        mBoardConfigs          = app.mBoardConfigDao().findAll();
        mProjectsDao           = app.mProjectsDao();

        createBoardConfigRadios();

        findViewById(R.id.buttonSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String projectName = mEditTextName.getText().toString().trim();

                if (projectName.length() == 0) {
                    mEditTextName.setError("Project name can't be empty");
                    return;
                }

                // Save the new project
                BoardConfig boardConfig = mBoardConfigs.apply(mBoardConfigRadioGroup.getCheckedRadioButtonId()) ;
                long projectId = mProjectsDao.create(Project.apply(projectName, boardConfig));

                // Start the project activity
                Intent intent = new Intent(v.getContext(), ProjectActivity.class);
                intent.putExtra(ProjectActivity.EXTRA_PROJECT_ID(), projectId);
                startActivity(intent);

            }
        });

    }


    /**
     * Add a radio button for each available boardConfig into the radioGroup
     */
    private void createBoardConfigRadios() {

        for (int i = 0; i < mBoardConfigs.size(); i++) {

            BoardConfig boardConfig = mBoardConfigs.apply(i);

            RadioButton radio = new RadioButton(this);
            radio.setId(i);
            radio.setText(boardConfig.name());

            mBoardConfigRadioGroup.addView(radio);
        }

        // Check the first board config radio
        mBoardConfigRadioGroup.check(0);
    }
}


