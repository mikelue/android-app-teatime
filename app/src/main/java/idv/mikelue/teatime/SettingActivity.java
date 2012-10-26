package idv.mikelue.teatime;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

import static idv.mikelue.teatime.AppPreferences.*;

public class SettingActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
	private final static String TAG = SettingActivity.class.getSimpleName();

	public SettingActivity() {}

	private AppPreferences appPrefs;
	private EditTextPreference durationPreference;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);

		durationPreference = (EditTextPreference)findPreference(KEY_ALARM_DURATION);
	}

	/**
	 * Fix the value duration by putting limit of minimum value on it.<p>
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		if (!KEY_ALARM_DURATION.equals(key)) {
			return;
		}

		int duration = processAndGetDuration(sharedPreferences);
		sharedPreferences.edit().putString(KEY_ALARM_DURATION, String.valueOf(duration))
			.commit();

		refreshDuration();
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);

		refreshDuration();
	}
	@Override
	protected void onPause()
	{
		super.onPause();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.unregisterOnSharedPreferenceChangeListener(this);
	}

	private void refreshDuration()
	{
		String duration = durationPreference.getSharedPreferences().getString(KEY_ALARM_DURATION, DEFAULT_DURATION_STR);

		durationPreference.setText(duration);
		durationPreference.setSummary(
			getResources().getString(
				R.string.message_seconds, duration
			)
		);
	}

	/**
	 * This method check on and fix the duration with unrecognized format.<p>
	 */
	private static int processAndGetDuration(SharedPreferences prefs)
	{
		String duration = prefs.getString(KEY_ALARM_DURATION, DEFAULT_DURATION_STR)
			.trim();

		if (duration.equals("")) {
			duration = DEFAULT_DURATION_STR;
		}

		/**
		 * Keep the duration at leat as MIN_DURATION
		 */
		int intDuration = Integer.parseInt(duration);
		if (intDuration < MIN_DURATION) {
			intDuration = MIN_DURATION;
		}
		// :~)

		return intDuration;
	}
}
