package idv.mikelue.teatime.ticking;

import android.os.Handler;

import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import idv.mikelue.teatime.test.AbstractMockAndroidEnvTestBase;

public class TickingWorkerTest extends AbstractMockAndroidEnvTestBase {
	private Handler mockHandler;

	/**
	 * Setups the handler to wait posted thread finished in current thread.</p>
	 */
	@BeforeClass
	private void setupMockHandler()
	{
		new MockUp<Handler>() {
			@Mock
			void $init() {}
			@Mock
			boolean post(Runnable runnable)
			{
				Thread threadOfWorker = new Thread(runnable);
				threadOfWorker.start();

				try {
					threadOfWorker.join();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}

				return true;
			}
		};

		mockHandler = new Handler();
	}

	public TickingWorkerTest() {}

	/**
	 * Tests the adding of first round and the worker should start the ticking
	 * thread.<p>
	 */
	@Test
	public void addFirstRound() throws InterruptedException
	{
		/**
		 * Start the worker by adding first round
		 */
		TickingWorker testWorker = new SimpleTickingWorker(mockHandler);
		testWorker.tickRound(-1, 10);
		// :~)

		/**
		 * Gets the status of ticking thread and interrupted it
		 */
		Thread tickingThread = Deencapsulation.getField(testWorker, Thread.class);
		boolean isAlive = tickingThread.isAlive();
		tickingThread.interrupt();
		// :~)

		Assert.assertEquals(testWorker.getTickingPool().getNumberOfAliveRounds(), 1); // Ensure that the ticking round has been added
		Assert.assertTrue(isAlive); // Ensure that the ticking thread had started
	}

	/**
	 * Tests the releasing of worker.<p>
	 */
	@Test
	public void demolish() throws InterruptedException
	{
		/**
		 * Start the worker by adding first round
		 */
		TickingWorker testWorker = new SimpleTickingWorker(mockHandler);
		testWorker.tickRound(-1, 20);
		Thread tickingThread = Deencapsulation.getField(testWorker, Thread.class);
		// :~)

		testWorker.demolish();
		Thread.sleep(1000); // Wait for interrupting of system

		/**
		 * Ensure that all of the resources are released
		 */
		Assert.assertFalse(tickingThread.isAlive());
		Assert.assertNull(testWorker.getTickingPool());
		// :~)
	}

	/**
	 * Tests the added rounds has been ended and the ticking thread is
	 * stopped.<p>
	 */
	@Test(dependsOnMethods="addFirstRound")
	public void afterAllRoundsAreEnded()
	{
		/**
		 * Add two test rounds which have 0 seconds(stop the ticking thread as
		 * soon as possible), and wait for the ticking thread of worker is stopped
		 */
		TickingWorker testWorker = new SimpleTickingWorker(mockHandler);
		testWorker.tickRound(-1, 0);
		testWorker.tickRound(-2, 0);

		waitTickingThreadStoped(testWorker);
		// :~)

		Assert.assertEquals(testWorker.getTickingPool().consumeEndedRounds().size(), 2); // Ensure that there are two ended rounds
	}

	/**
	 * Tests the cancelation of round.<p>
	 */
	@Test(dependsOnMethods="afterAllRoundsAreEnded")
	public void cancelRound() throws InterruptedException
	{
		/**
		 * Prepare ticking rounds
		 */
		TickingWorker testWorker = new SimpleTickingWorker(mockHandler);
		testWorker.tickRound(-1, 20);
		testWorker.tickRound(-2, 20);
		// :~)

		Thread tickingThread = Deencapsulation.getField(testWorker, Thread.class);

		/**
		 * Cancel first round:
		 * 1. The thread should be running
		 * 2. The pool left 1 ticking round
		 */
		testWorker.cancelRound(-1);

		Assert.assertTrue(tickingThread.isAlive());
		Assert.assertEquals(testWorker.getTickingPool().getNumberOfAliveRounds(), 1);
		// :~)

		/**
		 * Cancel second round:
		 * 1. The thread should be stopped
		 * 2. The pool has no ticking round
		 * 3. The pool has no ended round
		 */
		testWorker.cancelRound(-2);
		tickingThread.join(); // Wait for the stopping of ticking thread

		Assert.assertEquals(testWorker.getTickingPool().getNumberOfAliveRounds(), 0);
		Assert.assertEquals(testWorker.getTickingPool().consumeEndedRounds().size(), 0);
		// :~)
	}

	/**
	 * Tests the callback of {@link TickingWorker#onTick}.<p>
	 */
	@Test(dependsOnMethods="afterAllRoundsAreEnded")
	public void onTick()
	{
		SenseTickTickingWorker testWorker = new SenseTickTickingWorker(mockHandler);

		/**
		 * Start the worker
		 */
		testWorker.tickRound(-1, 1);
		waitTickingThreadStoped(testWorker);
		// :~)

		Assert.assertTrue(testWorker.ticked); // Ensure onTick() has been called
	}

	private void waitTickingThreadStoped(TickingWorker testWorker)
	{
		Thread tickingThread = Deencapsulation.getField(testWorker, Thread.class);

		try {
			tickingThread.join(); // Wait for the stopping of ticking thread
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}

class SenseTickTickingWorker extends TickingWorker {
	boolean ticked = false;

	SenseTickTickingWorker(Handler handler)
	{
		super(handler);
	}

	@Override
	protected void onTick()
	{
		ticked = true;
	}
}
class SimpleTickingWorker extends TickingWorker {
	SimpleTickingWorker(Handler handler)
	{
		super(handler);
	}

	@Override
	protected void onTick() {}
}
