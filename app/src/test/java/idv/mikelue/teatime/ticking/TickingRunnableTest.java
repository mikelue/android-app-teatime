package idv.mikelue.teatime.ticking;

import org.testng.Assert;
import org.testng.annotations.Test;

import idv.mikelue.teatime.test.AbstractMockAndroidEnvTestBase;

public class TickingRunnableTest extends AbstractMockAndroidEnvTestBase {
	public TickingRunnableTest() {}

	/**
	 * Tests the function of stop.<p>
	 */
	@Test
	public void stopable() throws InterruptedException
	{
		/**
		 * Run the thread of ticking
		 */
		TickingRunnable testTickingRunnable = new TickingRunnable(
			new TickingRunnable.TickObserver() {
				@Override
				public void tick() {}
			}
		);
		Thread testThread = runAndWaitForFirstTick(testTickingRunnable);
		// :~)

		testTickingRunnable.stopTicking();
		Assert.assertFalse(waitOrInterruptRunnable(testThread)); // Ensure that the ticking thread is not alive
	}

	/**
	 * Tests the callback of observer.<p>
	 */
	@Test(dependsOnMethods="stopable")
	public void observer() throws InterruptedException
	{
		IfCalledObserver observer = new IfCalledObserver();

		/**
		 * Run the thread of ticking
		 */
		TickingRunnable testTickingRunnable = new TickingRunnable(observer);
		runAndWaitForFirstTick(testTickingRunnable);

		testTickingRunnable.stopTicking();
		// :~)

		Assert.assertTrue(observer.hasTicked); // Ensure that the ticking has been observed
	}

	private Thread runAndWaitForFirstTick(Runnable tickingRunnable) throws InterruptedException
	{
		Thread testThread = new Thread(tickingRunnable);
		testThread.start();
		Thread.sleep(TickingAccuracy.TICKING_RATE * 2); // Wait for first tick

		return testThread;
	}
	private boolean waitOrInterruptRunnable(Thread thread) throws InterruptedException
	{
		Thread.sleep(TickingAccuracy.TICKING_RATE * 2); // Wait for end of ticking thread

		boolean isAlive = thread.isAlive();
		if (isAlive) {
			thread.interrupt();
		}

		return isAlive;
	}
}

class IfCalledObserver implements TickingRunnable.TickObserver {
	IfCalledObserver() {}

	boolean hasTicked = false;

	@Override
	public void tick()
	{
		hasTicked = true;
	}
}
