package fr.dmconcept.bob.client.activities

import org.scaloid.common._
import fr.dmconcept.bob.client.BobApplication

class BoardConfigActivity extends SActivity {

  implicit override val loggerTag = LoggerTag("BobClient")

  onCreate {

    debug("BoardConfigActivity.onCreate()")

    contentView = new SVerticalLayout {

      new SRadioGroup {

        // Create a radio for each board config
        getApplication.asInstanceOf[BobApplication].boardConfigDao.findAll().map { boardConfig =>
            new SRadioButton(
              boardConfig.name,
              toast(s"Board config $getTag selected")
            ).setTag(boardConfig.id)
        }

      }.fill

    }

    /*
    def showBoardConfig(i: Int) {

      debug("Selected")
      Log.i(TAG, "showBoardConfig(" + i + ")");

      BoardConfig boardConfig = mBoardConfigs.apply(i);

      // Clear the board config layout
      mBoardConfigDetails.removeAllViews();

      // Create the header
      // TODO don't recreate at each change
      LinearLayout headerLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.layout_boardconfig_details, mBoardConfigDetails);
      ((TextView) headerLayout.findViewById(R.id.textPort )).setText("Port #"      );
      ((TextView) headerLayout.findViewById(R.id.textServo)).setText("Servo #");
      ((TextView) headerLayout.findViewById(R.id.textStart)).setText("Start timing");
      ((TextView) headerLayout.findViewById(R.id.textEnd  )).setText("End timing"  );

      boardConfig.servoConfigs().foreach(new AbstractFunction1<ServoConfig, String>() {

          @Override
          public String apply(ServoConfig servoConfig) {

              // Inflate the board config details layout
              LinearLayout detailsLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.layout_boardconfig_details, null);

              ((TextView) detailsLayout.findViewById(R.id.textPort)).setText(String.valueOf(servoConfig.port()));
              ((TextView) detailsLayout.findViewById(R.id.textServo)).setText(String.valueOf(servoConfig.timings()._1()));
              ((TextView) detailsLayout.findViewById(R.id.textStart)).setText(String.valueOf(servoConfig.timings()._2()));

              mBoardConfigDetails.addView(detailsLayout);

              return "TODO";
          }

      });

    }
  */

  }

}

