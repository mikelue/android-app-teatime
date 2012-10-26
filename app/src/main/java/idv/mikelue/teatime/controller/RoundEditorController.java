package idv.mikelue.teatime.controller;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.widget.ListView;

import idv.mikelue.teatime.R;
import idv.mikelue.teatime.database.StopgapRoundDao;
import idv.mikelue.teatime.database.TeaTimeDao;
import idv.mikelue.teatime.model.Session.IconType;
import idv.mikelue.teatime.model.Session;
import idv.mikelue.teatime.view.RoundCursorAdapter.RoundActionListener;
import idv.mikelue.teatime.view.RoundCursorAdapter;
import static idv.mikelue.teatime.SessionActivity.NEW_SESSION_ID;
import static idv.mikelue.teatime.view.WarningToast.toastSimpleMessage;

/**
 * This controller co-ordinates the loading and editing of rounds.<p>
 *
 * WARNING: The id of loader used in the controoler is 2.<p>
 */
public class RoundEditorController {
	private final static String TAG = "RoundEditorCtrl";

	/**
	 * The limit number of rounds in session.<p>
	 */
	private final static int MIN_NUMBER_OF_ROUNDS = 1;
	private final static int MAX_NUMBER_OF_ROUNDS = 99;
	// :~)

	private final static int LOADER_ID = 2;

	private final Context context;
	private final LoaderManager loaderManager;
	private final TeaTimeDao teaTimeDao;
	private final StopgapRoundDao stopgapDao;
	private RoundCursorAdapter roundAdapter;

	private final Session sourceSession;

	private boolean hasChanged = false;

	/**
	 * Constructs this object with needed services.<p>
	 *
	 * @param activity The activity of hosting this controller
	 * @param newSourceSession The source data of session
	 * @param newTeaTimeDao The DAO to access persisted data
	 * @param newStopgapDao The DAO to perform in-memory modification of database
	 */
	public RoundEditorController(
		FragmentActivity activity, Session newSourceSession,
		TeaTimeDao newTeaTimeDao, StopgapRoundDao newStopgapDao
	) {
		context = activity;
		loaderManager = activity.getSupportLoaderManager();

		sourceSession = newSourceSession;

		teaTimeDao = newTeaTimeDao;
		stopgapDao = newStopgapDao;

		/**
		 * Setup adapter and ListView for sessions
		 */
		roundAdapter = new RoundCursorAdapter(context, null);
		roundAdapter.setThemeColor(sourceSession.getIconType());
		roundAdapter.setActionListener(new RoundActionListenerImpl(this));

		ListView listViewOfRounds = (ListView)activity.findViewById(R.id.round_section);
		listViewOfRounds.setAdapter(roundAdapter);
		// :~)
	}

	/**
	 * Loads the data from persisted database into in-memory database and
	 * affects the adapter.<p>
	 */
	public void init()
	{
		/**
		 * Synchronize the data from persited databae to stopgap database
		 */
		if (sourceSession.getId() != NEW_SESSION_ID) {
			loaderManager.initLoader(LOADER_ID, null, new RoundListLoaderCallbacks()).forceLoad();
			return;
		}
		// :~)

		/**
		 * Build new session
		 */
		Log.d(TAG, "Prepare default data of new session");
		stopgapDao.buildRoundsForNewSession();
		hasChanged = true;
		refreshAdapter();
		// :~)
	}

	/**
	 * Check whether the rounds has been changed.<p>
	 *
	 * @return true if data has been changed
	 */
	public boolean hasChanged()
	{
		return hasChanged;
	}

	/**
	 * Gets the current data of rounds.<p>
	 *
	 * @return The rounds in stopgap database
	 */
	public Cursor getRounds()
	{
		return stopgapDao.listRounds();
	}

	/**
	 * Sets the new color of session.<p>
	 */
	public void setThemeColor(IconType newColor)
	{
		roundAdapter.setThemeColor(newColor);
		refreshAdapter();
	}

	public void release()
	{
		roundAdapter.getCursor().close();
	}

	void addRoundByExistingOne(int idOfExistingRound)
	{
		if (roundAdapter.getCount() >= MAX_NUMBER_OF_ROUNDS) {
			toastSimpleMessage(context, R.string.message_remove_last_round);
			return;
		}

		Log.v(TAG, String.format("Add round by copying from source[%d]", idOfExistingRound));

		hasChanged = true;

		stopgapDao.copyRoundAndInsert(idOfExistingRound);
		refreshAdapter();
	}
	void removeRound(int roundId)
	{
		if (roundAdapter.getCount() <= MIN_NUMBER_OF_ROUNDS) {
			toastSimpleMessage(context, R.string.message_remove_last_round);
			return;
		}

		hasChanged = true;

		Log.v(TAG, String.format("Remove round[%d]", roundId));

		stopgapDao.removeRound(roundId);
		refreshAdapter();
	}
	void resetRound(int roundId)
	{
		Log.d(TAG, String.format("Reset round(Stopgap): [%d]", roundId));

		hasChanged = true;

		stopgapDao.resetRound(roundId);
		refreshAdapter();
	}
	void skipRound(int roundId)
	{
		Log.d(TAG, String.format("Skip round(Stopgap): [%d]", roundId));

		hasChanged = true;

		stopgapDao.skipRound(roundId);
		refreshAdapter();
	}
	void setTimeOfRound(int roundId, int newSeconds, boolean affectSuccessiveRounds)
	{
		Log.d(TAG, String.format("Change Time. Round:[%d] Seconds:[%d] Affect successive rounds:[%s]", roundId, newSeconds, affectSuccessiveRounds));

		hasChanged = true;

		stopgapDao.setSecondsOfRound(roundId, newSeconds, affectSuccessiveRounds);
		refreshAdapter();
	}

	private void refreshAdapter()
	{
		Cursor newData = stopgapDao.listRounds();
		roundAdapter.setNumberOfRounds(newData.getCount());
		roundAdapter.changeCursor(newData);
	}

	/**
	 * Copies the persisted to in-memory database for modification of rounds.<p>
	 */
	private class RoundListLoaderCallbacks implements LoaderCallbacks<Integer> {
		RoundListLoaderCallbacks() {}

		@Override
		public Loader<Integer> onCreateLoader(int id, Bundle args)
		{
			return new AsyncTaskLoader(context) {
				@Override
				public Integer loadInBackground()
				{
					/**
					 * Sync the persisted data to in-memory database
					 */
					Log.d(TAG, "Sync data from persisted database");
					Cursor persistedData = teaTimeDao.listRounds(sourceSession.getId());
					int numberOfData = persistedData.getCount();
					stopgapDao.syncRounds(persistedData);
					persistedData.close();
					// :~)

					return numberOfData;
				}
			};
		}
		@Override
		public void onLoadFinished(Loader<Integer> loader, Integer data)
		{
			Log.d(TAG, String.format("Sync [%d] rounds to stopgap database", data));

			loaderManager.destroyLoader(LOADER_ID);

			refreshAdapter();
		}
		@Override
		public void onLoaderReset(Loader<Integer> loader) {}
	}
}

/**
 * Process the actions on rounds triggered by user.<p>
 */
class RoundActionListenerImpl implements RoundActionListener {
	private RoundEditorController roundCtrl;

	RoundActionListenerImpl(RoundEditorController newSessionCtrl)
	{
		roundCtrl = newSessionCtrl;
	}

	/**
	 * Adds a round to clicked item. The new data of round is as same as the
	 * clicked item<p>
	 */
	@Override
	public void addBy(int roundIdOfSource)
	{
		roundCtrl.addRoundByExistingOne(roundIdOfSource);
	}
	/**
	 * Removes a round. The last round in session can't be removed.<p>
	 */
	@Override
	public void remove(int roundId)
	{
		roundCtrl.removeRound(roundId);
	}
	@Override
	public void reset(int roundId)
	{
		roundCtrl.resetRound(roundId);
	}
	@Override
	public void skip(int roundId)
	{
		roundCtrl.skipRound(roundId);
	}
	@Override
	public void setTime(int roundId, int newSeconds, boolean affectSuccessiveRounds)
	{
		roundCtrl.setTimeOfRound(roundId, newSeconds, affectSuccessiveRounds);
	}
}
