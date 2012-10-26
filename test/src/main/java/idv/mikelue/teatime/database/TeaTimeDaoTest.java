package idv.mikelue.teatime.database;

import java.util.List;
import java.util.ArrayList;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import idv.mikelue.teatime.model.Session;
import idv.mikelue.teatime.model.TickingRound;
import idv.mikelue.teatime.test.AbstractDatabaseTestBase;

public class TeaTimeDaoTest extends AbstractDatabaseTestBase<TeaTimeDao> {
	private final static String TAG = TeaTimeDaoTest.class.getSimpleName();

	public TeaTimeDaoTest() {}

	/**
	 * Tests the adding of new session(with rounds).<p>
	 */
	public void testAddNewSession()
	{
		runAndAssertAddNewSession(
			buildStopgapRounds(new Object[][] {
				new Object[] {
					1, null, 1, 76
				},
				new Object[] {
					2, null, 2, 44
				},
				new Object[] {
					3, null, 3, 21
				}
			}),
			null
		);
		runAndAssertAddNewSession(
			buildStopgapRounds(new Object[][] {
				new Object[] {
					1, null, 3, 5
				},
				new Object[] {
					7, null, 8, 10
				},
				new Object[] {
					5, null, 11, 15
				}
			}),
			null
		);
		runAndAssertAddNewSession(
			buildStopgapRounds(new Object[][] {
				new Object[] {
					1, 2342323873L, 2, 11
				},
				new Object[] {
					7, 2342323873L, 4, 11
				},
				new Object[] {
					5, null, 6, 11
				}
			}),
			2342323873L
		);
		runAndAssertAddNewSession(
			buildStopgapRounds(new Object[][] {
				new Object[] {
					88, 2342323873L, 102, 60
				},
				new Object[] {
					87, 4612223873L, 103, 50
				},
				new Object[] {
					86, 6692123873L, 104, 40
				}
			}),
			6692123873L
		);
	}
	private void runAndAssertAddNewSession(
		Cursor testStopgapRounds, Long expectedLastUsedTime
	) {
		Log.d(TAG, String.format("Add seesion. [%d] rouns. Last used time: [%s]", testStopgapRounds.getCount(), expectedLastUsedTime));

		/**
		 * Prepare data for testing
		 */
		final String testSessionName = "!SessionName!";
		Session testSession = new Session(testSessionName, Session.IconType.Type5);
		// :~)

		getDaoObject().addNewSession(testSession, testStopgapRounds);

		/**
		 * Assert the new data of session in database
		 */
		Cursor insertedSession = getDaoObject().getSQLiteDatabase().rawQuery(
			"SELECT ss_id, ss_time_last_used FROM tt_session WHERE ss_name = ?",
			new String[] { testSessionName }
		);

		assertEquals(1, insertedSession.getCount());
		insertedSession.moveToFirst();

		if (expectedLastUsedTime == null) {
			assertTrue(insertedSession.isNull(1));
		} else {
			assertEquals(expectedLastUsedTime, new Long(insertedSession.getLong(1)));
		}

		int newSessionId = insertedSession.getInt(0);
		// :~)

		/**
		 * Assert the new data of rounds in database
		 */
		assertRoundsOfSession(newSessionId, testStopgapRounds);
		// :~)

		removeDatabase();
	}

	/**
	 * Tests the modification of session(without rounds).<p>
	 */
	public void testModifySessionSolely()
	{
		final String testSessionName = "!NewSessionName!";
		final Session.IconType testIconType = Session.IconType.Type5;
		final String sqlGetTestSession =
			" SELECT ss_id, ss_name, ss_icon_type FROM tt_session" +
			" WHERE ss_id = 1";

		/**
		 * Prepare test data
		 */
		Session modifiedSession = new Session(
			getDaoObject().getSQLiteDatabase().rawQuery(sqlGetTestSession, null)
		);
		modifiedSession.setName(testSessionName);
		modifiedSession.setIconType(testIconType);
		// :~)

		getDaoObject().modifySession(modifiedSession, null);

		Cursor testRounds = getDaoObject().getSQLiteDatabase().rawQuery(sqlGetTestSession, null);
		testRounds.moveToFirst();

		/**
		 * Assert modified session
		 */
		assertEquals(testSessionName, testRounds.getString(1));
		assertEquals(testIconType, Session.IconType.valueOfDatabaseValue(testRounds.getInt(2)));
		// :~)
	}

	/**
	 * Tests the modification of session(with rounds).<p>
	 *
	 * Since the {@link #testAddNewSession} has tested various cases for data
	 * of rounds, this test soley asserts the simple case of rounds.<p>
	 */
	public void testModifyRoundsOfSession()
	{
		/**
		 * Make the number of ended rounds larger than new data of rounds
		 */
		getDaoObject().getSQLiteDatabase().execSQL(
			"INSERT INTO tt_last_tick VALUES(1, 100);"
		);
		getDaoObject().getSQLiteDatabase().execSQL(
			"INSERT INTO tt_last_tick VALUES(2, 100);"
		);
		getDaoObject().getSQLiteDatabase().execSQL(
			"INSERT INTO tt_last_tick VALUES(3, 100);"
		);
		getDaoObject().getSQLiteDatabase().execSQL(
			"INSERT INTO tt_last_tick VALUES(4, 100);"
		);
		getDaoObject().getSQLiteDatabase().execSQL(
			"INSERT INTO tt_last_tick VALUES(5, 100);"
		);
		// :~)

		/**
		 * Prepare test data
		 */
		final String sqlGetTestSession =
			" SELECT ss_id, ss_name, ss_icon_type FROM tt_session" +
			" WHERE ss_id = 1";

		Session modifiedSession = new Session(
			getDaoObject().getSQLiteDatabase().rawQuery(
				" SELECT ss_id, ss_name, ss_icon_type FROM tt_session" +
				" WHERE ss_id = 1", null
			)
		);
		// :~)

		Cursor testStopgapRounds = buildStopgapRounds(new Object[][] {
			new Object[] {
				1, 234324, 1, 76
			},
			new Object[] {
				2, null, 2, 44
			},
			new Object[] {
				3, null, 3, 21
			}
		});
		getDaoObject().modifySession(modifiedSession, testStopgapRounds);

		/**
		 * Assert the cached data of session
		 */
		Cursor newSessionData = getDaoObject().getSQLiteDatabase().rawQuery(
			" SELECT ss_number_of_ended_rounds, ss_number_of_rounds FROM tt_session" +
			" WHERE ss_id = 1", null
		);

		newSessionData.moveToFirst();

		assertEquals(1, newSessionData.getInt(0));
		assertEquals(3, newSessionData.getInt(1));
		// :~)

		/**
		 * Assert the data of rounds
		 */
		assertRoundsOfSession(modifiedSession.getId(), testStopgapRounds);
		// :~)
	}

	/**
	 * Tests the saving of record for ended round and its containing
	 * session.<p>
	 */
	public void testSaveEndedRound()
	{
		/**
		 * Save ended round
		 */
		getDaoObject().saveEndedRound(1);
		// :~)

		/**
		 * Assert simple case of saving an ended round
		 */
		Cursor resultTime = loadDataOfRound1();

		assertFalse(resultTime.isNull(0));
		assertFalse(resultTime.isNull(1));
		assertEquals(resultTime.getLong(0), resultTime.getLong(1));
		// :~)

		/**
		 * Prepare the end of session
		 */
		getDaoObject().saveEndedRound(2);
		getDaoObject().saveEndedRound(3);
		getDaoObject().saveEndedRound(4);
		getDaoObject().saveEndedRound(5);

		assertEquals(5, getNumberOfEndedRoundsOfSession1());
		// :~)

		/**
		 * Assert case of session which return back to first round
		 */
		getDaoObject().saveEndedRound(1);

		resultTime = loadDataOfRound1();
		assertFalse(resultTime.isNull(0));
		assertFalse(resultTime.isNull(1));
		assertEquals(resultTime.getLong(0), resultTime.getLong(1));
		// :~)
	}
	private Cursor loadDataOfRound1()
	{
		Cursor resultTime = getDaoObject().getSQLiteDatabase().rawQuery(
			" SELECT lt_time_last_reached, ss_time_last_used" +
			" FROM tt_session_round AS sr" +
			" 	INNER JOIN" +
			" 	tt_session AS ss" +
			" 	ON sr.sr_ss_id = ss.ss_id" +
			" 	LEFT OUTER JOIN" +
			" 	tt_last_tick AS lt" +
			" 	ON sr.sr_id = lt.lt_sr_id" +
			" WHERE sr_id = 1", null
		);
		resultTime.moveToFirst();

		return resultTime;
	}

	/**
	 * Tests the skipping of round in session.<p>
	 */
	public void testSkipCurrentRound()
	{
		/**
		 * Assert the first skipping
		 */
		getDaoObject().skipCurrentRound(1);
		assertEquals(1, getNumberOfEndedRoundsOfSession1());
		// :~)

		/**
		 * Assert two successive skippings
		 */
		getDaoObject().skipCurrentRound(1);
		getDaoObject().skipCurrentRound(1);
		assertEquals(3, getNumberOfEndedRoundsOfSession1());
		// :~)

		/**
		 * Assert backing to first round
		 */
		getDaoObject().skipCurrentRound(1);
		getDaoObject().skipCurrentRound(1);
		getDaoObject().skipCurrentRound(1);
		assertEquals(1, getNumberOfEndedRoundsOfSession1());
		// :~)
	}
	private int getNumberOfEndedRoundsOfSession1()
	{
		Cursor sessionData = getDaoObject().getSQLiteDatabase().rawQuery(
			" SELECT ss_number_of_ended_rounds" +
			" FROM tt_session" +
			" WHERE ss_id = 1", null
		);
		sessionData.moveToFirst();

		return sessionData.getInt(0);
	}

	/**
	 * Tests the resetting of last ended round in session.<p>
	 */
	public void testResetLastRound()
	{
		/**
		 * Prepare some of ended rounds
		 */
		getDaoObject().skipCurrentRound(1);
		getDaoObject().skipCurrentRound(1);
		getDaoObject().skipCurrentRound(1);

		assertEquals(3, getNumberOfEndedRoundsOfSession1());
		// :~)

		/**
		 * Assert reset an ended round
		 */
		getDaoObject().resetLastRound(1);
		assertEquals(2, getNumberOfEndedRoundsOfSession1());
		// :~)

		/**
		 * Assert resetting to first round
		 */
		getDaoObject().resetLastRound(1);
		getDaoObject().resetLastRound(1);
		assertEquals(0, getNumberOfEndedRoundsOfSession1());
		getDaoObject().resetLastRound(1);
		assertEquals(0, getNumberOfEndedRoundsOfSession1());
		// :~)
	}

	/**
	 * Tests removal of session.<p>
	 */
	public void testRemoveSession()
	{
		getDaoObject().removeSession(1); // Perform removal of session

		Cursor resultData = getDaoObject().getSQLiteDatabase().rawQuery(
			" SELECT ss_id" +
			" FROM tt_session" +
			" WHERE ss_id = 1", null
		);

		assertEquals(0, resultData.getCount());
	}

	/**
	 * Tests listing of sessions.<p>
	 */
	public void testListSessions()
	{
		/**
		 * Prepare the session which has all of its rounds ended
		 *
		 * First session: has finished all of its rounds
		 * Second session: has finished its first rounds
		 */
		getDaoObject().saveEndedRound(1);
		getDaoObject().saveEndedRound(2);
		getDaoObject().saveEndedRound(3);
		getDaoObject().saveEndedRound(4);
		getDaoObject().saveEndedRound(5);

		getDaoObject().saveEndedRound(6);
		// :~)

		Cursor resultData = getDaoObject().listSessions();
		resultData.moveToFirst();

		// Assert second session
		assertDataOfNextRound(resultData, 2, 25);

		resultData.moveToNext();

		// Assert first session
		assertDataOfNextRound(resultData, 1, 35);
	}
	private void assertDataOfNextRound(Cursor rowData, int expectedSequence, int expectedSecondsOfNextRound)
	{
		int nextSequenceOfRound = rowData.getInt(rowData.getColumnIndex("next_sequence_of_round"));
		int secondsOfNextRound = rowData.getInt(rowData.getColumnIndex("seconds_of_next_round"));

		Log.d("TAG", String.format("Next sequence:[%d], Seconds:[%d]", nextSequenceOfRound, secondsOfNextRound));

		assertEquals(expectedSequence, nextSequenceOfRound);
		assertEquals(expectedSecondsOfNextRound, secondsOfNextRound);
	}

	/**
	 * Tests listing of rounds in session.<p>
	 */
	public void testListRounds()
	{
		Cursor resultData = getDaoObject().listRounds(1);

		assertEquals(5, resultData.getCount());
	}

	/**
	 * Assert the rounds of session.<p>
	 */
	private void assertRoundsOfSession(int sessionId, Cursor expectedRounds)
	{
		Log.d(TAG, String.format("Assert rounds for session[%d]. Expected [%d] rounds.", sessionId, expectedRounds.getCount()));

		Cursor testRounds = getDaoObject().getSQLiteDatabase().rawQuery(
			" SELECT sr_sequence, sr_seconds, lt_time_last_reached" +
			" FROM tt_session_round AS sr" +
			" 	LEFT OUTER JOIN" +
			" 	tt_last_tick AS lt" +
			" 	ON sr.sr_id = lt.lt_sr_id" +
			" WHERE sr_ss_id = ?" +
			" ORDER BY sr_sequence ASC",
			new String[] { String.valueOf(sessionId) }
		);

		assertEquals(3, testRounds.getCount());
		testRounds.moveToFirst();

		int sequence = 1;
		expectedRounds.moveToFirst();
		while (!expectedRounds.isAfterLast()) {
			/**
			 * Assert the essential data of rounds
			 */
			assertEquals(sequence, testRounds.getInt(testRounds.getColumnIndex("sr_sequence")));
			assertEquals(
				expectedRounds.getInt(expectedRounds.getColumnIndex("rd_seconds")),
				testRounds.getInt(testRounds.getColumnIndex("sr_seconds"))
			);
			// :~)

			/**
			 * Assert the time of last using of rounds
			 */
			if (!expectedRounds.isNull(expectedRounds.getColumnIndex("rd_time_last_reached"))) {
				assertEquals(
					expectedRounds.getInt(expectedRounds.getColumnIndex("rd_time_last_reached")),
					testRounds.getInt(testRounds.getColumnIndex("lt_time_last_reached"))
				);
			}
			// :~)

			sequence++;
			testRounds.moveToNext();
			expectedRounds.moveToNext();
		}
	}
	private Cursor buildStopgapRounds(Object[][] data)
	{
		MatrixCursor testRounds = new MatrixCursor(
			new String[] {
				"rd_id", "rd_time_last_reached", "rd_sequence", "rd_seconds"
			},
			data.length
		);

		for (Object[] rowData: data) {
			testRounds.addRow(rowData);
		}

		return testRounds;
	}

	@Override
	protected TeaTimeDao buildDaoObject()
	{
		return new TeaTimeDao(getContext());
	}
}
