package idv.mikelue.teatime;

import java.util.Set;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import idv.mikelue.teatime.model.TickingRound;
import idv.mikelue.teatime.ticking.TickingWorker;

/**
 * Executes ticking thread even the Activity has been stopped by user.
 * However, this service is not activated by {@link Service#startForeground}
 * because it seems rare that the process of this app is recyclied while it
 * is ticking.<p>
 */
public class TimerService extends Service {
	private final static String TAG = TimerService.class.getSimpleName();

	private final static String START_INTENT_ROUND_ID = "_round_id_";
	private final static String START_INTENT_TICKING_SECONDS = "_ticking_seconds_";

	public TimerService() {}

	/**
	 * Sets the id of round to intent used to {@link #onStartCommand start} this
	 * service.<p>
	 *
	 * @param tickingRound The object of ticking round
	 *
	 * @see #setTickingSeconds
	 */
	public static void setTickingRound(Intent intent, TickingRound tickingRound)
	{
		intent.putExtra(START_INTENT_ROUND_ID, tickingRound.getId());
		intent.putExtra(START_INTENT_TICKING_SECONDS, tickingRound.getRemainSeconds());
	}

	/**
	 * Binds the main thread of this process to {@link TickingWorker}, which
	 * uses that thread as {@link Handler} to perform thread-safe ticking
	 * operations.<p>
	 */
	private TickingPublisherImpl tickingPublisher;
	private TickingController tickingController;
	private TickingWorker tickingWorker;

	private TimerServiceBinder binder;
	private PowerManager.WakeLock wakeLock;

	@Override
	public void onCreate()
	{
		super.onCreate();
		Log.i(TAG, "Timer service created");

		/**
		 * Setup the power manager to prevent sleep of device
		 */
		PowerManager pm = (PowerManager)getSystemService(POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "TeaTime");
		// :~)

		/**
		 * Setup the worker of ticking
		 */
		tickingWorker = new TickingWorkerImpl();
		tickingPublisher = new TickingPublisherImpl();
		tickingController = new TickingControllerImpl();
		// :~)

		binder = new TimerServiceBinder(tickingPublisher, tickingController);
	}

	/**
	 * Client should use this method({@link #startService}) to require ticking
	 * a round.<p>
	 *
	 * See {@link #setRoundId} and {@link #setTickingSeconds} for the contained
	 * data of intent.<p>
	 *
	 * @param intent see {@link #setRoundId} and {@link #setTickingSeconds}
	 * @param flags irrelevant to this method
	 * @param startId irrelevant to this method
	 *
	 * @return {@link #START_NOT_STICKY}
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		/**
		 * This service is started
		 */
		if (Intent.ACTION_RUN.equals(intent.getAction())) {
			return START_NOT_STICKY;
		}
		// :~)

		Log.d(TAG,
			String.format(
				"Handle ticking request: Round id: [%d], Seconds: [%d]",
				intent.getIntExtra(START_INTENT_ROUND_ID, -1),
				intent.getIntExtra(START_INTENT_TICKING_SECONDS, 0)
			)
		);

		/**
		 * If the needed data of intent is insufficient,
		 * this would tick a round with 0 second, or invalid id of round.
		 */
		tickingWorker.tickRound(
			intent.getIntExtra(START_INTENT_ROUND_ID, -1),
			intent.getIntExtra(START_INTENT_TICKING_SECONDS, 0)
		);
		// :~)

		if (!wakeLock.isHeld()) {
			Log.i(TAG, "Acquire the wake lock");
			wakeLock.acquire();
		}

		return START_NOT_STICKY;
	}

	/**
	 * Provides {@link TimerServiceBinder} consisting {@link TickingPublisher}
	 * and {@link TickingController} object.<p>
	 *
	 * @param intent nothing needed
	 *
	 * @return object of {@link TimerServiceBinder}
	 */
	@Override
    public IBinder onBind(Intent intent)
	{
		Log.i(TAG, "Bind to timer service");
        return binder;
    }

	/**
	 * Releases the handler(by main thread of this process) and resoureces of
	 * {@link TickingWorker}.<p>
	 *
	 * To be convenient for testing, this method could be called by multiple
	 * times safely.<p>
	 */
	@Override
	public void onDestroy()
	{
		Log.i(TAG, "Destroy of service");

		binder.release();
		binder = null;

		tickingPublisher = null;
		tickingController = null;

		tickingWorker.demolish();
		tickingWorker = null;

		if (wakeLock.isHeld()) {
			wakeLock.release();
		}
	}

	private class TickingWorkerImpl extends TickingWorker {
		protected TickingWorkerImpl()
		{
			super(new Handler(Looper.getMainLooper()));
		}

		/**
		 * Sends the information of ticking rounds to registered {@link TickingPublisher#RoundObserver}
		 * or starts {@link TeaTimeActivity} for ended rounds if there is no
		 * registered observer.<p>
		 */
		@Override
		protected void onTick()
		{
			/**
			 * Release the holding of device on
			 */
			if (!stillRunning() && wakeLock.isHeld()) {
				Log.i(TAG, "Release the wake lock");
				wakeLock.release();
			}
			// :~)

			if (tickingPublisher.roundObserver == null) {
				Set<TickingRound> endedRounds = getTickingPool().consumeEndedRounds();
				if (endedRounds.size() == 0) {
					return;
				}

				/**
				 * Start TeaTimeActivity for alarm of ended rounds
				 */
				Log.i(TAG, "Calling of TeaTimeActivity for ended rounds");

				Intent startActivityForAlarm = new Intent(getApplicationContext(), TeaTimeActivity.class);
				startActivityForAlarm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				TeaTimeActivity.putEndedIdOfRounds(startActivityForAlarm, convertToIntArray(endedRounds));
				startActivity(startActivityForAlarm);
				// :~)
			} else {
				Log.i(TAG, "Send information of rounds to observer");

				/**
				 * Send the ended rounds first, then send alive rounds
				 */
				for (TickingRound endedRound: tickingWorker.getTickingPool().consumeEndedRounds()) {
					tickingPublisher.roundObserver.roundChanged(endedRound);
				}
				for (TickingRound aliveRound: tickingWorker.getTickingPool().getAliveRounds()) {
					tickingPublisher.roundObserver.roundChanged(aliveRound);
				}
				// :~)
			}
		}

		private int[] convertToIntArray(Set<TickingRound> endedRounds)
		{
			int[] idOfEndedRounds = new int[endedRounds.size()];

			int i = 0;
			for (TickingRound endedRound: endedRounds) {
				idOfEndedRounds[i++] = endedRound.getId();
			}

			return idOfEndedRounds;
		}
	}

	private class TickingControllerImpl implements TickingController {
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void stopTicking(int roundId)
		{
			tickingWorker.cancelRound(roundId);
		}
	}

	/**
	 * This publisher supports only one {@link TickingPublisher.RoundObserver}
	 * currently.<p>
	 */
	private class TickingPublisherImpl implements TickingPublisher {
		TickingPublisher.RoundObserver roundObserver = null;

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void registerRoundObserver(RoundObserver newObserver)
		{
			roundObserver = newObserver;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void unregisterRoundObserver(RoundObserver registeredObserver)
		{
			roundObserver = null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Set<TickingRound> getTickingRounds()
		{
			return tickingWorker.getTickingPool().getAliveRounds();
		}
	}
}
