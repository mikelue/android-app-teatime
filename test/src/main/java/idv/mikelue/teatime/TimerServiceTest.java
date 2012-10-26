package idv.mikelue.teatime;

import java.util.List;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Looper;
import android.test.ServiceTestCase;

import mockit.Deencapsulation;

import idv.mikelue.teatime.model.TickingRound;

public class TimerServiceTest extends ServiceTestCase<TimerService> {
	private final static String TAG = TimerServiceTest.class.getSimpleName();

	public TimerServiceTest()
	{
		super(TimerService.class);
	}

	/**
	 * Tests the content of binder from {@link TimerService#onBind}, and
	 * expects there is no ticking round in service.<p>
	 */
	public void testBinder()
	{
		assertEquals(
			0,
			bindService().getTickingPublisher().getTickingRounds().size()
		);
	}

	/**
	 * Tests the getting of alive rounds.<p>
	 */
	public void testGetTickingRoundsOfPublisher() throws InterruptedException
	{
		Thread newThread = prepreMainThread();

		/**
		 * Start the service with 2 rounds
		 */
		TimerServiceBinder binder = bindService();

		Intent testIntent = new Intent(getContext(), TimerService.class);
		TimerService.setTickingRound(testIntent, new TickingRound(-2, 60));

		TimerService.setTickingRound(testIntent, new TickingRound(-10, 7));
		startService(testIntent);
		TimerService.setTickingRound(testIntent, new TickingRound(-11, 7));
		startService(testIntent);

		Thread.sleep(250); // Wait for ready of pool
		// :~)

		assertEquals(2, binder.getTickingPublisher().getTickingRounds().size());

		newThread.interrupt();
	}

	/**
	 * Tests the notification over {@link TickingPublisher.RoundObserver}.<p>
	 */
	public void testRoundObserver() throws InterruptedException
	{
		Thread newThread = prepreMainThread();

		/**
		 * Bind service
		 */
		TimerServiceBinder binder = bindService();

		SensibleRoundObserver testRoundObserver = new SensibleRoundObserver();
		binder.getTickingPublisher().registerRoundObserver(testRoundObserver);
		// :~)

		final int testRoundId = 2;

		/**
		 * Start the service with a 0 second round
		 */
		Intent testIntent = new Intent(getContext(), TimerService.class);
		TimerService.setTickingRound(testIntent, new TickingRound(testRoundId, 100));
		startService(testIntent);
		// :~)

		Thread.sleep(1500);

		/**
		 * Assert the content of notification
		 */
		assertTrue(testRoundObserver.sensed);
		assertEquals(testRoundId, testRoundObserver.roundId);
		// :~)

		newThread.interrupt();
	}

	/**
	 * Executes a round(0 second) and you should check the log for
	 * Service.stopSelf().(with no registered observer)<p>
	 */
	public void testOnDestroyForNoTicking() throws InterruptedException
	{
		Thread newThread = prepreMainThread();

		/**
		 * Start the service with a 0 second round
		 */
		Intent testIntent = new Intent(getContext(), TimerService.class);
		TimerService.setTickingRound(testIntent, new TickingRound(1, 100));
		startService(testIntent);
		// :~)

		Thread.sleep(1000);

		newThread.interrupt();
	}

	private Thread prepreMainThread()
	{
		Thread newThread = new Thread() {
			@Override
			public void run()
			{
				super.run();
				Looper.prepareMainLooper();
				Looper.loop();
			}
		};
		newThread.start();
		return newThread;
	}

	private TimerServiceBinder bindService()
	{
		return (TimerServiceBinder)bindService(new Intent(getContext(), TimerService.class));
	}
}

class SensibleOfStartActivityContext extends ContextWrapper {
	boolean sensed = false;

	SensibleOfStartActivityContext(Context ordinaryContext)
	{
		super(ordinaryContext);
	}

	@Override
	public void startActivity(Intent intent)
	{
		super.startActivity(intent);

		sensed = true;
	}
}

class SensibleRoundObserver implements TickingPublisher.RoundObserver {
	int roundId = -1;
	boolean sensed = false;

	public void roundChanged(TickingRound tickingRound)
	{
		sensed = true;
		roundId = tickingRound.getId();
	}
}
