package idv.mikelue.teatime.database;

import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import idv.mikelue.teatime.R;

/**
 * This helper gives in-memory database for stopgap modification of rounds.<p>
 */
public class InMemorySOHOfRoundEditor extends SQLiteOpenHelper {
	private final static String TAG = InMemorySOHOfRoundEditor.class.getSimpleName();

	private final String DDL_SCHEMA;
	private Resources resources;

	public InMemorySOHOfRoundEditor(Context context)
	{
		super(context, null, null, 1);
		DDL_SCHEMA = context.getResources().getString(R.string.sql_schema_inmemory_rounds);
	}

	/**
	 * Builds schema for stopgap rounds.<p>
	 */
	@Override
	public void onCreate(SQLiteDatabase db)
	{
		/**
		 * Build schema
		 */
		Log.d(TAG, "Preparing database schema for stopgap rounds...");
		db.execSQL(DDL_SCHEMA);
		Log.d(TAG, "Preparing database schema for stopgap rounds is finished");
		// :~)
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}
