package idv.mikelue.teatime.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import idv.mikelue.teatime.R;
import idv.mikelue.teatime.AppPreferences;
import idv.mikelue.teatime.TickingController;
import idv.mikelue.teatime.TickingPublisher;
import idv.mikelue.teatime.TimerService;
import idv.mikelue.teatime.TimerServiceBinder;
import idv.mikelue.teatime.model.DetailTickingRound;
import idv.mikelue.teatime.model.RoundInfo;
import idv.mikelue.teatime.model.TickingRound;
import idv.mikelue.teatime.view.AlarmSection;
import idv.mikelue.teatime.view.TickingSection;
import idv.mikelue.teatime.view.TickingSection.CancelTickingListener;
import static idv.mikelue.teatime.TeaTimeActivity.DEFAULT_CONCURRENT_TICKING_ROUNDS;

/**
 * This controller is used to handle connectio with {@link TimerService} and
 * registeration of ticking observer.<p>
 */
public class ObserveTimerServiceController {
	private final static String TAG = "ObserverCtrl";

	private final Context context;
	private final TickingSection tickingSection;
	private final AlarmSection alarmSection;
	private final SessionListLoaderController sessionLoaderCtrl;
	private final RingtoneController ringtoneCtrl;
	private final DeferAlarmPool deferAlarmPool;

	private AppPreferences appPreferences;

	/**
	 * Handling classes for timer service
	 */
	private ServiceConnection timerServiceConn = null;
	private TickingPublisher tickingPublisher = null;
	private TickingPublisher.RoundObserver tickingRoundObserver = null;
	private TickingController tickingController = null;
	private int[] justEndedRoundsIds = null;
	// :~)

	/**
	 * Constructs this object with the context of activity and controlled view
	 * of ticking section.<p>
	 *
	 * @param newContext The context of activity
	 * @param newSessionLoaderCtrl The controller used to get data of rounds
	 * @param newJustEndedRoundIds The id of rounds which are just ended
	 */
	public ObserveTimerServiceController(Activity activity, SessionListLoaderController newSessionLoaderCtrl, int[] newJustEndedRoundIds)
	{
		/**
		 * Initialize base service/data/views controlled by this object
		 */
		context = activity;
		sessionLoaderCtrl = newSessionLoaderCtrl;

		tickingSection = (TickingSection)activity.findViewById(R.id.section_ticking);
		alarmSection = (AlarmSection)activity.findViewById(R.id.section_alarm);

		setupPreferences();
		ringtoneCtrl = new RingtoneController(context, appPreferences);
		// :~)

		/**
		 * The calling of additional data by timer service
		 */
		justEndedRoundsIds = newJustEndedRoundIds;
		// :~)

		setupAlarmSection();
		deferAlarmPool = new DeferAlarmPool(
			sessionLoaderCtrl, ringtoneCtrl, alarmSection
		);
	}

	/**
	 * Binds the service by context this object.<p>
	 */
	public void bindService()
	{
		Log.v(TAG, "Start timer service");

		/**
		 * Binds this activity to the timer service and start it
		 */
		timerServiceConn = new TimerServiceConnection();
		context.bindService(new Intent(context, TimerService.class), timerServiceConn, 0);

		Intent startServiceIntent = new Intent(context, TimerService.class);
		startServiceIntent.setAction(Intent.ACTION_RUN);
		context.startService(startServiceIntent);

		sessionLoaderCtrl.initLoader(new Observer() {
			@Override
			public void update(Observable o, Object arg)
			{
				// Notify the defered pool that the data of sessions is ready
				deferAlarmPool.setDataLoaded();
			}
		});
		// :~)
	}

	/**
	 * Unbinds service and release resources.<p>
	 */
	public void unbindService()
	{
		Log.v(TAG, "Unbind timer service");

		// Stop the ringtone
		ringtoneCtrl.forceStop();

		/**
		 * Unbind with the timer service
		 */
		clearInterfaceOfService();
		context.unbindService(timerServiceConn);
		// :~)

		// Release resource of list of session
		sessionLoaderCtrl.destroyLoader();

		/**
		 * Clean the displaying of ticking/alarm section
		 */
		tickingSection.clean();
		alarmSection.clean();
		// :~)
	}

	private void setupPreferences()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		appPreferences = AppPreferences.init(prefs);

		alarmSection.setAlarmingSeconds(appPreferences.getAlarmDuration());

		Log.d(TAG, String.format("Prefs: Sound:[%s] Vibration:[%s] Duration:[%d]",
			appPreferences.isAlarmWithSound(),
			appPreferences.isAlarmWithVibration(),
			appPreferences.getAlarmDuration()
		));
	}
	private void setupAlarmSection()
	{
		alarmSection.setCheckAlarmCallback(new CheckAlarmCallbackImpl(
			alarmSection, ringtoneCtrl, sessionLoaderCtrl
		));
	}

	private void clearInterfaceOfService()
	{
		if (tickingPublisher != null) {
			tickingPublisher.unregisterRoundObserver(tickingRoundObserver);
		}

		tickingRoundObserver = null;
		tickingPublisher = null;
		tickingController = null;
	}

	/**
	 * Handles the observer of ticking rounds and initiates displaying for
	 * ticking rounds.<p>
	 */
	private class TimerServiceConnection implements ServiceConnection {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			Log.v(TAG, "Timer service connected");

			TimerServiceBinder binder = (TimerServiceBinder)service;

			setupServiceInterface(binder);
			setupTickingSection();

			/**
			 * Hide the displaying of item in session list for ticking rounds
			 */
			for (TickingRound tickingRound: tickingPublisher.getTickingRounds()) {
				sessionLoaderCtrl.prehideItemFromList(tickingRound.getId());
			}
			// :~)

			/**
			 * The activity is raised by service in background,
			 * update the value of ended round
			 */
			if (justEndedRoundsIds != null) {
				for (int roundId: justEndedRoundsIds) {
					Log.d(TAG, String.format("Just ended round: [%d]", roundId));

					sessionLoaderCtrl.prehideItemFromList(roundId);
					deferAlarmPool.putEndedRound(roundId);
				}

				justEndedRoundsIds = null;
			}
			// :~)
		}
		@Override
		public void onServiceDisconnected(ComponentName name)
		{
			Log.v(TAG, "Disconnected from timer service");

			clearInterfaceOfService();
			sessionLoaderCtrl.destroyLoader();
		}

		private void setupServiceInterface(TimerServiceBinder binder)
		{
			/**
			 * Register the observer to service of timer
			 */
			tickingPublisher = binder.getTickingPublisher();
			tickingRoundObserver = new TickingRoundObserver(
				sessionLoaderCtrl, tickingSection,
				deferAlarmPool
			);
			tickingPublisher.registerRoundObserver(tickingRoundObserver);
			// :~)

			tickingController = binder.getTickingController();
		}
		private void setupTickingSection()
		{
			tickingSection.setCancelTickingListener(new CancelTickingListenerImpl(
				sessionLoaderCtrl, tickingController, tickingSection
			));
		}
	}
}

class TickingRoundObserver implements TickingPublisher.RoundObserver {
	private final static String TAG = TickingRoundObserver.class.getSimpleName();

	private final SessionListLoaderController sessionCtrl;
	private final TickingSection tickingSection;
	private final DeferAlarmPool alarmPool;

	TickingRoundObserver(
		SessionListLoaderController newSessionCtrl, TickingSection newTickingSescion,
		DeferAlarmPool newAlarmPool
	) {
		sessionCtrl = newSessionCtrl;
		tickingSection = newTickingSescion;
		alarmPool = newAlarmPool;
	}

	@Override
	public void roundChanged(TickingRound tickingRound)
	{
		Log.v(TAG, String.format("Ticking for round[%d] seconds[%d]",
			tickingRound.getId(), tickingRound.getRemainSeconds())
		);

		/**
		 * Alarm the ended round
		 */
		if (tickingRound.isEnded()) {
			alarmPool.putEndedRound(tickingRound.getId());
		}
		// :~)

		/**
		 * Since the loading of session list is at another thread,
		 * it is possible that the loading is not ready, but an changing of ticking has arrived.
		 */
		if (!alarmPool.isDataLoaded()) {
			return;
		}
		// :~)

		/**
		 * Update the ticking section
		 */
		DetailTickingRound detailTickingRound = sessionCtrl.getRound(tickingRound.getId());
		detailTickingRound.setRemainSeconds(tickingRound.getRemainSeconds());
		tickingSection.showTickingRound(detailTickingRound);
		// :~)
	}
}

class CheckAlarmCallbackImpl implements AlarmSection.CheckAlarmCallback {
	private final AlarmSection alarmSection;
	private final RingtoneController ringtoneCtrl;
	private final SessionListLoaderController sessionCtrl;

	CheckAlarmCallbackImpl(AlarmSection newAlarmSection, RingtoneController newRingtoneCtrl, SessionListLoaderController newSessionCtrl)
	{
		alarmSection = newAlarmSection;
		ringtoneCtrl = newRingtoneCtrl;
		sessionCtrl = newSessionCtrl;
	}

	@Override
	public void checkAlarm(RoundInfo alarmRound)
	{
		sessionCtrl.reshowRound(alarmRound);

		if (!alarmSection.hasAlarmingRound()) {
			ringtoneCtrl.forceStop();
		}
	}
}

/**
 * Handles the actions while a ticking round has been cancelled.<p>
 */
class CancelTickingListenerImpl implements CancelTickingListener {
	private final SessionListLoaderController sessionCtrl;
	private final TickingController tickingCtrl;
	private final TickingSection tickingSection;
	private final Handler handler = new Handler();

	CancelTickingListenerImpl(
		SessionListLoaderController newSessionCtrl, TickingController newTickingCtrl,
		TickingSection newTickingSection
	) {
		sessionCtrl = newSessionCtrl;
		tickingCtrl = newTickingCtrl;
		tickingSection = newTickingSection;
	}

	/**
	 * Cancel the ticking round.<p>
	 *
	 * <ol>
	 * 	<li>Ask the timer service there is a ticking round is cancelled</li>
	 * 	<li>Notify the list of session that a cancelled round should be showed agagin</li>
	 * <ol>
	 */
	@Override
	public void cancelTicking(final int roundId)
	{
		tickingCtrl.stopTicking(roundId);

		/**
		 * Since there may be a change of ticking round after user cancel the
		 * round, so cancellation is queued to the main(UI) thread in case of
		 * the showing of round again.
		 */
		handler.post(new Runnable() {
			@Override
			public void run()
			{
				sessionCtrl.cancelTickingRound(roundId);
			}
		});
	}
}

/**
 * Since various situations may cause an alarm coming before the data of
 * session's list is ready, this pool queues the ended rounds and starts
 * alarming when the needed data is ready.<p>
 */
class DeferAlarmPool {
	private final List<Integer> pendingAlarms = new ArrayList<Integer>(DEFAULT_CONCURRENT_TICKING_ROUNDS);
	private final AlarmSection alarmSection;
	private final RingtoneController ringtoneCtrl;
	private final SessionListLoaderController sessionCtrl;

	private boolean dataLoaded = false;

	DeferAlarmPool(SessionListLoaderController newSessionCtrl, RingtoneController newRingtoneCtrl, AlarmSection newAlarmSection)
	{
		sessionCtrl = newSessionCtrl;
		alarmSection = newAlarmSection;
		ringtoneCtrl = newRingtoneCtrl;
	}

	boolean isDataLoaded()
	{
		return dataLoaded;
	}

	/**
	 * Puts the round which is ended.<p>
	 */
	void putEndedRound(int roundId)
	{
		if (dataLoaded) {
			startAlarmRound(roundId);
			return;
		}

		// Data is not ready
		pendingAlarms.add(roundId);
	}

	/**
	 * Sets the the data has benn loaded.<p>
	 */
	void setDataLoaded()
	{
		dataLoaded = true;

		if (pendingAlarms.size() == 0) {
			return;
		}

		/**
		 * Flush all of the pending alarms
		 */
		for (int roundId: pendingAlarms) {
			alarmSection.addAlarmRound(sessionCtrl.getRound(roundId));
		}

		sessionCtrl.recordEndedRounds(getRoundIdsAsIntArray());
		ringtoneCtrl.startPlay();
		pendingAlarms.clear();
		// :~)
	}

	private void startAlarmRound(int roundId)
	{
		sessionCtrl.recordEndedRounds(roundId);

		DetailTickingRound round = sessionCtrl.getRound(roundId);
		alarmSection.addAlarmRound(round);

		ringtoneCtrl.startPlay();
	}

	private int[] getRoundIdsAsIntArray()
	{
		int[] pendingIds = new int[pendingAlarms.size()];
		for (int i = 0; i < pendingAlarms.size(); i++) {
			pendingIds[i] = pendingAlarms.get(i);
		}

		return pendingIds;
	}
}
