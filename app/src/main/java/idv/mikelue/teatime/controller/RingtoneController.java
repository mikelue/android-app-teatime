package idv.mikelue.teatime.controller;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;

import idv.mikelue.teatime.R;
import idv.mikelue.teatime.AppPreferences;

/**
 * This controller handles the playing of ringtone and prolong the playing
 * ringtone while a new alarm has arrived.<p>
 */
public class RingtoneController {
	private final static String TAG = RingtoneController.class.getSimpleName();

	private final int playDuration;
	private final Handler handler = new Handler();

	private final Ringtone alarmRingtone;
	private final Vibrator vibrator;
	private final PowerManager.WakeLock wakeLock;

	/**
	 * Constructs this object with data of preferences
	 */
	public RingtoneController(Context context, AppPreferences newAppPreferences)
	{
		playDuration = newAppPreferences.getAlarmDuration() * 1000;

		alarmRingtone = newAppPreferences.isAlarmWithSound() ?
			buildRingtone(context) : null;
		vibrator = newAppPreferences.isAlarmWithVibration() ?
			(Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE) : null;

		PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TeaTimeAlarm");
	}

	/**
	 * Stop the ringtone by setting of alarm duration.<p>
	 */
	private final Runnable stopAlarming = new Runnable() {
		@Override
		public void run()
		{
			Log.v(TAG, "Stop ring tone for end of alarm duration");

			stopSystemServiceOfAlarming();
		}
	};

	private final long[] vibratePattern = new long[] {
		1000, 500
	};
	public void startPlay()
	{
		if (alarmRingtone != null) {
			if (!alarmRingtone.isPlaying()) {
				Log.d(TAG, "Play ring tone");
				alarmRingtone.play();
			}
		}

		if (vibrator != null) {
			Log.d(TAG, "Start vibrating");
			vibrator.vibrate(vibratePattern, 0);
		}

		if (!wakeLock.isHeld()) {
			Log.d(TAG, "Acquire wake lock");
			wakeLock.acquire();
		}

		/**
		 * Prolong the alarming
		 */
		handler.removeCallbacks(stopAlarming);
		handler.postDelayed(stopAlarming, playDuration);
		// :~)
	}

	/**
	 * Stops the ringtone because there is no need to play.<p>
	 */
	public void forceStop()
	{
		Log.v(TAG, "Stop ring tone directly");

		stopSystemServiceOfAlarming();
		handler.removeCallbacks(stopAlarming);
	}

	/**
	 * Stops the pending thread to stop the alarm ringtone and
	 * the playing ringtone.<p>
	 */
	public void release()
	{
		Log.v(TAG, "Stop ring tone for release of controller");
		forceStop();
	}

	private void stopSystemServiceOfAlarming()
	{
		if (alarmRingtone != null && alarmRingtone.isPlaying()) {
			alarmRingtone.stop();
		}

		if (vibrator != null) {
			vibrator.cancel();
		}

		if (wakeLock.isHeld()) {
			wakeLock.release();
		}
	}

	private final static Uri URI_FALLBACK_ALARM_SOUND =
		Uri.parse("android.resource://idv.mikelue.teatime/raw/" + R.raw.sound_alarm);
	private Ringtone buildRingtone(Context context)
	{
		Ringtone newAlarmRingtone = RingtoneManager.getRingtone(
			context,
			RingtoneManager.getDefaultUri(
				RingtoneManager.TYPE_ALARM
			)
		);

		/**
		 * Setup ringtone for Android emualtor
		 */
		if (newAlarmRingtone == null) {
			newAlarmRingtone = RingtoneManager.getRingtone(
				context, URI_FALLBACK_ALARM_SOUND
			);
		}
		// :~)

		return newAlarmRingtone;
	}
}
