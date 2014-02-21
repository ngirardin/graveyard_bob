package fr.dmconcept.bob.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import fr.dmconcept.bob.R;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

/**
 * This is the main activity of the HelloIOIO example application.
 *
 * It displays a toggle button on the screen, which enables control of the
 * on-board LED. This example shows a very simple usage of the IOIO, by using
 * the {@link IOIOActivity} class. For a more advanced use case, see the
 * HelloIOIOPower example.
 */
public class XoxoActivity extends IOIOActivity {

    static final String TAG = "activities.XoxoActivity";

    enum Mode { STATIC, LOOP}

    static final float LOOP_START =  556.00f;
    static final float LOOP_END   = 2472.00f;

    Mode mode = Mode.LOOP;
    float txtPosition = LOOP_START + ((LOOP_END - LOOP_START) / 2);
    float loopStep  = 10.00f;
    int   loopSleep = 10;

    /**
     * Called when the activity is first created. Here we normally initialize
     * our GUI.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xoxo);

        /**
         * LOOP
         */
        ((EditText) findViewById(R.id.editTextStep    )).setText(String.valueOf(loopStep   ));
        ((EditText) findViewById(R.id.editTextSleep   )).setText(String.valueOf(loopSleep  ));

        findViewById(R.id.buttonLoop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mode      = Mode.LOOP;
                loopStep  = Float.parseFloat(((TextView) findViewById(R.id.editTextStep     )).getText().toString());
                loopSleep = Integer.parseInt(((TextView) findViewById(R.id.editTextSleep    )).getText().toString());

                Log.i(TAG, "Loop mode");

            }
        });

        /**
         * STATIC
         */
        ((EditText) findViewById(R.id.editTextPosition)).setText(String.valueOf(txtPosition));

        findViewById(R.id.buttonSet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mode        = Mode.STATIC;
                txtPosition = Float.parseFloat(((TextView) findViewById(R.id.editTextPosition )).getText().toString());

                System.out.println("***** Static mode, request position " + txtPosition);

            }
        });

    }

    /**
     *
     * This is the thread on which all the IOIO activity happens. It will be run
     * every time the application is resumed and aborted when it is paused. The
     * method setup() will be called right after a connection with the IOIO has
     * been established (which might happen several times!). Then, loop() will
     * be called repetitively until the IOIO gets disconnected.
     */

    static enum Direction {TO_THE_LEFT, TO_THE_RIGHT}

    class Looper extends BaseIOIOLooper {

        static final int PIN  = 3;
        static final int FREQ = 50;

        PwmOutput outPin;
        Direction loopDirection = Direction.TO_THE_RIGHT;

        float position  = LOOP_START;

        public Looper() {
            super();
        }

        /**
         * Called every time a connection with IOIO has been established.
         * Typically used to open pins.
         *
         * @throws ConnectionLostException
         *             When IOIO connection is lost.
         */
        @Override
        protected void setup() throws ConnectionLostException {
            System.out.println("*** Setup");
            outPin = ioio_.openPwmOutput(PIN, FREQ);
        }


        /**
         * Called repetitively while the IOIO is connected.
         *
         * @throws ConnectionLostException
         *             When IOIO connection is lost.
         */
        @Override
        public void loop() throws ConnectionLostException {

            if (mode == Mode.LOOP)
                loopLoop();
            else
                loopStatic();

//            System.out.println(mode + " " + position + "%");
            outPin.setPulseWidth(position);

        }

        private void loopLoop() {

            position = (loopDirection == Direction.TO_THE_RIGHT) ? position + loopStep: position - loopStep;

            if (loopDirection == Direction.TO_THE_RIGHT && position > LOOP_END) {

                loopDirection = Direction.TO_THE_LEFT;
                position  = LOOP_END;

            } else if (loopDirection ==  Direction.TO_THE_LEFT && position < LOOP_START) {

                loopDirection = Direction.TO_THE_RIGHT;
                position  = LOOP_START;

            }

            try {
                Thread.sleep(loopSleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        private void loopStatic() {

            if (position != txtPosition) {
                position = txtPosition;
                System.out.println("*** Setting position to " + txtPosition);
            }

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void disconnected() {
            super.disconnected();
            System.out.println("*** Disconnected");
        }

        @Override
        public void incompatible() {
            super.incompatible();
            System.out.println("*** Incompatible");
        }

    }

    /**
     * A method to create our IOIO thread.
     */
    @Override
    protected IOIOLooper createIOIOLooper() {
        return new Looper();
    }

}