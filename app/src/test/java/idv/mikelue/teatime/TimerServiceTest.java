package idv.mikelue.teatime;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager.WakeLock;

import mockit.Cascading;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.Mockit;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import idv.mikelue.teatime.model.TickingRound;
import idv.mikelue.teatime.test.AbstractMockAndroidEnvTestBase;
import idv.mikelue.teatime.ticking.TickingPool;
import idv.mikelue.teatime.ticking.TickingWorker;

/**
 * Tests internal operations for {@link TimerService} by mock environment.<p>
 */
public class TimerServiceTest extends AbstractMockAndroidEnvTestBase {
	@Mocked
	private Binder mockBinder;
	@Mocked
	private WakeLock mockWakeLock;

	@Mocked(methods={"<init>", "getApplicationContext", "startActivity", "getSystemService"})
	private TimerService mockStubTimerService;
	@Mocked(methods={"getTickingPool", "stillRunning", "tickRound"})
	private TickingWorker mockTickingWorker;

	public TimerServiceTest() {}

	/**
	 * Tests the setting for data of intent used to start service.<p>
	 */
	@Test
	public void setTickingRound()
	{
		final int testRoundId = -99;
		final int testRemainSeconds = 48;

		new MockUp<Intent>() {
			int invokeTimes = 0;

			@Mock
			void $init() {}

			@Mock(invocations=2)
			Intent putExtra(String name, int data)
			{
				switch (invokeTimes) {
					case 0:
						Assert.assertEquals(name, "_round_id_");
						Assert.assertEquals(data, testRoundId);
						invokeTimes++;
						break;
					case 1:
						Assert.assertEquals(name, "_ticking_seconds_");
						Assert.assertEquals(data, testRemainSeconds);
						invokeTimes++;
						break;
				}

				return getMockInstance();
			}
		};

		TimerService.setTickingRound(new Intent(), new TickingRound(testRoundId, testRemainSeconds));
	}

	/**
	 * Tests the calling of {@link TickingWorker} for ticking a round.<p>
	 */
	@Test
	public void onStartCommand()
	{
		final int testRoundId = -11;
		final int testSeconds = -20;

		/**
		 * Mock and record TickingWorker
		 */
		TimerService testService = buildTestService();

		final TickingWorker mockTickingWorker = Deencapsulation.getField(testService, TickingWorker.class);
		// :~)

		/**
		 * Mock intent which is used in TickingWorker
		 */
		new MockUp<Intent>() {
			@Mock
			void $init() {}

			@Mock
			int getIntExtra(String name, int defaultValue)
			{
				if ("_round_id_".equals(name)) {
					return testRoundId;
				}

				if ("_ticking_seconds_".equals(name)) {
					return testSeconds;
				}

				return -1;
			}

			@Mock
			String getAction()
			{
				return "";
			}
		};
		// :~)

		testService.onStartCommand(new Intent(), -1, -1);

		new Verifications() {{
			mockTickingWorker.tickRound(testRoundId, testSeconds);
			times = 1;
		}};
	}

	/**
	 * Tests the implementation of {@link TickingWorker#onTick} while there is
	 * no registered {@link TickingPublisher.RoundObserver}.<p>
	 */
	@Test(dataProvider="OnTickWithoutObserver")
	public void onTickWithoutObserver(final Set<TickingRound> testEndedRounds)
	{
		/**
		 * Mock the rounds in pool
		 */
		final TimerService testService = buildTestService();
		final TickingWorker tickingWorker = Deencapsulation.getField(testService, TickingWorker.class);

		new NonStrictExpectations() {
			TickingPool mockTickingPool;

			{
				tickingWorker.getTickingPool();
				result = mockTickingPool;

				mockTickingPool.consumeEndedRounds();
				result = testEndedRounds;
			}
		};
		// :~)

		/**
		 * Verify the intent which is used to start Activity
		 */
		if (testEndedRounds.size() > 0) {
			new MockUp<Intent>() {
				@Mock(invocations=1)
				void $init(Context context, Class<TeaTimeActivity> classOfActivity)
				{
					Assert.assertEquals(classOfActivity, TeaTimeActivity.class);
				}
				@Mock(invocations=1)
				Intent putExtra(String name, int[] value)
				{
					Assert.assertEquals(name, "_id_of_ended_rounds_");
					Assert.assertEquals(value.length, testEndedRounds.size());

					return getMockInstance();
				}
				@Mock(invocations=1)
				Intent addFlags(int flags)
				{
					Assert.assertEquals(flags, Intent.FLAG_ACTIVITY_NEW_TASK);

					return getMockInstance();
				}
			};
		}
		// :~)

		Deencapsulation.invoke(tickingWorker, "onTick");

		/**
		 * Verify whether "onTick" starts TeaTimeActivity or not
		 */
		new Verifications() {{
			mockStubTimerService.startActivity((Intent)any);
			times = testEndedRounds.size() > 0 ? 1 : 0;
		}};
		// :~)
	}
	@DataProvider(name="OnTickWithoutObserver")
	private Object[][] getOnTickWithoutObserver()
	{
		Set<TickingRound> towEndedRounds = new HashSet<TickingRound>();
		towEndedRounds.add(new TickingRound(-1, 0));
		towEndedRounds.add(new TickingRound(-2, 0));

		return new Object[][] {
			{ Collections.<TickingRound>emptySet() },
			{ towEndedRounds }
		};
	}

	@Mocked @mockit.Capturing(maxInstances=1)
	private TickingPublisher.RoundObserver mockObserver;
	/**
	 * Tests the implementation of {@link TickingWorker#onTick} while there a
	 * registered {@link TickingPublisher.RoundObserver}.<p>
	 */
	@Test(dataProvider="OnTickWithObserver")
	public void onTickWithObserver(
		final Set<TickingRound> testAliveRounds, final Set<TickingRound> testEndedRounds
	) {
		TimerService testService = buildTestService();

		/**
		 * Mock the rounds in pool
		 */
		final TickingWorker tickingWorker = Deencapsulation.getField(testService, "tickingWorker");

		new NonStrictExpectations() {
			TickingPool mockTickingPool;

			{
				tickingWorker.getTickingPool();
				result = mockTickingPool;

				mockTickingPool.getAliveRounds();
				result = testAliveRounds;

				mockTickingPool.consumeEndedRounds();
				result = testEndedRounds;
			}
		};
		// :~)

		/**
		 * Verify the times of notification to observer
		 */
		TickingPublisher tickingPublisher = Deencapsulation.getField(testService, "tickingPublisher");
		tickingPublisher.registerRoundObserver(mockObserver);
		// :~)

		Deencapsulation.invoke(tickingWorker, "onTick");

		new Verifications() {{
			mockObserver.roundChanged((TickingRound)any);
			times = testAliveRounds.size() + testEndedRounds.size();
		}};
	}
	@DataProvider(name="OnTickWithObserver")
	private Object[][] getOnTickWithObserver()
	{
		Set<TickingRound> twoTickingRounds = new HashSet<TickingRound>();
		twoTickingRounds.add(new TickingRound(-1, 0));
		twoTickingRounds.add(new TickingRound(-2, 0));

		return new Object[][] {
			{ Collections.<TickingRound>emptySet(), Collections.<TickingRound>emptySet() }, // Nothing in pool
			{ twoTickingRounds, Collections.<TickingRound>emptySet() }, // Pool merely has ticking rounds
			{ Collections.<TickingRound>emptySet(), twoTickingRounds }, // Pool merely has ended rounds
			{ twoTickingRounds, twoTickingRounds } // Pool has ticking rounds and eded rounds
		};
	}

	private TimerService buildTestService()
	{
		Mockit.stubOut(Looper.class);
		Mockit.stubOut(Handler.class);

		/**
		 * Create and initiate timer service
		 */
		TimerService testService = new TimerService();
		Deencapsulation.setField(testService, mockWakeLock);
		Deencapsulation.setField(
			testService,
			Deencapsulation.newInnerInstance("TickingWorkerImpl", testService)
		);
		Deencapsulation.setField(
			testService,
			Deencapsulation.newInnerInstance("TickingPublisherImpl", testService)
		);

		new NonStrictExpectations() {{
			invoke(mockTickingWorker, "stillRunning");
			result = true;
		}};
		// :~)

		return testService;
	}
}
