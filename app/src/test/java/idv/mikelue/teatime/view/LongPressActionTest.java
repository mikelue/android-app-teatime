package idv.mikelue.teatime.view;

import android.content.Context;
import android.os.Handler;
import android.view.MotionEvent;

import mockit.Deencapsulation;
import mockit.Delegate;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LongPressActionTest {
	@Mocked
	private Handler mockHandler;
	@Mocked
	private MotionEvent mockMotionEvent;

	public LongPressActionTest() {}

	/**
	 * Tests the starting of action by long click.<p>
	 */
	@Test
	public void onLongClick()
	{
		new NonStrictExpectations() {{
			mockHandler.postDelayed(withInstanceOf(Runnable.class), anyLong);
			result = new Delegate() {
				boolean hasRun = false;

				boolean postDelayed(Runnable r, long delayMillis)
				{
					if (!hasRun) {
						hasRun = true;
						r.run();
					}

					return hasRun;
				}
			};
		}};

		SensiblePressingActionListener testListener = new SensiblePressingActionListener();
		LongPressAction testPressAction = new LongPressAction(mockHandler, testListener);
		testPressAction.getLongClickListener().onLongClick(null);

		Assert.assertEquals(testListener.called, 1);

		new Verifications() {{
			mockHandler.postDelayed((Runnable)any, anyLong);
			times = 2;
		}};
	}

	/**
	 * Tests the event of onTouch.<p>
	 */
	@Test
	public void onTouch()
	{
		SensiblePressingActionListener testListener = new SensiblePressingActionListener();
		LongPressAction testPressAction = new LongPressAction(mockHandler, testListener);

		/**
		 * Normal press the button
		 */
		setupMockMotionAction(MotionEvent.ACTION_DOWN);
		Assert.assertFalse(
			testPressAction.getTouchListener().onTouch(null, mockMotionEvent)
		);
		Assert.assertEquals(testListener.called, 1);
		// :~)

		/**
		 * Normal release the button
		 */
		setupMockMotionAction(MotionEvent.ACTION_UP);
		Assert.assertFalse(
			testPressAction.getTouchListener().onTouch(null, mockMotionEvent)
		);
		Assert.assertEquals(testListener.called, 1);
		// :~)

		// Set the status into "pressing"
		Deencapsulation.setField(testPressAction, "pressing", true);

		/**
		 * Release the button while in pressing
		 */
		setupMockMotionAction(MotionEvent.ACTION_UP);
		Assert.assertTrue(
			testPressAction.getTouchListener().onTouch(null, mockMotionEvent)
		);
		Assert.assertEquals(
			Deencapsulation.getField(testPressAction, "pressing"),
			false
		);
		// :~)
	}
	private void setupMockMotionAction(final int action)
	{
		new NonStrictExpectations() {{
			mockMotionEvent.getAction();
			result = action;
		}};
	}

	private static int getInitDelay()
	{
		return Deencapsulation.<Integer>getField(LongPressAction.class, "INIT_DEALY");
	}
	private static int getMinDelay()
	{
		return Deencapsulation.<Integer>getField(LongPressAction.class, "MIN_DEALY");
	}
}

class SensiblePressingActionListener implements LongPressAction.PressingActionListener {
	int called = 0;

	SensiblePressingActionListener() {}

	@Override
	public void stillPress()
	{
		++called;
	}
}
