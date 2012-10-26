package idv.mikelue.teatime.view;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import idv.mikelue.teatime.R;
import idv.mikelue.teatime.model.DetailTickingRound;
import idv.mikelue.teatime.model.Session.IconType;
import idv.mikelue.teatime.test.AbstractViewTestBase;

public class TickingSectionTest extends AbstractViewTestBase {
	private final static int FIX_COUNT_OF_CHILDREN = 1; // The header of section

	public TickingSectionTest() {}

	/**
	 * Tests the initialization of TickingSection.<p>
	 */
	public void testInitTickingSection()
	{
		TickingSection testSection = buildTestSection();

		assertEquals("Expected GONE", View.GONE, testSection.getVisibility());
		assertEquals(FIX_COUNT_OF_CHILDREN + 0, testSection.getChildCount());
	}

	/**
	 * Tests adding of ticking round.<p>
	 */
	public void testAddTickingRound()
	{
		TickingSection testSection = buildTestSection();

		/**
		 * Execute the code of changing view in UI thread
		 */
		showTickingRoundInUiThread(testSection, buildTickingRound(1, 20));
		// :~)

		getInstrumentation().waitForIdleSync();

		assertEquals("Expected VISIBLE", View.VISIBLE, testSection.getVisibility());
		assertEquals(FIX_COUNT_OF_CHILDREN + 1, testSection.getChildCount());
	}

	/**
	 * Tests removal of ticking round which is ended.<p>
	 */
	public void testEndedTickingRound() throws InterruptedException
	{
		TickingSection testSection = buildTestSection();

		/**
		 * Execute the code of changing view in UI thread
		 */
		showTickingRoundInUiThread(testSection, buildTickingRound(1, 20));
		showTickingRoundInUiThread(testSection, buildTickingRound(2, 20));
		// :~)

		/**
		 * End of first ticking round
		 */
		showTickingRoundInUiThread(testSection, buildTickingRound(1, 0));
		getInstrumentation().waitForIdleSync();
		assertEquals("Expected VISIBLE", View.VISIBLE, testSection.getVisibility());
		assertEquals(FIX_COUNT_OF_CHILDREN + 1, testSection.getChildCount());
		// :~)

		/**
		 * End of second ticking round
		 */
		showTickingRoundInUiThread(testSection, buildTickingRound(2, 0));
		getInstrumentation().waitForIdleSync();
		assertEquals("Expected GONE", View.GONE, testSection.getVisibility());
		assertEquals(FIX_COUNT_OF_CHILDREN + 0, testSection.getChildCount());
		// :~)
	}

	/**
	 * Tests the sequence of views of ticking items.<p>
	 */
	public void testSequenceOfItems()
	{
		TickingSection testSection = buildTestSection();
		testSection.showTickingRound(buildTickingRound(10, 20));
		testSection.showTickingRound(buildTickingRound(28, 18));
		testSection.showTickingRound(buildTickingRound(21, 15));

		assertEquals(21, getTagOfView(testSection, 0 + FIX_COUNT_OF_CHILDREN));
		assertEquals(28, getTagOfView(testSection, 1 + FIX_COUNT_OF_CHILDREN));
		assertEquals(10, getTagOfView(testSection, 2 + FIX_COUNT_OF_CHILDREN));
	}

	/**
	 * Tests the callback while user click the cancel button.<p>
	 */
	public void testCancelCallback()
	{
		final int testRoundId = 76;

		RecordCancelTickingListener testListener = new RecordCancelTickingListener();

		/**
		 * Perform the clicking
		 */
		TickingSection testSection = buildTestSection();
		testSection.setCancelTickingListener(testListener);
		testSection.showTickingRound(buildTickingRound(testRoundId, 60));

		testSection.findViewById(R.id.button_cancel).performClick();
		// :~)

		assertEquals(testRoundId, testListener.sentRoundId);
	}

	private void showTickingRoundInUiThread(final TickingSection testSection, final DetailTickingRound tickingRound)
	{
		try {
			runTestOnUiThread(new Runnable() {
				@Override
				public void run()
				{
					testSection.showTickingRound(tickingRound);
				}
			});
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	private int getTagOfView(ViewGroup section, int indexOfChild)
	{
		ViewGroup itemView = (ViewGroup)section.getChildAt(indexOfChild);

		return (Integer)itemView.findViewById(R.id.button_cancel).getTag();
	}
	private DetailTickingRound buildTickingRound(int roundId, int seconds)
	{
		DetailTickingRound testRound = new DetailTickingRound(roundId, seconds);
		testRound.setName("TestSession");
		testRound.setRoundSequence(roundId);
		testRound.setNumberOfRounds(roundId + 2);
		testRound.setIconType(IconType.Type1);

		return testRound;
	}
	private TickingSection buildTestSection()
	{
		LayoutInflater layoutInflater = LayoutInflater.from(getInstrumentation().getTargetContext());
		return (TickingSection)layoutInflater.inflate(R.layout.ticking_section, null);
	}
}

/**
 * Test listener to assert the calling while users click cancel button
 */
class RecordCancelTickingListener implements TickingSection.CancelTickingListener {
	int sentRoundId = -1;

	RecordCancelTickingListener() {}

	@Override
	public void cancelTicking(int roundId)
	{
		sentRoundId = roundId;
	}
}
