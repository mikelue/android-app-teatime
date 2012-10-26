package idv.mikelue.teatime.view;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.test.UiThreadTest;
import android.view.ViewGroup;

import idv.mikelue.teatime.R;
import idv.mikelue.teatime.view.TeaTimeListAdapter.SessionActionListener;
import idv.mikelue.teatime.test.AbstractViewTestBase;

public class TeaTimeListAdapterTest extends AbstractViewTestBase {
	public TeaTimeListAdapterTest() {}

	/**
	 * Tests the calling of listener on actions of session item.<p>
	 */
	@UiThreadTest
	public void testSessionActionListener() throws ClassNotFoundException
	{
		final int testRoundId = 13;

		/**
		 * Setup adapter
		 */
		SensibleActionListener testListener = new SensibleActionListener();

		Thread.currentThread().getContextClassLoader().loadClass(
			TeaTimeListAdapter.class.getName()
		);

		TeaTimeListAdapter testAdapter = new TeaTimeListAdapter(getInstrumentation().getTargetContext());
		testAdapter.changeCursor(buildSessionData(testRoundId));
		testAdapter.setSessionActionListener(testListener);
		// :~)

		/**
		 * Trigger the button of built view by adapter
		 */
		ViewGroup testView = (ViewGroup)testAdapter.getView(0, null, null);
		testView.findViewById(R.id.button_start_ticking).performClick();
		testView.findViewById(R.id.button_setting).performClick();
		// :~)

		assertEquals(testRoundId, testListener.roundIdOfTriggerTicking);
		assertEquals(testRoundId, testListener.roundIdOfTriggerSetting);
	}

	private Cursor buildSessionData(int testRoundId)
	{
		MatrixCursor sessionData = new MatrixCursor(
			new String[] {
				"_id",
				"ss_id", "ss_icon_type", "ss_name", "ss_number_of_rounds",
				"id_of_next_round", "seconds_of_next_round", "next_sequence_of_round"
			}
		);
		sessionData.addRow(new Object[] {
			testRoundId,
			1, 2, "test-session", 3,
			testRoundId, 30, 2
		});

		return sessionData;
	}
}

class SensibleActionListener implements SessionActionListener {
	int roundIdOfTriggerTicking = -1;
	int roundIdOfTriggerSetting = -1;

	SensibleActionListener() {}

	@Override
	public void startTicking(int roundId)
	{
		roundIdOfTriggerTicking = roundId;
	}
	@Override
	public void settingSession(int roundId)
	{
		roundIdOfTriggerSetting = roundId;
	}
}
