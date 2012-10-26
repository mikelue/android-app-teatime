package idv.mikelue.teatime.ticking;

import java.lang.ref.WeakReference;

import android.util.Log;

/**
 * This thread executes loop endlessly and calls {@link TickObserver#tick}
 * for every {@link TickingAccuracy#TICKING_RATE} milliseconds. Client should implement {@link TickObserver} to receive
 * the tick.<p>
 *
 * @see TickObserver
 */
public class TickingRunnable implements Runnable {
	/**
	 * Callback interface for ticking of every {@link TickingAccuracy#TICKING_RATE} milliseconds.<p>
	 */
	public interface TickObserver {
		/**
		 * {@link TickingRunnable} would call this method while it wakes up for
		 * every {@link TickingAccuracy#TICKING_RATE} milliseconds.<p>
		 */
		public void tick();
	}

	private final static String TAG = TickingRunnable.class.getSimpleName();

	private boolean stopFlag = false;
	private WeakReference<TickObserver> weakRefObserver;

	/**
	 * Constructs this object with observer, which is notified while this
	 * thread is running.<p>
	 *
	 * @param newObserver The observer of ticking
	 */
	public TickingRunnable(TickObserver newObserver)
	{
		weakRefObserver = new WeakReference(newObserver);
	}

	/**
	 * Checks whether this thread is acquired for stopping.<p>
	 *
	 * @see #stopTicking
	 *
	 * @return true if this thread has beed acquired for stopping
	 */
	public boolean isTickingStopped()
	{
		return stopFlag;
	}

	/**
	 * Stops this thread, which will stop the endless loop by waking up of next
	 * tick.<p>
	 *
	 * @see #isTickingStopped
	 */
	public void stopTicking()
	{
		stopFlag = true;
	}

	/**
	 * Executes an endless loop and calls {@link TickObserver#tick} for every
	 * {@link TickingAccuracy#TICKING_RATE} milliseconds.<p>
	 */
	@Override
	public void run()
	{
		Log.d(TAG, "Ticking thread is started");
		while (true) {
			try {
				Thread.sleep(TickingAccuracy.TICKING_RATE);
			} catch (InterruptedException e) {
				Log.w(TAG, "Ticking thread is interrupted");
				return;
			}

			/**
			 * Check the flag of stopping
			 */
			if (stopFlag) {
				Log.d(TAG, "Ticking thread is stopped");
				break;
			}
			// :~)

			/**
			 * Ensure that the observer is not recycled by Android.
			 */
			TickObserver observer = weakRefObserver.get();
			if (observer == null) {
				Log.v(TAG, "Observer has been recycled");
				break;
			}
			// :~)

			Log.d(TAG, "Notify tick");
			observer.tick();
		}
	}
}
