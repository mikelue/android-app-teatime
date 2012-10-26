package idv.mikelue.teatime;

import android.content.SharedPreferences;

import mockit.Deencapsulation;
import mockit.Delegate;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static idv.mikelue.teatime.AppPreferences.*;

public class SettingActivityTest {
	@Mocked @mockit.Cascading
	private SharedPreferences mockSharedPref;

	public SettingActivityTest() {}

	/**
	 * Tests the convertion of duration setting({@value String}) to int
	 * value.<p>
	 */
	@Test(dataProvider="ProcessAndGetDuration")
	public void processAndGetDuration(
		final String testValue, final Integer expectedValue
	) {
		new NonStrictExpectations() {{
			mockSharedPref.getString(anyString, anyString);
			result = new Delegate() {
				String getString(String key, String defaultValue)
				{
					return testValue == null ? defaultValue : testValue;
				}
			};
		}};

		Assert.assertEquals(
			Deencapsulation.<Integer>invoke(SettingActivity.class, "processAndGetDuration", mockSharedPref),
			expectedValue
		);
	}

	@DataProvider(name="ProcessAndGetDuration")
	private Object[][] getAndProcessDurationDataProvider()
	{
		int defaultDuration = Deencapsulation.<Integer>getField(AppPreferences.class, "DEFAULT_DURATION");

		return new Object[][] {
			{ "4", 4 },
			{ "12", 12 },
			{ "508", 508 },
			{ String.valueOf(MIN_DURATION - 1), MIN_DURATION }, // Tests the range of preferences
			/**
			 * Test the empty/null value
			 */
			{ "", defaultDuration },
			{ null, defaultDuration }
			// :~)
		};
	}
}
