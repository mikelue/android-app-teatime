package idv.mikelue.teatime.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import idv.mikelue.teatime.R;
import static idv.mikelue.teatime.model.RoundInfo.MAX_SECONDS;
import static idv.mikelue.teatime.model.RoundInfo.MIN_SECONDS;

/**
 * Provides in-memory query/modification for stopgap data of round.<p>
 *
 * @see InMemorySOHOfRoundEditor
 */
public class StopgapRoundDao extends AbstractDaoBase {
	private final static String TAG = StopgapRoundDao.class.getSimpleName();

	private int nextFakeRoundId = 1;

	/**
	 * Constructs this DAO by context.<p>
	 */
	public StopgapRoundDao(Context context)
	{
		super(context);
	}

	/**
	 * Lists data of rounds.<p>
	 *
	 * @return The data of rounds sorted by their sequence(ascending)
	 */
	public Cursor listRounds()
	{
		return getSQLiteDatabase().rawQuery(
			getResources().getString(R.string.sql_dml_inmemory_listRounds),
			null
		);
	}

	/**
	 * Copies a round and insert after the copied one.<p>
	 *
	 * @param copiedRoundId The id of round to be copied
	 */
	public void copyRoundAndInsert(int copiedRoundId)
	{
		Log.d(TAG, String.format("Add round from copying round[%d]", copiedRoundId));

		SQLiteDatabase db = getSQLiteDatabase();
		db.beginTransaction();

		try {
			/**
			 * 1. Reset the ended rounds since the new round
			 * 2. Increase the sequence of consecutive rounds
			 */
			db.execSQL(
				getResources().getString(R.string.sql_dml_inmemory_putBackConsecutiveRounds),
				new Object[] {
					copiedRoundId
				}
			);
			// :~)
			/**
			 * 3. Copy the data of new round
			 */
			db.execSQL(
				getResources().getString(R.string.sql_dml_inmemory_copyRound),
				new Object[] {
					nextFakeRoundId, copiedRoundId
				}
			);

			++nextFakeRoundId;
			// :~)

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	/**
	 * Synchronizes the rounds which are persisted in database.<p>
	 *
	 * This method initiates the fake id of round for insertion of new
	 * round.<p>
	 *
	 * @param persistedRounds The data of rounds
	 */
	public void syncRounds(Cursor persistedRounds)
	{
		Log.d(TAG, String.format("Sync [%d] rounds", persistedRounds.getCount()));

		persistedRounds.moveToFirst();

		int indexOfLastUsedTime = persistedRounds.getColumnIndex("lt_time_last_reached");

		SQLiteDatabase db = getSQLiteDatabase();
		db.beginTransaction();
		try {
			while (!persistedRounds.isAfterLast()) {
				/**
				 * Initiate the counter for generating over fake id of round
				 */
				int persistedRoundId = persistedRounds.getInt(persistedRounds.getColumnIndex("sr_id"));

				if (persistedRoundId > nextFakeRoundId) {
					nextFakeRoundId = persistedRoundId;
				}
				// :~)

				db.execSQL(
					getResources().getString(R.string.sql_dml_inmemory_syncRound),
					new Object[] {
						persistedRoundId,
						persistedRounds.getInt(persistedRounds.getColumnIndex("sr_sequence")),
						persistedRounds.getInt(persistedRounds.getColumnIndex("sr_seconds")),
						persistedRounds.isNull(indexOfLastUsedTime) ? null :
							persistedRounds.getLong(indexOfLastUsedTime)
					}
				);

				persistedRounds.moveToNext();
			}

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}

		++nextFakeRoundId;
	}

	/**
	 * Removes a round.<p>
	 *
	 * @param roundId The id of round to be removed
	 */
	public void removeRound(int roundId)
	{
		Log.d(TAG, String.format("Remove round[%d]", roundId));

		getSQLiteDatabase().execSQL(
			getResources().getString(R.string.sql_dml_inmemory_removeRound),
			new Object[] { roundId }
		);
	}

	/**
	 * Skips a round and its predecessor rounds.<p>
	 *
	 * @param roundId The id of round to be skipped
	 */
	public void skipRound(int roundId)
	{
		Log.d(TAG, String.format("Skip round[%d]", roundId));

		getSQLiteDatabase().execSQL(
			getResources().getString(R.string.sql_dml_inmemory_skipPredecessorRounds),
			new Object[] { roundId }
		);
	}

	/**
	 * Resets a round and its consecutive rounds.<p>
	 */
	public void resetRound(int roundId)
	{
		Log.d(TAG, String.format("Reset round[%d]", roundId));

		getSQLiteDatabase().execSQL(
			getResources().getString(R.string.sql_dml_inmemory_resetConsecutiveRounds),
			new Object[] { roundId }
		);
	}

	/**
	 * Builds the data of rounds for new session.<p>
	 */
	public void buildRoundsForNewSession()
	{
		SQLiteDatabase db = getSQLiteDatabase();

		db.beginTransaction();
		try {
			for (String sql: getResources().getStringArray(R.array.sql_dml_inmemory_defaultRounds)) {
				db.execSQL(
					sql,
					new Object[] {
						nextFakeRoundId++
					}
				);
			}

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	/**
	 * Sets the seconds of round.<p>
	 *
	 * @param roundId The id of round to be set
	 * @param seconds The value of seconds
	 * @param affectConsecutiveRounds true if the seconds of consecutive rounds
	 *	to be increased/decresed respectively
	 */
	public void setSecondsOfRound(int roundId, int seconds, boolean affectConsecutiveRounds)
	{
		Log.d(TAG, String.format(
			"Set round[%d] to [%d] seconds. Affect consecutive rounds: [%s]",
			roundId, seconds, affectConsecutiveRounds
		));

		int changedBySeconds = 0;
		int sequenceOfRound = Integer.MAX_VALUE;

		SQLiteDatabase db = getSQLiteDatabase();

		/**
		 * Get the sequence of modified round and calculate the seconds by
		 * which it change consecutive rounds
		 */
		if (affectConsecutiveRounds) {
			Cursor result = db.rawQuery(
				getResources().getString(R.string.sql_dml_inmemory_findRound),
				new String[] {
					String.valueOf(roundId)
				}
			);
			result.moveToFirst();

			sequenceOfRound = result.getInt(result.getColumnIndex("rd_sequence"));
			changedBySeconds = seconds - result.getInt(result.getColumnIndex("rd_seconds"));

			result.close();
		}
		// :~)

		db.beginTransaction();
		try {
			db.execSQL(
				getResources().getString(R.string.sql_dml_inmemory_setSeconds),
				new Object[] { seconds, roundId }
			);

			/**
			 * Change the seconds of consecutive rounds by increased/decreased value
			 */
			if (affectConsecutiveRounds) {
				db.execSQL(
					getResources().getString(R.string.sql_dml_inmemory_changeSecondsOfConsecutiveRoundsBy),
					new Object[] {
						MIN_SECONDS, MAX_SECONDS,
						changedBySeconds, sequenceOfRound
					}
				);
			}
			// :~)

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	@Override
	protected SQLiteOpenHelper buildSqliteDatabaseHelper()
	{
		return new InMemorySOHOfRoundEditor(getContext());
	}
}
