package fr.dmconcept.bob.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import fr.dmconcept.bob.BobApplication;
import fr.dmconcept.bob.R;
import fr.dmconcept.bob.models.BoardConfig;
import fr.dmconcept.bob.models.Project;
import fr.dmconcept.bob.models.dao.ProjectsDao;

import java.util.List;

public class NewProjectActivity extends Activity {

    private static final String TAG = "activities.NewProjectActivity";

    private List<BoardConfig> mBoardConfigs;
    private RadioGroup mBoardConfigRadioGroup;
    private ProjectsDao mProjectsDao;
    private EditText mEditTextName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate()");

        setContentView(R.layout.activity_newproject);

        BobApplication app = (BobApplication)getApplication();

        mBoardConfigRadioGroup = (RadioGroup) findViewById(R.id.boardConfigRadioGroup);
        mEditTextName          = (EditText)findViewById(R.id.editTextName);
        mBoardConfigs          = app.getBoardConfigDao().findAll();
        mProjectsDao           = app.getProjectsDao();

        createBoardConfigRadios();

        findViewById(R.id.buttonSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String projectName = mEditTextName.getText().toString().trim();

                if (projectName.length() == 0){ // validation
                    mEditTextName.setError("Project name can't be empty");
                }else{
                    BoardConfig boardConfig = mBoardConfigs.get(mBoardConfigRadioGroup.getCheckedRadioButtonId()) ;
                    mProjectsDao.save(new Project(projectName, boardConfig));
                    finish();
                }
            }
        });
    }


    /**
     * Add a radio button for each available boardConfig into the radioGroup
     */
    private void createBoardConfigRadios() {

        for (int i = 0; i < mBoardConfigs.size(); i++) {

            BoardConfig boardConfig = mBoardConfigs.get(i);

            RadioButton radio = new RadioButton(this);
            radio.setId(i);
            radio.setText(boardConfig.getName());

            mBoardConfigRadioGroup.addView(radio);
        }

        // Check the first board config radio
        mBoardConfigRadioGroup.check(0);
    }
}


