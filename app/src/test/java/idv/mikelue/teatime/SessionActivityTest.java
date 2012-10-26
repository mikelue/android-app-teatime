package idv.mikelue.teatime;

import android.content.Intent;

import mockit.Mock;
import mockit.MockUp;
import org.testng.Assert;
import org.testng.annotations.Test;

import idv.mikelue.teatime.model.Session.IconType;
import idv.mikelue.teatime.model.Session;

public class SessionActivityTest {
	public SessionActivityTest() {}

	/**
	 * Tests the setting of session id for intent to start {@link SessionActivity}.<p>
	 */
	@Test
	public void setSessionData()
	{
		final int testId = 77;
		final int testNumberOfRounds = 4;
		final String testName = "test-session-name";
		final IconType testIconType = Session.IconType.Type5;

		new MockUp<Intent>() {
			@Mock
			void $init() {}

			@Mock(invocations=2)
			Intent putExtra(String name, int value)
			{
				if ("_session_id_".equals(name)) {
					Assert.assertEquals(name, "_session_id_");
					Assert.assertEquals(value, testId);
				} else if ("_session_color_value_".equals(name)) {
					Assert.assertEquals(name, "_session_color_value_");
					Assert.assertEquals(value, testIconType.getDatabaseValue());
				}

				return getMockInstance();
			}
			@Mock(invocations=1)
			Intent putExtra(String name, String value)
			{
				Assert.assertEquals(name, "_session_name_");
				Assert.assertEquals(value, testName);

				return getMockInstance();
			}
		};

		Session testSession = new Session(testId);
		testSession.setName(testName);
		testSession.setIconType(testIconType);
		testSession.setNumberOfRounds(testNumberOfRounds);

		SessionActivity.setSessionData(new Intent(), testSession);
	}
}
