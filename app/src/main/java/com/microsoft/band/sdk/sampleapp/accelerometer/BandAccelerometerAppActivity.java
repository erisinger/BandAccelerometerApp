//Copyright (c) Microsoft Corporation All rights reserved.  
// 
//MIT License: 
// 
//Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
//documentation files (the  "Software"), to deal in the Software without restriction, including without limitation
//the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
//to permit persons to whom the Software is furnished to do so, subject to the following conditions: 
// 
//The above copyright notice and this permission notice shall be included in all copies or substantial portions of
//the Software. 
// 
//THE SOFTWARE IS PROVIDED ""AS IS"", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
//TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
//THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
//CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
//IN THE SOFTWARE.
package com.microsoft.band.sdk.sampleapp.accelerometer;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.BandIOException;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.UserConsent;
import com.microsoft.band.sdk.sampleapp.accelerometer.MHLClient.MHLMobileIOClient;
import com.microsoft.band.sdk.sampleapp.accelerometer.MHLClient.MHLSensorReadings.MHLAccelerometerReading;
import com.microsoft.band.sdk.sampleapp.accelerometer.MHLClient.MHLSensorReadings.MHLGyroscopeReading;
import com.microsoft.band.sdk.sampleapp.accelerometer.MHLClient.MHLSensorReadings.MHLHeartRateReading;
import com.microsoft.band.sdk.sampleapp.accelerometer.MHLClient.MHLSensorReadings.MHLSensorReading;
import com.microsoft.band.sensors.BandAccelerometerEvent;
import com.microsoft.band.sensors.BandGyroscopeEvent;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandAccelerometerEventListener;
import com.microsoft.band.sensors.BandGyroscopeEventListener;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.HeartRateConsentListener;
import com.microsoft.band.sensors.SampleRate;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.app.Activity;
import android.os.AsyncTask;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class BandAccelerometerAppActivity extends Activity {

	String TEMP_USER_ID = "1";

	private BandClient client = null;
	private Button btnStart;
	private TextView txtStatus;

	private MHLMobileIOClient mHLClient = null;

	private BandAccelerometerEventListener mAccelerometerEventListener = new BandAccelerometerEventListener() {
		@Override
		public void onBandAccelerometerChanged(final BandAccelerometerEvent event) {
			if (event != null) {
				double x, y, z;
				long t = System.currentTimeMillis();
				final double g = 9.8;

				x = event.getAccelerationX() * g;
				y = event.getAccelerationY() * g;
				z = event.getAccelerationZ() * g;

				MHLSensorReading reading = new MHLAccelerometerReading(TEMP_USER_ID, "MS_BAND_1", "bogus_serial_number", t, x, y, z, "");

				mHLClient.addSensorReading(reading);

//				Log.d("submitted reading: ", "new reading");

//				appendToUI(String.format("" + event.getAccelerationX()
//						+ "," + event.getAccelerationY()
//						+ "," + event.getAccelerationZ()));
			}
		}
	};

	private BandGyroscopeEventListener mGyroscopeEventListener = new BandGyroscopeEventListener() {
		@Override
		public void onBandGyroscopeChanged(final BandGyroscopeEvent event) {
			if (event != null) {

				double x, y, z;
				long t = System.currentTimeMillis();
				x = event.getAngularVelocityX();
				y = event.getAngularVelocityY();
				z = event.getAngularVelocityZ();

				mHLClient.addSensorReading(new MHLGyroscopeReading(TEMP_USER_ID, "MS_BAND_1", "bogus_serial_number", t, x, y, z, ""));
			}
		}
	};

	private BandHeartRateEventListener mHeartRateEventListener = new BandHeartRateEventListener() {
		@Override
		public void onBandHeartRateChanged(final BandHeartRateEvent event) {
			if (event != null) {
				long t = System.currentTimeMillis();
				mHLClient.addSensorReading(new MHLHeartRateReading(TEMP_USER_ID, "MS_BAND_1", "bogus_serial_number", t, event.getHeartRate(), ""));
			}
		}
	};

	private HeartRateConsentListener mHeartRateConsentListener = new HeartRateConsentListener() {

		@Override
		public void userAccepted(boolean b) {

		}
	};
	/**
	 * ATTENTION: This was auto-generated to implement the App Indexing API.
	 * See https://g.co/AppIndexing/AndroidStudio for more information.
	 */
	private GoogleApiClient client2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		txtStatus = (TextView) findViewById(R.id.txtStatus);
		btnStart = (Button) findViewById(R.id.btnStart);

		btnStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mHLClient = new MHLMobileIOClient(BandAccelerometerAppActivity.this, "none.cs.umass.edu", 9999, TEMP_USER_ID);
//				mHLClient = new MHLMobileIOClient(BandAccelerometerAppActivity.this, "192.168.0.12", 9999, TEMP_USER_ID);
				txtStatus.setText("");
				new MultiSensorSubscriptionTask().execute();
			}
		});
		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client2 = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
	}

	@Override
	protected void onResume() {
		super.onResume();
		txtStatus.setText("");
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (client != null) {
			try {
				client.getSensorManager().unregisterAccelerometerEventListener(mAccelerometerEventListener);
			} catch (BandIOException e) {
				appendToUI(e.getMessage());
			}
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client2.connect();
		Action viewAction = Action.newAction(
				Action.TYPE_VIEW, // TODO: choose an action type.
				"BandAccelerometerApp Page", // TODO: Define a title for the content shown.
				// TODO: If you have web page content that matches this app activity's content,
				// make sure this auto-generated web page URL is correct.
				// Otherwise, set the URL to null.
				Uri.parse("http://host/path"),
				// TODO: Make sure this auto-generated app URL is correct.
				Uri.parse("android-app://com.microsoft.band.sdk.sampleapp.accelerometer/http/host/path")
		);
		AppIndex.AppIndexApi.start(client2, viewAction);
	}

	@Override
	public void onStop() {
		super.onStop();

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		Action viewAction = Action.newAction(
				Action.TYPE_VIEW, // TODO: choose an action type.
				"BandAccelerometerApp Page", // TODO: Define a title for the content shown.
				// TODO: If you have web page content that matches this app activity's content,
				// make sure this auto-generated web page URL is correct.
				// Otherwise, set the URL to null.
				Uri.parse("http://host/path"),
				// TODO: Make sure this auto-generated app URL is correct.
				Uri.parse("android-app://com.microsoft.band.sdk.sampleapp.accelerometer/http/host/path")
		);
		AppIndex.AppIndexApi.end(client2, viewAction);
		client2.disconnect();
	}

	private class MultiSensorSubscriptionTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			try {
				if (getConnectedBandClient()) {
					appendToUI("Band is connected.\n");

					client.getSensorManager().registerAccelerometerEventListener(mAccelerometerEventListener, SampleRate.MS16);
					client.getSensorManager().registerGyroscopeEventListener(mGyroscopeEventListener, SampleRate.MS32);
					client.getSensorManager().registerHeartRateEventListener(mHeartRateEventListener);

				} else {
					appendToUI("Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
				}
			} catch (BandException e) {
				String exceptionMessage = "";
				switch (e.getErrorType()) {
					case UNSUPPORTED_SDK_VERSION_ERROR:
						exceptionMessage = "Microsoft Health BandService doesn't support your SDK Version. Please update to latest SDK.\n";
						break;
					case SERVICE_ERROR:
						exceptionMessage = "Microsoft Health BandService is not available. Please make sure Microsoft Health is installed and that you have the correct permissions.\n";
						break;
					default:
						exceptionMessage = "Unknown error occured: " + e.getMessage() + "\n";
						break;
				}
				appendToUI(exceptionMessage);

			} catch (Exception e) {
				appendToUI(e.getMessage());
			}
			return null;
		}
	}

	@Override
	protected void onDestroy() {
		if (client != null) {
			try {
				client.disconnect().await();
			} catch (InterruptedException e) {
				// Do nothing as this is happening during destroy
			} catch (BandException e) {
				// Do nothing as this is happening during destroy
			}
		}
		super.onDestroy();
	}

	public void appendToUI(final String string) {



		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				new AlertDialog.Builder(new ContextThemeWrapper(BandAccelerometerAppActivity.this, android.R.style.Theme_Dialog))
						.setTitle("Heart Rate Anomaly Detected!")
						.setMessage(string)
						.setPositiveButton("Thanks for being Aware", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								// do nothing
							}
						})
						.show();
				txtStatus.setText(string);
			}
		});
	}

	private boolean getConnectedBandClient() throws InterruptedException, BandException {
		if (client == null) {
			BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
			if (devices.length == 0) {
				appendToUI("Band isn't paired with your phone.\n");
				return false;
			}
			client = BandClientManager.getInstance().create(getBaseContext(), devices[0]);
		} else if (ConnectionState.CONNECTED == client.getConnectionState()) {
			return true;
		}

		appendToUI("Band is connecting...\n");

		// check current user heart rate consent
		if (client.getSensorManager().getCurrentHeartRateConsent() !=
				UserConsent.GRANTED) {
			// user hasn’t consented, request consent
			client.getSensorManager().requestHeartRateConsent(this, mHeartRateConsentListener);
		}

		return ConnectionState.CONNECTED == client.connect().await();
	}
}

