package idv.mikelue.teatime.model;

import idv.mikelue.teatime.ticking.TickingAccuracy;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TickingRoundTest {
	public TickingRoundTest() {}

	/**
	 * Tests the prevention of zero-division by TICKING_RATIO_OF_1_SECOND.<p>
	 */
	@Test
	public void getRemainSecondsPreventZeroDivision()
	{
		TickingRound tickingRound = new TickingRound(-1, 1);
		tickingRound.decrease();

		Assert.assertEquals(tickingRound.getRemainSeconds(), 1);
	}

	/**
	 * Tests the decrease in time.<p>
	 */
	@Test
	public void decrease()
	{
		final int testSeconds = 3;
		TickingRound testRound = new TickingRound(-1, testSeconds);

		for (int i = 0; i < TickingAccuracy.TICKING_RATIO_OF_1_SECOND; i++) {
			testRound.decrease();
		}

		Assert.assertEquals(testRound.getRemainSeconds(), testSeconds - 1);
	}

	/**
	 * Tests checking for the end of round.<p>
	 */
	@Test
	public void isEnded()
	{
		TickingRound testRound = new TickingRound(-1, 1);

		Assert.assertFalse(testRound.isEnded());

		testRound.decrease();
		testRound.decrease();
		testRound.decrease();
		testRound.decrease();

		Assert.assertTrue(testRound.isEnded());
	}
}
