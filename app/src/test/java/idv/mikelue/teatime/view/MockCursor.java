package idv.mikelue.teatime.view;

import android.database.Cursor;

import mockit.Mock;
import mockit.MockUp;

public class MockCursor extends MockUp<Cursor> {
	private final int[] rows;
	private int currentPosition = -1;

	MockCursor(int[] newRows)
	{
		rows = newRows;
	}

	@Mock
	public boolean moveToNext()
	{
		++currentPosition;
		if (currentPosition >= rows.length) {
			throw new RuntimeException("Cursor overflow");
		}

		return true;
	}

	@Mock
	public int getPosition()
	{
		return currentPosition;
	}

	@Mock
	public boolean moveToPosition(int newPosition)
	{
		if (newPosition >= rows.length) {
			throw new RuntimeException("Cursor overflow");
		}

		currentPosition = newPosition;
		return true;
	}

	@Mock
	public int getColumnIndex(String columnName)
	{
		return -1;
	}

	@Mock
	public int getInt(int columnIndex)
	{
		return rows[currentPosition];
	}

	@Mock
	public int getCount()
	{
		return rows.length;
	}
}
