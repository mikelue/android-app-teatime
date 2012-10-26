package idv.mikelue.teatime.database;

import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * This base provides skeleton for construct a databae helper for access by
 * sub-class.<p>
 */
public abstract class AbstractDaoBase {
	private Context context;
	private Resources resources;

	/**
	 * Gets the writable {@link SQLiteDatabase} built by {@link #buildSqliteDatabaseHelper}.<p>
	 *
	 * @return The database to be operated
	 */
	private SQLiteOpenHelper sqlliteDatabaseHelper = null;
	public SQLiteDatabase getSQLiteDatabase()
	{
		if (sqlliteDatabaseHelper == null) {
			sqlliteDatabaseHelper = buildSqliteDatabaseHelper();
		}

		return sqlliteDatabaseHelper.getWritableDatabase();
	}

	/**
	 * Closes the database object and releases other resources.<p>
	 */
	public void release()
	{
		if (sqlliteDatabaseHelper == null) {
			return;
		}

		sqlliteDatabaseHelper.close();
	}

	/**
	 * Constructs this object with context
	 */
	protected AbstractDaoBase(Context newContext)
	{
		context = newContext;
		resources = newContext.getResources();
	}

	/**
	 * Gets the {@link Context} object.<p>
	 *
	 * @return The context of activity
	 */
	protected Context getContext()
	{
		return context;
	}

	/**
	 * Gets the {@link Resources} object for SQL.<p>
	 *
	 * @return The resources in current context
	 */
	protected Resources getResources()
	{
		return resources;
	}

	/**
	 * Sub-class should implements the building of the database helper.<p>
	 *
	 * @return The helper of Sqlite database
	 */
	abstract protected SQLiteOpenHelper buildSqliteDatabaseHelper();
}
