package idv.mikelue.teatime.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import android.util.Log;

import mockit.Deencapsulation;

public class TeaTimeSQLiteOpenHelperTest extends AndroidTestCase {
	private final static String TAG = TeaTimeSQLiteOpenHelperTest.class.getSimpleName();

	public TeaTimeSQLiteOpenHelperTest() {}

	/**
	 * Setup renaming for file prefix of context to "test-init.".<p>
	 */
	@Override
	public void setUp() throws Exception
	{
		super.setUp();

		setContext(new RenamingDelegatingContext(getContext(), "test-init."));
	}
	@Override
	public void tearDown() throws Exception
	{
		if (sqliteOpenHelper != null) {
			sqliteOpenHelper.close();
		}

		super.tearDown();
	}

	/**
	 * Tests the creation of database.<p>
	 */
	public void testOnCreate()
	{
		SQLiteDatabase db = getSqliteOpenHelper().getWritableDatabase();

		// Make sure that the leaf table(is not referenced by any of other
		// table) is created
		db.query("tt_last_tick", null, null, null, null, null, null);
	}

	/**
	 * Tests the value of number of rounds which is auto-updated by trigger.<p>
	 */
	public void testTriggerForNumberOfRounds()
	{
		// Since there are default data while preparing schema, we can assert
		// the number of rounds without building data of testing
		assertNumberOfRounds();

		/**
		 * Delete two rounds from both of sessions built by default
		 */
		getSqliteOpenHelper().getWritableDatabase().execSQL(
			" DELETE FROM tt_session_round" +
			" WHERE sr_id IN (4, 5)"
		);
		getSqliteOpenHelper().getWritableDatabase().execSQL(
			" DELETE FROM tt_session_round" +
			" WHERE sr_ss_id = 2"
		);
		// :~)

		assertNumberOfRounds();
	}
	private void assertNumberOfRounds()
	{
		final String sqlCacheColumnAndCalculatedExpection =
			" SELECT ss.ss_id, ss.ss_number_of_rounds," +
			" 	COUNT(sr.sr_id) AS expected_number_of_rounds" +
			" FROM tt_session AS ss" +
			" 	INNER JOIN" +
			" 	tt_session_round AS sr" +
			" 	ON ss.ss_id = sr.sr_ss_id" +
			" GROUP BY ss.ss_id, ss.ss_number_of_rounds";

		Cursor resultData = getSqliteOpenHelper().getReadableDatabase().rawQuery(
			sqlCacheColumnAndCalculatedExpection, null
		);
		resultData.moveToFirst();

		/**
		 * Assert the number of rounds
		 */
		while (!resultData.isAfterLast()) {
			Log.d(TAG,
				String.format(
					"Assert the number of rounds for session: [%d], Cached Column:[%d]. Calculated Value: [%d]",
					resultData.getInt(0), resultData.getInt(1), resultData.getInt(2)
			));
			assertEquals(resultData.getInt(2), resultData.getInt(1));

			resultData.moveToNext();
		}
		// :~)
	}

	/**
	 * Tests the value of number of eneded rounds which is auto-updated by
	 * trigger.<p>
	 */
	public void testTriggerForNumberOfEndedRounds()
	{
		/**
		 * Insert two records of ended rounds from both of sessions built by default
		 */
		getSqliteOpenHelper().getWritableDatabase().execSQL(
			" INSERT INTO tt_last_tick(lt_sr_id, lt_time_last_reached)" +
			" VALUES(1, 234242432)"
		);
		getSqliteOpenHelper().getWritableDatabase().execSQL(
			" INSERT INTO tt_last_tick(lt_sr_id, lt_time_last_reached)" +
			" VALUES(2, 234242432)"
		);
		getSqliteOpenHelper().getWritableDatabase().execSQL(
			" INSERT INTO tt_last_tick(lt_sr_id, lt_time_last_reached)" +
			" VALUES(6, 234242432)"
		);
		getSqliteOpenHelper().getWritableDatabase().execSQL(
			" INSERT INTO tt_last_tick(lt_sr_id, lt_time_last_reached)" +
			" VALUES(7, 234242432)"
		);
		// :~)

		assertNumberOfEndedRounds();

		/**
		 * Delete a record of ended rounds from both of sessions built by default
		 */
		getSqliteOpenHelper().getWritableDatabase().execSQL(
			" DELETE FROM tt_last_tick" +
			" WHERE lt_sr_id = 1"
		);
		getSqliteOpenHelper().getWritableDatabase().execSQL(
			" DELETE FROM tt_session_round" +
			" WHERE sr_ss_id = 2"
		);
		// :~)

		assertNumberOfEndedRounds();
	}

	private void assertNumberOfEndedRounds()
	{
		final String sqlCacheColumnAndCalculatedExpection =
			" SELECT ss.ss_id, ss.ss_number_of_ended_rounds," +
			" 	COUNT(lt.lt_sr_id) AS expected_number_of_ended_rounds" +
			" FROM tt_session AS ss" +
			" 	INNER JOIN" +
			" 	tt_session_round AS sr" +
			" 	ON ss.ss_id = sr.sr_ss_id" +
			" 	LEFT OUTER JOIN" +
			" 	tt_last_tick AS lt" +
			" 	ON sr.sr_id = lt.lt_sr_id" +
			" GROUP BY ss.ss_id, ss.ss_number_of_ended_rounds";

		Cursor resultData = getSqliteOpenHelper().getReadableDatabase().rawQuery(
			sqlCacheColumnAndCalculatedExpection, null
		);
		resultData.moveToFirst();

		/**
		 * Assert the number ofe ended rounds
		 */
		while (!resultData.isAfterLast()) {
			Log.d(TAG,
				String.format(
					"Assert the number of ended rounds for session: [%d], Cached Column:[%d]. Calculated Value: [%d]",
					resultData.getInt(0), resultData.getInt(1), resultData.getInt(2)
			));
			assertEquals(resultData.getInt(2), resultData.getInt(1));

			resultData.moveToNext();
		}
		// :~)
	}

	private SQLiteOpenHelper sqliteOpenHelper = null;
	private SQLiteOpenHelper getSqliteOpenHelper()
	{
		if (sqliteOpenHelper == null) {
			sqliteOpenHelper = new TeaTimeSQLiteOpenHelper(getContext());

			/**
			 * Remove database when this method is called first time
			 */
			String dbName = Deencapsulation.getField(
				sqliteOpenHelper, "DB_NAME"
			);
			((RenamingDelegatingContext)getContext()).deleteDatabase(dbName);
			// :~)
		}

		return sqliteOpenHelper;
	}
}
