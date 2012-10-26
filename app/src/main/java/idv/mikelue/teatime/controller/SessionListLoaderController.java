package idv.mikelue.teatime.controller;

import java.util.Observable;
import java.util.Observer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import idv.mikelue.teatime.R;
import idv.mikelue.teatime.SessionActivity;
import idv.mikelue.teatime.TimerService;
import idv.mikelue.teatime.database.TeaTimeDao;
import idv.mikelue.teatime.model.DetailTickingRound;
import idv.mikelue.teatime.model.RoundInfo;
import idv.mikelue.teatime.model.Session.IconType;
import idv.mikelue.teatime.model.TickingRound;
import idv.mikelue.teatime.view.TeaTimeListAdapter.SessionActionListener;
import idv.mikelue.teatime.view.TeaTimeListAdapter;
import idv.mikelue.teatime.view.TickingSection;
import static idv.mikelue.teatime.TeaTimeActivity.DEFAULT_CONCURRENT_TICKING_ROUNDS;

/**
 * This object controls interaction between {@Link CursorAdapter} and {@link LoaderCallbacks}.<p>
 *
 * WARNING: The value of loader's id is 1
 */
public class SessionListLoaderController extends Observable {
	private final static String TAG = "SessionListCtrl";
	private final static int LOADER_ID__SESSION_LIST = 1;

	private final Context context;
	private final LoaderManager loaderManager;
	private final TeaTimeDao teaTimeDao;

	private final TeaTimeListAdapter adapterOfList;
	private final TickingSection tickingSection;
	private final Handler handler;

	private final Map<Integer, DetailTickingRound> rounds = new HashMap<Integer, DetailTickingRound>();

	/**
	 * Initializes by source DAO object and target adapter.<p>
	 */
	public SessionListLoaderController(FragmentActivity activity, TeaTimeDao newTeaTimeDao)
	{
		context = activity;
		loaderManager = activity.getSupportLoaderManager();
		teaTimeDao = newTeaTimeDao;

		adapterOfList = new TeaTimeListAdapter(context);
		adapterOfList.setSessionActionListener(new SessionActionListenerImpl());
		ListView listView = (ListView)activity.findViewById(R.id.session_list);
		listView.setAdapter(adapterOfList);

		tickingSection = (TickingSection)activity.findViewById(R.id.section_ticking);

		activity.findViewById(R.id.button_add_new_session)
			.setOnClickListener(new AddNewSessionListener());

		handler = new Handler();
	}

	/**
	 * Gets the data of round by id.<p>
	 *
	 * @param roundId The id of round
	 */
	public DetailTickingRound getRound(int roundId)
	{
		return rounds.get(roundId);
	}

	/**
	 * Initialize the loader, the repeated calling of this method is safe.<p>
	 *
	 * The usage of observer is same as {@link Observable#addObserver}, but the
	 * observer will be removed while this this controller is ready.<p>
	 *
	 * @param observer The observer wants to know when this controller is ready
	 */
	void initLoader(Observer observer)
	{
		Log.v(TAG, "Initialize loader of session list");
		loaderManager.initLoader(LOADER_ID__SESSION_LIST, null, loaderCallbacks);

		addObserver(observer);
	}

	/**
	 * Destroy the loader.<p>
	 */
	void destroyLoader()
	{
		Log.v(TAG, "Destroy loader of session list");

		loaderManager.destroyLoader(LOADER_ID__SESSION_LIST);
	}

	/**
	 * Records the ended of round.<p>
	 *
	 * @param roundIds A batch of ended rounds to be recorded
	 */
	void recordEndedRounds(final int... roundIds)
	{
		/**
		 * Execute the updating to database in asynchroinzed manner
		 */
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params)
			{
				for (int roundId: roundIds)  {
					teaTimeDao.saveEndedRound(roundId);
				}

				return null;
			}

			@Override
			protected void onPostExecute(Void result)
			{
				Log.v(TAG, "Restart loader of session list");
				loaderManager.restartLoader(LOADER_ID__SESSION_LIST, null, loaderCallbacks);
			}
		}.execute();
		// :~)
	}

	/**
	 * Notify this list that a ticking round has been canceled.<p>
	 *
	 * The item of canceled round should be re-showed.<p>
	 */
	void cancelTickingRound(int roundId)
	{
		DetailTickingRound canceledRound = rounds.get(roundId);

		canceledRound.setRemainSeconds(0);
		tickingSection.showTickingRound(canceledRound);

		canceledRound.resetTime(); // Restore the remain time of round
		reshowRound(canceledRound);
	}

	/**
	 * Re-shows the round which had been hidden.<p>
	 *
	 * @param alarmRound The round had alarmed
	 */
	void reshowRound(RoundInfo alarmRound)
	{
		DetailTickingRound detailRoundInfo = (DetailTickingRound)alarmRound;

		adapterOfList.bringInSession(detailRoundInfo.getSessionId());
		adapterOfList.notifyDataSetChanged();
	}

	/**
	 * Sets the ticking round before loading the database from database
	 *
	 * @param roundId The id round
	 */
	private Set<Integer> preHiddenRoundIds = new HashSet<Integer>(DEFAULT_CONCURRENT_TICKING_ROUNDS);
	void prehideItemFromList(int roundId)
	{
		preHiddenRoundIds.add(roundId);
	}

	private LoaderCallbacks<Cursor> loaderCallbacks = new LoaderCallbacks<Cursor>() {
		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args)
		{
			return new CursorLoader(context) {
				@Override
				public Cursor loadInBackground()
				{
					return teaTimeDao.listSessions();
				}
			};
		}
		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor data)
		{
			Log.v(TAG, "Loading data of sessions from database finished");

			/**
			 * Refresh the data of ticking rounds which is ready to tick
			 */
			rounds.clear();
			data.moveToFirst();
			while (!data.isAfterLast()) {
				DetailTickingRound round = new DetailTickingRound(
					data.getInt(data.getColumnIndex("id_of_next_round")),
					data.getInt(data.getColumnIndex("seconds_of_next_round"))
				);
				round.setSessionId(data.getInt(data.getColumnIndex("ss_id")));
				round.setRoundSequence(data.getInt(data.getColumnIndex("next_sequence_of_round")));
				round.setNumberOfRounds(data.getInt(data.getColumnIndex("ss_number_of_rounds")));
				round.setName(data.getString(data.getColumnIndex("ss_name")));
				round.setIconType(IconType.valueOfDatabaseValue(
					data.getInt(data.getColumnIndex("ss_icon_type"))
				));
				rounds.put(round.getId(), round);

				data.moveToNext();
			}
			// :~)

			/**
			 * Notify the observers that this controller has loaded data from database
			 */
			setChanged();
			notifyObservers();
			deleteObservers();
			clearChanged();
			// :~)

			/**
			 * Rule out already ticking rounds
			 */
			for (int idOfAlreadyTickingRounds: preHiddenRoundIds) {
				adapterOfList.ruleOutSession(rounds.get(idOfAlreadyTickingRounds).getSessionId());
			}
			preHiddenRoundIds.clear();
			// :~)

			adapterOfList.changeCursor(data);
		}
		@Override
		public void onLoaderReset(Loader<Cursor> loader)
		{
			rounds.clear();
			adapterOfList.changeCursor(null);
		}
	};

	/**
	 * Handles the events triggered by clicking button on item of session
	 */
	class SessionActionListenerImpl implements SessionActionListener {
		SessionActionListenerImpl() {}

		/**
		 * Handles the clicking of start ticking.<p>
		 *
		 * @param startbutton The button being clicked
		 */
		@Override
		public void startTicking(int roundId)
		{
			Log.d(TAG, String.format("Start ticking round: %d", roundId));

			final DetailTickingRound round = rounds.get(roundId);

			/**
			 * Show the ticking item in dedicated section and
			 * hide the displaying in list
			 */
			adapterOfList.ruleOutSession(round.getSessionId());
			adapterOfList.notifyDataSetChanged();
			tickingSection.showTickingRound(round);
			// :~)

			/**
			 * Ask the service to tick the round
			 */
			handler.post(
				new Runnable() {
					@Override
					public void run()
					{
						Intent tickingIntent = new Intent(context, TimerService.class);
						TimerService.setTickingRound(tickingIntent, new TickingRound(round));
						context.startService(tickingIntent);
					}
				}
			);
			// :~)
		}

		/**
		 * Handles the clicking of setting of a round.<p>
		 *
		 * @param startbutton The button being clicked
		 */
		@Override
		public void settingSession(int roundId)
		{
			Log.v(TAG, String.format("Setup round: %d",  roundId));

			DetailTickingRound detailTickingRound = getRound(roundId);

			/**
			 * Start the activity with choosed session
			 */
			Intent startSessionActivity = new Intent(context, SessionActivity.class);
			startSessionActivity.setAction(Intent.ACTION_EDIT);
			SessionActivity.setSessionData(startSessionActivity, detailTickingRound.getSession());

			context.startActivity(startSessionActivity);
			// :~)
		}
	};
}

class AddNewSessionListener implements View.OnClickListener {
	@Override
	public void onClick(View button)
	{
		Context context = button.getContext();

		// Start session activity with nothing
		Intent intent = new Intent(context, SessionActivity.class);
		intent.setAction(Intent.ACTION_INSERT);

		context.startActivity(intent);
	}
};
