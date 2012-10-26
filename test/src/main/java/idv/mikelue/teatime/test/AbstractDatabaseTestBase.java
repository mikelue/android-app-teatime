package idv.mikelue.teatime.test;

import android.database.sqlite.SQLiteOpenHelper;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import mockit.Deencapsulation;

import idv.mikelue.teatime.database.AbstractDaoBase;

public abstract class AbstractDatabaseTestBase<T extends AbstractDaoBase> extends AndroidTestCase {
	/**
	 * The name of TAG from {@link Class#getSimpleName}.<p>
	 */
	protected final String TAG = getClass().getSimpleName();

	protected AbstractDatabaseTestBase() {}

	/**
	 * Setup renaming for file prefix of context to "test.".<p>
	 */
	@Override
	public void setUp() throws Exception
	{
		super.setUp();
		setContext(new RenamingDelegatingContext(getContext(), "test."));
	}

	@Override
	public void tearDown() throws Exception
	{
		removeDatabase();
		super.tearDown();
	}

	/**
	 * Gets the DAO object which is built by {@link #buildDaoObject}.<p>
	 *
	 * @return The DAO object(singleton)
	 */
	private T daoObject = null;
	protected T getDaoObject()
	{
		if (daoObject == null) {
			daoObject = buildDaoObject();
		}

		return daoObject;
	}

	/**
	 * Sub-class should implements this method to build dao object.<p>
	 *
	 * @return The DAO object to be tested
	 */
	abstract protected T buildDaoObject();

	/**
	 * Removes the database of testing.<p>
	 */
	protected void removeDatabase()
	{
		if (daoObject == null) {
			return;
		}

		daoObject.release();

		String dbName = Deencapsulation.getField(
			Deencapsulation.getField(daoObject, SQLiteOpenHelper.class),
			"DB_NAME"
		);
		((RenamingDelegatingContext)getContext()).deleteDatabase(dbName);
	}
}
