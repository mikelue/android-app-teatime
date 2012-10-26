package idv.mikelue.teatime;

import android.content.Intent;

import mockit.Mock;
import mockit.MockUp;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TeaTimeActivityTest {
	public TeaTimeActivityTest() {}

	/**
	 * Tests the setting for value of intent used to start {@link TeaTimeActivity}.<p>
	 */
	@Test
	public void putEndedIdOfRounds()
	{
		final int[] testRoundIds = new int[] { -10, -20, 74 };

		new MockUp<Intent>() {
			@Mock
			void $init() {}

			@Mock(invocations=1)
			Intent putExtra(String name, int[] value)
			{
				Assert.assertEquals(name, "_id_of_ended_rounds_");
				Assert.assertEquals(value.length, testRoundIds.length);
				for (int i = 0; i < testRoundIds.length; i++) {
					Assert.assertEquals(value[i], testRoundIds[i]);
				}

				return getMockInstance();
			}
		};

		TeaTimeActivity.putEndedIdOfRounds(new Intent(), testRoundIds);
	}
}
