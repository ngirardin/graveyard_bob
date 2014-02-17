package fr.dmconcept.bob.activities;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.logging.Handler;

import fr.dmconcept.bob.R;
import ioio.lib.api.DigitalOutput;
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

    /**
     * Called when the activity is first created. Here we normally initialize
     * our GUI.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.xoxo);
    }

    /**
     * This is the thread on which all the IOIO activity happens. It will be run
     * every time the application is resumed and aborted when it is paused. The
     * method setup() will be called right after a connection with the IOIO has
     * been established (which might happen several times!). Then, loop() will
     * be called repetitively until the IOIO gets disconnected.
     */
    class Looper extends BaseIOIOLooper {

        private DigitalOutput outPin;
        private boolean pinUp = false;

        public Looper() {
            super();
            System.out.println("Looper()");
        }

        /**
         * Called every time a connection with IOIO has been established.
         * Typically used to open pins.
         *
         * @throws ConnectionLostException
         *             When IOIO connection is lost.
         *
         * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#setup()
         */
        @Override
        protected void setup() throws ConnectionLostException {
            log("setup");
            outPin = ioio_.openDigitalOutput(3, true);
        }

        /**
         * Called repetitively while the IOIO is connected.
         *
         * @throws ConnectionLostException
         *             When IOIO connection is lost.
         *
         * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#loop()
         */
        @Override
        public void loop() throws ConnectionLostException {

            pinUp = !pinUp;

            log("loop() " + pinUp);

            outPin.write(pinUp);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }

        @Override
        public void disconnected() {
            super.disconnected();
            System.out.println("--- Disconnected");
        }

        @Override
        public void incompatible() {
            super.incompatible();
            System.out.println("--- Incompatible");
        }

    }

    /**
     * A method to create our IOIO thread.
     */
    @Override
    protected IOIOLooper createIOIOLooper() {
        return new Looper();
    }

    void log(final String message) {

        System.out.println(message);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                TextView textView = new TextView(getBaseContext());
                textView.setText(message);

                LinearLayout xoxoLog = (LinearLayout) findViewById(R.id.xoxoLog);
                xoxoLog.addView(textView);

            }
        });

    }

}