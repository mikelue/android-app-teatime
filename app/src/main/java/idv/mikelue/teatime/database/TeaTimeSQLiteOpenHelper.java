package idv.mikelue.teatime.database;

import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import idv.mikelue.teatime.R;

/**
 * Initiation of database containing data of sessions, rounds, etc.<p>
 */
public class TeaTimeSQLiteOpenHelper extends SQLiteOpenHelper {
	private final static String TAG = TeaTimeSQLiteOpenHelper.class.getSimpleName();

	private final static String DB_NAME = "tea-time";
	private final static int CURRENT_VERSION = 1;

	private Resources resources;

	public TeaTimeSQLiteOpenHelper(Context context)
	{
		super(context, DB_NAME, null, CURRENT_VERSION);
		resources = context.getResources();
	}

	/**
	 * Builds schema and provides sample data of sessions.<p>
	 */
	@Override
	public void onCreate(SQLiteDatabase db)
	{
		/**
		 * Build schema
		 */
		Log.d(TAG, "Preparing database schema...");
		String[] ddl = resources.getStringArray(R.array.sql_schema_v1);

		for (String sql: ddl) {
			db.execSQL(sql);
		}
		Log.d(TAG, "Preparing database schema is finished");
		// :~)

		/**
		 * Build default data
		 */
		String[] dml = resources.getStringArray(R.array.sql_defaultData_v1);

		Log.d(TAG, "Preparing default data...");
		db.beginTransaction();
		try {
			for (String sql: dml) {
				db.execSQL(sql);
			}

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		Log.d(TAG, "Preparing default data is finished");
		// :~)
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}
