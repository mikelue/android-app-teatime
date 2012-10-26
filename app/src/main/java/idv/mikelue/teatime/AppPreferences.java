package idv.mikelue.teatime;

import android.content.SharedPreferences;

/**
 * Represents the content of preferences of application.<p.
 */
public class AppPreferences {
	/**
	 * Key setting of preferences
	 */
	final static String KEY_ALARM_DURATION = "alarm_duration";

	private final static String KEY_ALARM_SOUND = "alarm_sound";
	private final static String KEY_ALARM_VIBRATION = "alarm_vibration";
	// :~)

	/**
	 * The minium duration(seconds) while alarming({@value #MIN_DURATION}).<p>
	 */
	private final static int DEFAULT_DURATION = 30;

	final static int MIN_DURATION = 3;
	final static String DEFAULT_DURATION_STR = String.valueOf(DEFAULT_DURATION);
	// :~)

	/**
	 * Load the data of preferences from {@link SharedPreferences}.<p>
	 *
	 * @param preferences The preferences in Android storage
	 */
	public static AppPreferences init(SharedPreferences preferences)
	{
		return new AppPreferences(preferences);
	}

	private boolean alarmWithSound = true;
	private boolean alarmWithVibration = true;
	private int alarmDuration = DEFAULT_DURATION;

	private AppPreferences(SharedPreferences newPrefs)
	{
		setAlarmWithSound(newPrefs.getBoolean(KEY_ALARM_SOUND, true));
		setAlarmWithVibration(newPrefs.getBoolean(KEY_ALARM_VIBRATION, true));
		setAlarmDuration(getDurationFromPrefs(newPrefs));
	}

	/**
	 * Gets whether to play sound while alarming.<p>
	 *
	 * @return whether to play sound while alarming
	 *
	 * @see #setAlarmWithSound
	 */
	public boolean isAlarmWithSound()
	{
		return alarmWithSound;
	}
	/**
	 * Sets whether to play sound while alarming.<p>
	 *
	 * @param newAlarmWithSound whether to play sound while alarming
	 *
	 * @see #isAlarmWithSound
	 */
	public void setAlarmWithSound(boolean newAlarmWithSound)
	{
		alarmWithSound = newAlarmWithSound;
	}

	/**
	 * Gets whether to vibrate while alarming.<p>
	 *
	 * @return whether to vibrate while alarming
	 *
	 * @see #setAlarmWithVibration
	 */
	public boolean isAlarmWithVibration()
	{
		return alarmWithVibration;
	}
	/**
	 * Sets whether to vibrate while alarming.<p>
	 *
	 * @param newAlarmWithVibration whether to vibrate while alarming
	 *
	 * @see #isAlarmWithVibration
	 */
	public void setAlarmWithVibration(boolean newAlarmWithVibration)
	{
		alarmWithVibration = newAlarmWithVibration;
	}

	/**
	 * Gets the duration(seconds) while alarming.<p>
	 *
	 * @return the duration(seconds) while alarming
	 *
	 * @see #setAlarmDuration
	 */
	public int getAlarmDuration()
	{
		return alarmDuration;
	}
	/**
	 * Sets the duration(seconds) while alarming.<p>
	 *
	 * @param newAlarmDuration new the duration(seconds) while alarming
	 *
	 * @see #getAlarmDuration
	 */
	public void setAlarmDuration(int newAlarmDuration)
	{
		alarmDuration = newAlarmDuration;
	}

	private static int getDurationFromPrefs(SharedPreferences prefs)
	{
		return Integer.parseInt(prefs.getString(KEY_ALARM_DURATION, DEFAULT_DURATION_STR));
	}
}
