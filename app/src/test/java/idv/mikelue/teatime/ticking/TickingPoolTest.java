package idv.mikelue.teatime.ticking;

import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import idv.mikelue.teatime.model.TickingRound;

public class TickingPoolTest {
	public TickingPoolTest() {}

	/**
	 * Tests the decrease in time.<p>
	 */
	@Test
	public void performDecrease()
	{
		final int testSeconds = 7;
		final int decreaseSeconds = 2;

		/**
		 * Perform decrease in time
		 */
		TickingPool testPool = buildTickingPool(
			new TickingRound(-1, testSeconds)
		);
		for (int i = 0; i < TickingAccuracy.TICKING_RATIO_OF_1_SECOND * decreaseSeconds; i++) {
			testPool.performDecrease();
		}
		// :~)

		Assert.assertEquals(
			testPool.getAliveRounds().iterator().next()
				.getRemainSeconds(),
			testSeconds - decreaseSeconds
		);
	}

	/**
	 * Tests the removal of alive rounds.<p>
	 */
	@Test
	public void removeAliveRound()
	{
		final int testRoundId = 77;

		/**
		 * Perform removal of round
		 */
		TickingPool testPool = buildTickingPool(
			new TickingRound(testRoundId, -1),
			new TickingRound(-1, -1)
		);
		boolean resultOfRemoval = testPool.removeAliveRound(testRoundId);
		// :~)

		Assert.assertTrue(resultOfRemoval);
		Assert.assertEquals(
			testPool.getNumberOfAliveRounds(), 1
		);
		Assert.assertEquals(
			testPool.getAliveRounds().size(), 1
		);
	}

	/**
	 * Tests the consuming of end rounds.<p>
	 */
	@Test
	public void consumeEndedRounds()
	{
		/**
		 * Perform decrease
		 */
		TickingPool testPool = buildTickingPool(
			new TickingRound(-1, 0),
			new TickingRound(-2, 0),
			new TickingRound(-3, 10)
		);
		testPool.performDecrease();
		// :~)

		/**
		 * Assert the alive rounds
		 */
		Set<TickingRound> aliveRounds = testPool.getAliveRounds();

		Assert.assertEquals(aliveRounds.size(), 1); // The result of second consume should be empty
		Assert.assertEquals(aliveRounds.iterator().next().getId(), -3);
		// :~)

		/**
		 * Assert the ended rounds
		 */
		Set<TickingRound> endedRounds = testPool.consumeEndedRounds();

		Assert.assertEquals(endedRounds.size(), 2); // The result of second consume should be empty
		for (TickingRound endedRound: endedRounds) {
			Assert.assertTrue(endedRound.getId() == -1 || endedRound.getId() == -2);
		}

		Assert.assertEquals(testPool.consumeEndedRounds().size(), 0); // The result of second consume should be empty
		// :~)
	}

	private TickingPool buildTickingPool(TickingRound... rounds)
	{
		TickingPool pool = new TickingPool();

		for (TickingRound tickingRound: rounds) {
			pool.addTickingRound(tickingRound);
		}

		return pool;
	}
}
