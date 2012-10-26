package idv.mikelue.teatime.database;

import java.util.Date;

import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import mockit.Deencapsulation;

import idv.mikelue.teatime.R;
import static idv.mikelue.teatime.model.RoundInfo.MAX_SECONDS;
import static idv.mikelue.teatime.model.RoundInfo.MIN_SECONDS;

public class StopgapRoundDaoTest extends AndroidTestCase {
	private final static String TAG = StopgapRoundDaoTest.class.getSimpleName();

	public StopgapRoundDaoTest() {}

	@Override
	public void setUp() throws Exception
	{
		super.setUp();
	}

	@Override
	public void tearDown() throws Exception
	{
		if (testDao != null) {
			testDao.release();
		}

		super.tearDown();
	}

	/**
	 * Tests the modifiation of seconds for round.<p>
	 *
	 * This test ensures that the seconds of affected consecutive rounds won't
	 * be less than 1 second.<p>
	 */
	public void testSetSecondsOfRound()
	{
		/**
		 * Simple addition of seconds for simple cases
		 */
		runAndAssertSetSecondsOfRound(
			new int[] { 10 }, 1, 3, false, new int[] { 13 }
		);
		runAndAssertSetSecondsOfRound(
			new int[] { 10 }, 1, -3, false, new int[] { 7 }
		);
		// :~)

		/**
		 * Addition of seconds for simple cases without affecting consecutive rounds
		 */
		runAndAssertSetSecondsOfRound(
			new int[] { 10, 20 }, 1, 3, false, new int[] { 13, 20 }
		);
		runAndAssertSetSecondsOfRound(
			new int[] { 10, 20 }, 1, -3, false, new int[] { 7, 20 }
		);
		// :~)

		/**
		 * Addition of seconds for simple cases with affecting consecutive rounds
		 */
		runAndAssertSetSecondsOfRound(
			new int[] { 10, 20, 30, 40 }, 2, 4, true, new int[] { 10, 24, 34, 44 }
		);
		runAndAssertSetSecondsOfRound(
			new int[] { 10, 20, 30, 40 }, 2, -4, true, new int[] { 10, 16, 26, 36 }
		);
		// :~)

		/**
		 * Addition of seconds for out of range cases to consecutive rounds
		 */
		runAndAssertSetSecondsOfRound(
			new int[] { 10, MAX_SECONDS}, 1, 1, true, new int[] { 11, MAX_SECONDS }
		);
		runAndAssertSetSecondsOfRound(
			new int[] { 10, MIN_SECONDS}, 1, -1, true, new int[] { 9, MIN_SECONDS }
		);
		// :~)
	}

	private void runAndAssertSetSecondsOfRound(
		int[] testSeconds, int testIdOfRound,
		int addedValue, boolean affectConsecutiveRounds, int[] expectedSeconds
	) {
		Log.d(TAG, String.format("[runAndAssertSetSecondsOfRound] Test Seconds:[%s] Round Id: [%d]", testSeconds, testIdOfRound));

		/**
		 * Prepare test data
		 */
		for (int i = 0; i < testSeconds.length; i++) {
			addPersistedRound(i + 1, i + 1, testSeconds[i], 0L);
		}
		// :~)

		getTestDao().setSecondsOfRound(testIdOfRound, testSeconds[testIdOfRound - 1] + addedValue, affectConsecutiveRounds);

		Cursor result = getRoundsSortedBySequence();

		/**
		 * Assert result data
		 */
		for (int i = 0; i < expectedSeconds.length; i++) {
			assertEquals(expectedSeconds[i], result.getInt(result.getColumnIndex("rd_seconds")));
			result.moveToNext();
		}
		// :~)

		getTestDao().release();
	}

	/**
	 * Tests the synchronization from persisted data.<p>
	 *
	 * This test also ensure that the fake id of rounds is the number next to
	 * maximum id of source data.<p>
	 */
	public void testSyncRound()
	{
		/**
		 * Prepare the source of persisted data
		 */
		long testTime = new Date().getTime();

		MatrixCursor testPersistedData = new MatrixCursor(new String[] {
			"sr_id", "sr_sequence", "sr_seconds", "lt_time_last_reached"
		});

		final int maximumSourceId = 17;

		testPersistedData.addRow(new Object[] {
			13, 1, 55, null
		});
		testPersistedData.addRow(new Object[] {
			maximumSourceId, 2, 75, testTime
		});
		// :~)

		getTestDao().syncRounds(testPersistedData);

		/**
		 * Assert the copied data
		 */
		Cursor result = getRoundsSortedBySequence();
		assertEquals(2, result.getCount());
		// :~)

		/**
		 * Assert for 1st round
		 */
		result.moveToPosition(0);
		assertEquals(13, result.getInt(0)); // 1st round
		assertTrue(result.isNull(3));
		// :~)

		/**
		 * Assert for 2nd round
		 */
		result.moveToPosition(1);
		assertEquals(maximumSourceId, result.getInt(0));
		assertEquals(testTime, result.getLong(3));
		// :~)

		assertEquals(maximumSourceId + 1, Deencapsulation.getField(getTestDao(), "nextFakeRoundId"));
	}

	/**
	 * Tests the copying of round(insert a new one after copied one).<p>
	 */
	public void testCopyRoundAndInsert()
	{
		final int beginSequence = 3;

		// As a new session
		addPersistedRound(1, beginSequence, 10, null);
		Deencapsulation.setField(getTestDao(), "nextFakeRoundId", 2);

		Cursor result;

		/**
		 * Add a round by copying 1st round
		 */
		getTestDao().copyRoundAndInsert(1);

		result = getRoundsSortedBySequence();
		assertEquals(2, result.getCount());
		assertSequenceAt(result, 1, beginSequence + 1);
		// :~)

		/**
		 * Add a round by copying 1st round(again)
		 */
		getTestDao().copyRoundAndInsert(1);

		result = getRoundsSortedBySequence();
		assertEquals(3, result.getCount());
		assertSequenceAt(result, 1, beginSequence + 1);
		// :~)

		/**
		 * Add a round by copying 3rd round
		 */
		getTestDao().copyRoundAndInsert(3);

		result = getRoundsSortedBySequence();
		assertEquals(4, result.getCount());
		assertSequenceAt(result, 3, beginSequence + 3);
		// :~)
	}
	private void assertSequenceAt(Cursor cursor, int position, int expectedValue)
	{
		cursor.moveToPosition(position);
		assertEquals(expectedValue, cursor.getInt(cursor.getColumnIndex("rd_sequence")));
	}

	/**
	 * Tests listing of rounds.<p>
	 */
	public void testListRounds()
	{
		final int[] expectedSeconds = new int[] {
			21, 32, 24, 66, 55
		};

		/**
		 * Prepare data
		 */
		addPersistedRound(6, 1, expectedSeconds[0], null);
		addPersistedRound(8, 2, expectedSeconds[1], null);
		addPersistedRound(9, 3, expectedSeconds[2], null);
		addPersistedRound(10, 4, expectedSeconds[3], null);
		addPersistedRound(11, 5, expectedSeconds[4], null);

		Deencapsulation.setField(getTestDao(), "nextFakeRoundId", 10); // The testing of fake id for new round is at testSyncRound()
		// :~)

		Cursor result = getTestDao().listRounds();

		/**
		 * Assert the number of rounds
		 */
		assertEquals(5, result.getCount());
		result.moveToFirst();
		// :~)

		/**
		 * Assert the sequence of rounds
		 */
		int colIdxOfTime = result.getColumnIndex("rd_seconds");
		for (int i = 0; i < expectedSeconds.length; i++) {
			assertEquals(expectedSeconds[i], result.getInt(colIdxOfTime));
			result.moveToNext();
		}
		// :~)
	}

	private void assertRound(Cursor cursorOfRound, int expectedId, int expectedSequence, int expectedSeconds)
	{
		assertEquals(expectedId, cursorOfRound.getInt(0));
		assertEquals(expectedSequence, cursorOfRound.getInt(1));
		assertEquals(expectedSeconds, cursorOfRound.getInt(2));
	}
	private Cursor getRoundsSortedBySequence()
	{
		Cursor result = getUsingSqliteDatabase().query(
			"sg_round", new String[] { "rd_id", "rd_sequence", "rd_seconds", "rd_time_last_reached" },
			null, null, null, null,
			"rd_sequence ASC"
		);

		if (result.getCount() > 0) {
			result.moveToFirst();
		}

		return result;
	}

	/**
	 * Tests the building of rounds for new session.<p>
	 */
	public void testBuildRoundsForNewSession()
	{
		getTestDao().buildRoundsForNewSession();

		assertEquals(2, getRoundsSortedBySequence().getCount());
	}

	/**
	 * Tests resetting of round.<p>
	 */
	public void testResetRound()
	{
		long testTime = new Date().getTime();

		/**
		 * Preapre existing data
		 */
		addPersistedRound(6, 1, 21, testTime);
		addPersistedRound(8, 2, 32, testTime + 20000);
		addPersistedRound(9, 3, 43, testTime + 40000);
		// :~)

		getTestDao().resetRound(8);

		Cursor result = getRoundsSortedBySequence();

		/**
		 * Assert 1st round
		 */
		assertFalse(result.isNull(3));
		// :~)

		/**
		 * Assert 2nd and 3rd rounds
		 */
		result.moveToPosition(1);
		assertTrue(result.isNull(3));
		result.moveToPosition(2);
		assertTrue(result.isNull(3));
		// :~)
	}

	/**
	 * Tests skipping of round.<p>
	 */
	public void testSkipRound()
	{
		/**
		 * Prepare test data
		 */
		final long unaffectedTimeOfLastReaching = 4723872L;
		addPersistedRound(100, 1, 99, unaffectedTimeOfLastReaching);
		addPersistedRound(101, 2, 99, null);
		addPersistedRound(102, 3, 99, null);
		addPersistedRound(103, 4, 99, null);
		// :~)

		getTestDao().skipRound(101);
		Cursor result = getRoundsSortedBySequence();

		/**
		 * Assert the unaffected round(has time of last reaching)
		 */
		assertEquals(unaffectedTimeOfLastReaching, result.getLong(3));
		// :~)

		/**
		 * Assert the skept rounds
		 */
		result.moveToPosition(0);
		assertFalse(result.isNull(3));
		result.moveToPosition(1);
		assertFalse(result.isNull(3));
		// :~)

		/**
		 * Assert the unaffected round
		 */
		result.moveToPosition(2);
		assertTrue(result.isNull(3));
		result.moveToPosition(3);
		assertTrue(result.isNull(3));
		// :~)
	}

	/**
	 * Tests removal of round.<p>
	 */
	public void testRemoveRound()
	{
		Cursor result;

		/**
		 * Prepare test data
		 */
		addPersistedRound(1, 1, 10, 0L);
		addPersistedRound(2, 10, 20, 0L);
		addPersistedRound(3, 11, 30, 0L);

		result = getRoundsSortedBySequence();
		assertEquals(3, result.getCount());
		// :~)

		getTestDao().removeRound(2);

		/**
		 * Prepare test data
		 */
		result = getRoundsSortedBySequence();
		assertEquals(2, result.getCount());
		// :~)
	}

	private void addPersistedRound(int roundId, int sequence, int seconds, Long lastReachedTime)
	{
		getUsingSqliteDatabase().execSQL(
			getResources().getString(R.string.sql_dml_inmemory_syncRound),
			new Object[] {
				roundId, sequence, seconds, lastReachedTime
			}
		);
	}

	/**
	 * Builds the DAO object to be tested.<p>
	 */
	private StopgapRoundDao testDao;
	private StopgapRoundDao getTestDao()
	{
		if (testDao == null) {
			testDao = new StopgapRoundDao(getContext());
		}

		return testDao;
	}

	private Resources getResources()
	{
		return getContext().getResources();
	}
	private SQLiteDatabase getUsingSqliteDatabase()
	{
		return getTestDao().getSQLiteDatabase();
	}
}
