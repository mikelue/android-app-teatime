package idv.mikelue.teatime.database;

import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import idv.mikelue.teatime.R;
import idv.mikelue.teatime.model.*;

/**
 * This object provides essential operations of data for this app.<p>
 *
 * @see TeaTimeSQLiteOpenHelper
 */
public class TeaTimeDao extends AbstractDaoBase {
	private final static String TAG = TeaTimeDao.class.getSimpleName();

	/**
	 * Constructs this DAO by context.<p>
	 */
	public TeaTimeDao(Context context)
	{
		super(context);
	}

	/**
	 * Creates database if there is no existing one.<p>
	 */
	public void initDatabase()
	{
		getSQLiteDatabase();
	}

	/**
	 * Lists the sessions.<p>
	 *
	 * @return The sessions which are sorted by their time of last using
	 */
	public Cursor listSessions()
	{
		return getSQLiteDatabase().rawQuery(
			getResources().getString(R.string.sql_dml_listSessions), null
		);
	}

	/**
	 * Lists the round of a session.<p>
	 *
	 * @param sessionId The id of session
	 *
	 * @return The list of rounds sorted by their sequence(ascending)
	 */
	public Cursor listRounds(int sessionId)
	{
		return getSQLiteDatabase().rawQuery(
			getResources().getString(R.string.sql_dml_listRounds),
			new String[] { String.valueOf(sessionId) }
		);
	}

	/**
	 * Adds a new session into database.<p>
	 *
	 * @param newSession The data of session
	 * @param newRounds The rounds belong to new session
	 *
	 * @see InMemorySOHOfRoundEditor
	 */
	public void addNewSession(Session newSession, Cursor newRounds)
	{
		Log.d(TAG, String.format("Add session([%s]) with [%d] rounds", newSession.getName(), newRounds.getCount()));

		SQLiteDatabase db = getSQLiteDatabase();
		db.beginTransaction();

		try {
			/**
			 * Insert the data of session into database
			 */
			db.execSQL(
				getResources().getString(R.string.sql_dml_insertSession),
				new Object[] {
					newSession.getName(),
					newSession.getIconType().getDatabaseValue()
				}
			);
			// :~)

			/**
			 * Insert the data of rounds into database
			 */
			int newSessionId = getLastInsertRowId();
			Long lastUsedTime = buildRounds(newSessionId, newRounds, false);
			// :~)

			/**
			 * Update the time at which the session is last used
			 */
			updateTimeOfLastUsing(newSessionId, lastUsedTime);
			// :~)

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	/**
	 * Updates data of a session.<p>
	 *
	 * @param modifiedSession The new data of session
	 * @param stopgapRounds The stopgap rounds of this session, null if the
	 * 		rounds doesn't need to be updated
	 */
	public void modifySession(Session modifiedSession, Cursor stopgapRounds)
	{
		SQLiteDatabase db = getSQLiteDatabase();
		db.beginTransaction();

		try {
			/**
			 * Update the data of session into database
			 */
			db.execSQL(
				getResources().getString(R.string.sql_dml_updateSession),
				new Object[] {
					modifiedSession.getName(),
					modifiedSession.getIconType().getDatabaseValue(),
					modifiedSession.getId()
				}
			);
			// :~)

			/**
			 * Since the updating of rounds has its effort, this null-condition
			 * provides an opportunity to improve performance.
			 */
			if (stopgapRounds != null) {
				/**
				 * Update the data of rounds into database
				 */
				Long newLastUsedTime = buildRounds(modifiedSession.getId(), stopgapRounds, true);
				// :~)

				/**
				 * Update the time at which the session is last used
				 */
				updateTimeOfLastUsing(modifiedSession.getId(), newLastUsedTime);
				// :~)
			}
			// :~)

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	/**
	 * Removes a session.<p>
	 *
	 * @param sessionId The id of session to be removed
	 */
	public void removeSession(int sessionId)
	{
		Log.d(TAG, String.format("Remove session: [%d]", sessionId));

		getSQLiteDatabase().execSQL(
			getResources().getString(R.string.sql_dml_removeSession),
			new Object[] { sessionId }
		);
	}

	/**
	 * Saves the data for ended round. This method updates the time of last
	 * using to which the round belongs.<p>
	 *
	 * @param roundId The id of ended round
	 */
	public void saveEndedRound(int roundId)
	{
		Log.d(TAG, String.format("Save ended round[%d]", roundId));

		SQLiteDatabase db = getSQLiteDatabase();
		db.beginTransaction();

		try {
			saveEndedRoundWithoutTx(roundId);

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	/**
	 * Skips the round on which the next ticking start.<p>
	 *
	 * @param sessionId The id of session
	 *
	 * @see #resetLastRound
	 */
	public void skipCurrentRound(int sessionId)
	{
		Log.d(TAG, String.format("Skip round for session[%d]", sessionId));

		SQLiteDatabase db = getSQLiteDatabase();
		db.beginTransaction();

		try {
			Cursor nextRoundId = db.rawQuery(
				getResources().getString(R.string.sql_dml_getNextRoundIdOfSession),
				new String[] {
					String.valueOf(sessionId),
					String.valueOf(sessionId)
				}
			);
			nextRoundId.moveToFirst();

			saveEndedRoundWithoutTx(nextRoundId.getInt(nextRoundId.getColumnIndex("sr_id")));

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}
	/**
	 * Resets the round on which the last ticking started.<p>
	 *
	 * The resetting would be at first round of session if there is no ended
	 * round in it.<p>
	 *
	 * @param sessionId The id of session
	 *
	 * @see #skipCurrentRound
	 */
	public void resetLastRound(int sessionId)
	{
		Log.d(TAG, String.format("Reset last round for session[%d]", sessionId));

		getSQLiteDatabase().execSQL(
			getResources().getString(R.string.sql_dml_resetLastRound),
			new Object[] { sessionId, sessionId }
		);
	}

	@Override
	protected SQLiteOpenHelper buildSqliteDatabaseHelper()
	{
		return new TeaTimeSQLiteOpenHelper(getContext());
	}

	private void saveEndedRoundWithoutTx(int roundId)
	{
		long currentTime = new Date().getTime();

		SQLiteDatabase db = getSQLiteDatabase();
		db.execSQL(
			getResources().getString(R.string.sql_dml_cleanRecordsOfFinishedSession),
			new Object[] { roundId}
		);
		db.execSQL(
			getResources().getString(R.string.sql_dml_insertTickingRecord),
			new Object[] {
				roundId, currentTime
			}
		);
		db.execSQL(
			getResources().getString(R.string.sql_dml_updateTimeOfLastUsingFromRound),
			new Object[] {
				currentTime, roundId
			}
		);
	}

	/**
	 * Updates the time of session at which it is last used.<p>
	 *
	 * @param sessionId The id of session
	 * @param unixTime The time(UNIX time formate)
	 */
	private void updateTimeOfLastUsing(int sessionId, Long unixTime)
	{
		getSQLiteDatabase().execSQL(
			getResources().getString(R.string.sql_dml_updateTimeOfLastUsing),
			new Object[] { unixTime, sessionId }
		);
	}

	/**
	 * Builds the rounds of session in database.<p>
	 *
	 * @param sessionId The id of session
	 * @param stopgapRounds The new data from stopgpa rounds
	 * @param rebuild Whether or not to remove existing rounds
	 */
	private Long buildRounds(int sessionId, Cursor stopgapRounds, boolean rebuild)
	{
		final int columnIndexOfTimeOfLastReaching = stopgapRounds.getColumnIndex("rd_time_last_reached");
		Long lastUsedTime = null;

		SQLiteDatabase db = getSQLiteDatabase();

		/**
		 * Remove all of the rounds existing in session before build others
		 * from stopgap data
		 */
		if (rebuild) {
			db.execSQL(
				getResources().getString(R.string.sql_dml_removeRoundsInSession),
				new Object[] { sessionId }
			);
			/**
			 * Since the triiger(AFTER DELETE) can't make an effective update of catched number of ended rounds,
			 * this SQL reset the value directly.
			 */
			db.execSQL(
				getResources().getString(R.string.sql_dml_resetNumberOfEndedRounds),
				new Object[] { sessionId }
			);
			// :~)
		}
		// :~)

		stopgapRounds.moveToFirst();
		for (int i = 0; i < stopgapRounds.getCount(); i++) {
			db.execSQL(
				getResources().getString(R.string.sql_dml_insertRound),
				new Object[] {
					sessionId,
					i + 1, // Sequence of round
					stopgapRounds.getInt(stopgapRounds.getColumnIndex("rd_seconds"))
				}
			);

			/**
			 * Insert the ticking record
			 */
			if (!stopgapRounds.isNull(columnIndexOfTimeOfLastReaching)) {
				int newRoundId = getLastInsertRowId();

				db.execSQL(
					getResources().getString(R.string.sql_dml_insertTickingRecord),
					new Object[] {
						newRoundId, stopgapRounds.getLong(columnIndexOfTimeOfLastReaching)
					}
				);

				lastUsedTime = stopgapRounds.getLong(columnIndexOfTimeOfLastReaching);
			}
			// :~)

			stopgapRounds.moveToNext();
		}

		return lastUsedTime;
	}

	private int getLastInsertRowId()
	{
		Cursor dataForRowId = getSQLiteDatabase().rawQuery("SELECT last_insert_rowid()", null);
		dataForRowId.moveToFirst();
		int lastInsertRowId = dataForRowId.getInt(0);
		dataForRowId.close();

		return lastInsertRowId;
	}
}
