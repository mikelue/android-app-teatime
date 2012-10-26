package idv.mikelue.teatime.view;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import idv.mikelue.teatime.model.DetailTickingRound;

public class TickingRoundPoolTest {
	public TickingRoundPoolTest() {}

	/**
	 * Tests the adding of ticking round and prevention of duplicated
	 * adding.<p>
	 */
	@Test
	public void putTickingRound()
	{
		TickingRoundPool testPool = new TickingRoundPool();

		testPool.putTickingRound(buildTickingRound(1, 20), null);
		Assert.assertEquals(testPool.getNumberOfRounds(), 1);

		/**
		 * Assert the adding of duplicated round
		 */
		testPool.putTickingRound(buildTickingRound(1, 30), null);
		Assert.assertEquals(testPool.getNumberOfRounds(), 1);
		// :~)
	}

	/**
	 * Tests the removal of ticking round.<p>
	 */
	@Test
	public void removeTickingRound()
	{
		DetailTickingRound testRound = buildTickingRound(1, 20);

		TickingRoundPool testPool = new TickingRoundPool();

		testPool.putTickingRound(testRound, null);
		Assert.assertEquals(testPool.getNumberOfRounds(), 1);

		testPool.removeTickingRound(testRound);
		Assert.assertEquals(testPool.getNumberOfRounds(), 0);
	}

	/**
	 * Tests the indexing of rounds by their remain time.<p>
	 */
	@Test(dataProvider="GetIndexOfRoundByRemainTime", dependsOnMethods="putTickingRound")
	public void getIndexOfRoundByRemainTime(
		int testRoundSeconds, int expectedIndex
	) {
		TickingRoundPool testPool = new TickingRoundPool();

		testPool.putTickingRound(buildTickingRound(1, 10), null);
		testPool.putTickingRound(buildTickingRound(2, 20), null);
		testPool.putTickingRound(buildTickingRound(3, 30), null);

		DetailTickingRound testTickingRound = buildTickingRound(4, testRoundSeconds);
		testPool.putTickingRound(testTickingRound, null);

		Assert.assertEquals(
			testPool.getIndexOfRoundByRemainTime(testTickingRound),
			expectedIndex
		);
	}
	@DataProvider(name="GetIndexOfRoundByRemainTime")
	private Object[][] getGetIndexOfRoundByRemainTime()
	{
		return new Object[][] {
			{ 5, 0 },
			{ 10, 1 },
			{ 15, 1 },
			{ 20, 2 },
			{ 25, 2 },
			{ 30, 3 },
			{ 35, 3 }
		};
	}

	private DetailTickingRound buildTickingRound(int roundId, int seconds)
	{
		return new DetailTickingRound(roundId, seconds);
	}
}
