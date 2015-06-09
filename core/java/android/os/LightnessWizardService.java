package android.os;

import android.content.Context;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.IWindowManager;

import com.android.internal.widget.LockPatternUtils;

public class LightnessWizardService extends ILightnessWizard.Stub {

	public static final int LIGHTNESS_WIZARD_START = 1;
	public static final int LIGHTNESS_WIZARD_SLEEP = 2;
	public static final int LIGHTNESS_WIZARD_LIGHT_ON_SCREEN = 3;
	public static final int LIGHTNESS_WIZARD_SHUTDOWN_SCREEN = 4;

	private static final String TAG = "LightnessWizardService";

	private volatile boolean isSleeping = true;

//	private SensorManager mSensorManager;
//
//	private Sensor mLightSensor;
//	private Sensor mProximitySensor;

	private Context context;

	private WakeLock mWakeLock;

	public LightnessWizardService(Context context) {
		this.context = context;
	}

//	private void start() {
//		Log.v(TAG, "------LightnessWizardService start------");
//		mSensorManager = (SensorManager) context
//				.getSystemService(Context.SENSOR_SERVICE);
//		mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
//		mProximitySensor = mSensorManager
//				.getDefaultSensor(Sensor.TYPE_PROXIMITY);
//		mSensorManager.registerListener(this, mLightSensor,
//				SensorManager.SENSOR_DELAY_NORMAL);
//		mSensorManager.registerListener(this, mProximitySensor,
//				SensorManager.SENSOR_DELAY_NORMAL);
//		isSleeping = false;
//	}
//
//	private void sleep() {
//		mSensorManager.unregisterListener(this, mLightSensor);
//		mSensorManager.unregisterListener(this, mProximitySensor);
//		mLightSensor = null;
//		mProximitySensor = null;
//		mSensorManager = null;
//		isSleeping = true;
//	}

//	private void lightOn() {
//		try {
//			lightOnScreen();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	private void shutdown() {
//		try {
//			lockNowUnchecked();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	private void lightOnScreen() {
		PowerManager mPowerManager = getPowerManager();
		if (mPowerManager.isInteractive()) {
			return;
		}

		if (mWakeLock == null) {
			mWakeLock = mPowerManager.newWakeLock(
					PowerManager.ACQUIRE_CAUSES_WAKEUP
							| PowerManager.SCREEN_DIM_WAKE_LOCK,
					"LightnessWizardService");
		}
		// mWakeLock = mPowerManager.newWakeLock(
		// PowerManager.PARTIAL_WAKE_LOCK, "StartingAlertService");
		// mWakeLock.setReferenceCounted(false);
		mWakeLock.acquire();
		mWakeLock.release();
	}

	private void lockNowUnchecked() {
		PowerManager mPowerManager = getPowerManager();
		if (!mPowerManager.isInteractive()) {
			return;
		}
		long ident = Binder.clearCallingIdentity();
		try {
			// Power off the display
			mPowerManager.goToSleep(SystemClock.uptimeMillis(),
					PowerManager.GO_TO_SLEEP_REASON_DEVICE_ADMIN, 0);
			// Ensure the device is locked
			new LockPatternUtils(context)
					.requireCredentialEntry(UserHandle.USER_ALL);
			getWindowManager().lockNow(null);
		} catch (Exception e) {
		} finally {
			Binder.restoreCallingIdentity(ident);
		}
	}

	private IWindowManager getWindowManager() {
		IBinder b = ServiceManager.getService(Context.WINDOW_SERVICE);
		IWindowManager mIWindowManager = IWindowManager.Stub.asInterface(b);
		return mIWindowManager;
	}

	private PowerManager getPowerManager() {
		return (PowerManager) context.getSystemService(Context.POWER_SERVICE);
	}

//	@Override
//	public void onSensorChanged(SensorEvent event) {
//		if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
//			float[] values = event.values;
//			if (values[0] < 5) {
//				Log.v(TAG, "shutdown");
//				shutdown();
//				return;
//			} else if (values[0] > 20) {
//				Log.v(TAG, "lightOn");
//				lightOn();
//			}
//		}
//		if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
//			float[] values = event.values;
//			Log.v(TAG, "values[0] : " + values[0]);
//			Log.v(TAG, "values[1] : " + values[1]);
//			Log.v(TAG, "values[2] : " + values[2]);
//		}
//	}
//
//	@Override
//	public void onAccuracyChanged(Sensor sensor, int accuracy) {
//
//	}

	@Override
	public boolean isRunning() throws RemoteException {
		return !isSleeping;
	}

	@Override
	public void onSwitch(int flags) throws RemoteException {
		switch (flags) {
//		case LIGHTNESS_WIZARD_START:
//			start();
//			break;
//		case LIGHTNESS_WIZARD_SLEEP:
//			sleep();
//			break;
		case LIGHTNESS_WIZARD_LIGHT_ON_SCREEN:
			lightOnScreen();
			break;
		case LIGHTNESS_WIZARD_SHUTDOWN_SCREEN:
			lockNowUnchecked();
			break;
		}
	}
}