package idv.mikelue.teatime.view;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.test.InstrumentationTestCase;
import android.test.UiThreadTest;
import android.view.ViewGroup;
import android.widget.Adapter;

import idv.mikelue.teatime.R;
import idv.mikelue.teatime.view.RoundCursorAdapter.RoundActionListener;

public class RoundCursorAdapterTest extends InstrumentationTestCase {
	public RoundCursorAdapterTest() {}

	/**
	 * Tests the calling of listener on "add" button.<p>
	 */
	@UiThreadTest
	public void testAddByOfActionListener() throws Throwable
	{
		final int testRoundId = 13;
		SensibleActionListenerOfRound testListener = new SensibleActionListenerOfRound();
		RoundCursorAdapter testAdapter = buildAdapter(testRoundId, testListener);

		triggerButton(testAdapter, R.id.button_add_round);

		assertEquals(testRoundId, testListener.roundIdOfAddBy);
	}

	/**
	 * Tests the calling of listener on "remove" button.<p>
	 */
	@UiThreadTest
	public void testRemoveOfActionListener() throws Throwable
	{
		final int testRoundId = 13;
		SensibleActionListenerOfRound testListener = new SensibleActionListenerOfRound();
		RoundCursorAdapter testAdapter = buildAdapter(testRoundId, testListener);

		triggerButton(testAdapter, R.id.button_remove_round);

		assertEquals(testRoundId, testListener.roundIdOfRemove);
	}

	private void triggerButton(Adapter adapter, int buttonId)
	{
		/**
		 * Trigger the button of built view by adapter
		 */
		ViewGroup testView = (ViewGroup)adapter.getView(0, null, null);
		testView.findViewById(buttonId).performClick();
		// :~)
	}

	private RoundCursorAdapter buildAdapter(int testRoundId, RoundActionListener listener)
	{
		/**
		 * Setup adapter
		 */
		try {
			Thread.currentThread().getContextClassLoader().loadClass(
				RoundCursorAdapter.class.getName()
			);
		} catch (ClassNotFoundException e) { throw new RuntimeException(e); }

		RoundCursorAdapter testAdapter = new RoundCursorAdapter(
			getInstrumentation().getTargetContext(),
			buildSessionData(testRoundId)
		);
		testAdapter.setActionListener(listener);
		// :~)

		return testAdapter;
	}

	private Cursor buildSessionData(int testRoundId)
	{
		MatrixCursor sessionData = new MatrixCursor(
			new String[] {
				"_id",
				"rd_id", "rd_sequence", "rd_seconds", "rd_time_last_reached"
			}
		);
		sessionData.addRow(new Object[] {
			testRoundId,
			testRoundId, 1, 30, null
		});

		return sessionData;
	}
}

class SensibleActionListenerOfRound implements RoundActionListener {
	int roundIdOfAddBy = -1;
	int roundIdOfRemove = -1;

	SensibleActionListenerOfRound() {}

	@Override
	public void addBy(int roundIdOfSource)
	{
		roundIdOfAddBy = roundIdOfSource;
	}
	@Override
	public void remove(int roundId)
	{
		roundIdOfRemove = roundId;
	}
	@Override
	public void reset(int roundId)
	{
	}
	@Override
	public void skip(int roundId)
	{
	}
	@Override
	public void setTime(int roundId, int newSeconds, boolean affectConsecutiveRounds)
	{
	}
}
