package idv.mikelue.teatime.ticking;

import android.os.Handler;
import android.util.Log;

import idv.mikelue.teatime.model.TickingRound;

/**
 * This class combines {@link TickingRunnable}, {@link TickingPool} and {@link Handler} to
 * provide complex interaction of multiple ticking rounds concurrently.<p>
 *
 * The thread of {@link TickingRunnable} is acquired for stopping if there is
 * no any alive round in {@link TickingPool}. Otherwise, a new thread of
 * {@link TickingRunnable} is started by first alive round of {@link TickingPool}.<p>
 *
 * Client must extend this class to perform operations of ticking.
 */
public abstract class TickingWorker {
	private final static String TAG = TickingWorker.class.getSimpleName();

	private TickingRunnable.TickObserver tickingObserver = new DecreaseTimeObserver();
	private Handler handler = null;
	private TickingRunnable tickingRunnable = null;
	private Thread tickingThread = null;

	private TickingPool tickingPool = new TickingPool();

	/**
	 * Gets the pool of ticking rounds.<p>
	 *
	 * @return The pool which performs decrease by trigger from {@link TickingRunnable}.<p>
	 */
	public final TickingPool getTickingPool()
	{
		return tickingPool;
	}

	/**
	 * Starts the ticking of round.<p>
	 *
	 * @param roundId The id of round
	 * @param seconds The seconds of round
	 *
	 * @see #cancelRound
	 */
	public void tickRound(final int roundId, final int seconds)
	{
		Log.d(TAG, String.format("Start tick a round: [%d]", roundId));

		handler.post(new Runnable() {
			@Override
			public void run()
			{
				tickingPool.addTickingRound(new TickingRound(roundId, seconds));
				startTickingThread();
			}
		});
	}
	/**
	 * Cancels an alive round.<p>
	 *
	 * @param roundId The id of round to be canceled
	 *
	 * @see #tickRound
	 */
	public void cancelRound(final int roundId)
	{
		Log.d(TAG, String.format("Cancel a ticking round: [%d]", roundId));

		handler.post(new Runnable() {
			@Override
			public void run()
			{
				tickingPool.removeAliveRound(roundId);
				stopTickingThreadIfPoolIsEmpty();
			}
		});
	}

	/**
	 * Kills this object, including interruptintg the running thread of
	 * ticking, and releasing the round pool.<p>
	 *
	 * This method should be called by {@link android.app.Service#onDestroy()}.<p>
	 */
	public void demolish()
	{
		if (tickingThread != null && tickingThread.isAlive()) {
			Log.i(TAG, "Stop running thread of ticking");
			tickingRunnable.stopTicking();
			tickingThread.interrupt();
		}

		tickingPool = null;
		handler = null;
		tickingRunnable = null;
	}

	/**
	 * Gets whether the ticking thread of this worker is still running.<p>
	 *
	 * @return ture if the ticking thread is running
	 */
	protected boolean stillRunning()
	{
		return !tickingRunnable.isTickingStopped();
	}

	/**
	 * Construct this object by {@link Handler}, which is the way to guarantee
	 * thread-safe.<p>
	 */
	protected TickingWorker(Handler newHandler)
	{
		handler = newHandler;
	}

	/**
	 * Client must implement this method to be notified the tick from {@link TickingRunnable}.<p>
	 */
	protected abstract void onTick();

	private void startTickingThread()
	{
		/**
		 * Wait for previous started thread to stop.<p>
		 */
		if (tickingRunnable != null && tickingRunnable.isTickingStopped() &&
			tickingThread != null
		) {
			while (
				tickingThread.isAlive()
			) {
				Log.v(TAG, "Waiting for stopping of previous ticking thread");

				try {
					tickingThread.join();
				} catch (InterruptedException e) {
					Log.w(TAG, "Waiting for stopping has been interrupt" + e.toString());
				}
			}

			Log.d(TAG, "Get rid of previous stopped ticking thread");
			tickingThread = null;
		}
		// :~)

		/**
		 * There is an existing ticking thread
		 */
		if (tickingThread != null) {
			return;
		}
		// :~)

		Log.d(TAG, "Start ticking thread");
		tickingRunnable = new TickingRunnable(tickingObserver);
		tickingThread = new Thread(tickingRunnable);
		tickingThread.start();
	}
	/**
	 * If the pool of ticking rounds is empty, cancel the thread of ticking.<p>
	 */
	private void stopTickingThreadIfPoolIsEmpty()
	{
		if (
			tickingPool.getNumberOfAliveRounds() == 0 &&
			!tickingRunnable.isTickingStopped()
		) {
			Log.d(TAG, "Stop ticking thread");
			tickingRunnable.stopTicking();
		}
	}

	/**
	 * This observer perform the decrease in time triggered by ticking thread.<p>
	 */
	private class DecreaseTimeObserver implements TickingRunnable.TickObserver {
		@Override
		public void tick()
		{
			handler.post(new Runnable() {
				@Override
				public void run()
				{
					Log.v(TAG, "Handle decrease in time");
					tickingPool.performDecrease();
					stopTickingThreadIfPoolIsEmpty();
					onTick();
				}
			});
		}
	}
}
