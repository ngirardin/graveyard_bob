package fr.dmconcept.bob.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import fr.dmconcept.bob.BobApplication;
import fr.dmconcept.bob.R;
import fr.dmconcept.bob.models.BoardConfig;

import java.util.List;

public class BoardConfigActivity extends Activity {

    private List<BoardConfig> mBoardConfigs;

    private RadioGroup mBoardConfigRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mBoardConfigs = ((BobApplication) getApplication()).getBoardConfigDao().findAll();

        setContentView(R.layout.activity_boardconfig);

        mBoardConfigRadioGroup = (RadioGroup) findViewById(R.id.boardConfigRadioGroup);

        createBoardConfigRadios();

    }

    /**
     * Create the board config radios
     *
     */
    private void createBoardConfigRadios() {

        for (BoardConfig boardConfig : mBoardConfigs) {

            RadioButton radio = new RadioButton(this);

            radio.setTag(boardConfig.getId());

            radio.setText(boardConfig.getName());

            radio.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    //noinspection ConstantConditions
                    new AlertDialog.Builder(v.getContext())
                        .setMessage("Click on " + v.getTag())
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
                }
            });

            mBoardConfigRadioGroup.addView(radio);
        }

    }

}
