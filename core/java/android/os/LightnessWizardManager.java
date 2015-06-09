package android.os;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class LightnessWizardManager {

	private static final String TAG = "LightnessWizardManager";

	private ILightnessWizard mService;
	private Context mContext;

	private SensorManager mSensorManager;
	private LightnessWizardSensor mLightnessWizardSensor;

	/**
	 * Get the LightnessWizardManager instance to use for the supplied
	 * {@link android.content.Context Context} object.
	 */
	public static LightnessWizardManager getInstance(Context context) {
		return (LightnessWizardManager) context
				.getSystemService(Context.LIGHTNESS_WIZARD_SERVICE);
	}

	/**
	 * Creates a new instance.
	 *
	 * @param context
	 *            The current context in which to operate.
	 * @param service
	 *            The backing system service.
	 * @hide
	 */
	public LightnessWizardManager(Context context, ILightnessWizard service) {
		mService = service;
		mContext = context;
		mSensorManager = (SensorManager) mContext
				.getSystemService(Context.SENSOR_SERVICE);
		mLightnessWizardSensor = new LightnessWizardSensor();

		Log.v(TAG, "LightnessWizardManager created.");
	}

	public void startLightnessWizardPolicy() {
		mLightnessWizardSensor.registSensor();
	}

	public void stopLightnessWizardPolicy() {
		mLightnessWizardSensor.unregistSensor();
	}

	public void onSwitch(int flags) throws RemoteException {
		try {
			mService.onSwitch(flags);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private class LightnessWizardSensor implements SensorEventListener {

		private Sensor mLightSensor;
		private Sensor mProximitySensor;

		private boolean lightness = false;
		private boolean proximity = false;

		public LightnessWizardSensor() {
			mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
			mProximitySensor = mSensorManager
					.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			try {
				
				if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
					float[] values = event.values;
					final float distance = values[0];
					Log.v(TAG, "-------distance : " + values[0] + "-------");
                                        Log.v(TAG, "-------MaxRange : " + mProximitySensor.getMaximumRange() + "-------");
					proximity = distance >= Math.min(
									mProximitySensor.getMaximumRange(), 5.0f);
                                        Log.v(TAG, "-------proximity : " + proximity + "-------");
				}
				if (proximity) {
					mService.onSwitch(LightnessWizardService.LIGHTNESS_WIZARD_LIGHT_ON_SCREEN);
					//unregistSensor();
                                        Log.v(TAG, "-------LIGHT_ON_SCREEN-------");
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}

		public void registSensor() {
			mSensorManager.registerListener(this, mProximitySensor,
					SensorManager.SENSOR_DELAY_NORMAL);
		}

		public void unregistSensor() {
			mSensorManager.unregisterListener(this, mProximitySensor);
		}
	}
}
