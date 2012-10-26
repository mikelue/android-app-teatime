package idv.mikelue.teatime.view;

import android.content.Context;
import android.test.InstrumentationTestCase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import idv.mikelue.teatime.R;
import idv.mikelue.teatime.model.DetailTickingRound;
import idv.mikelue.teatime.model.RoundInfo;
import idv.mikelue.teatime.model.Session.IconType;
import idv.mikelue.teatime.view.ColorLoader;

public class AlarmSectionTest extends InstrumentationTestCase {
	public AlarmSectionTest() {}

	/**
	 * Tests the initialization of AlarmSection.<p>
	 */
	public void testInitAlarmSection()
	{
		AlarmSection testSection = buildTestSection();

		assertEquals("Expected GONE", View.GONE, testSection.getVisibility());
		assertEquals(0, testSection.getChildCount());
	}

	/**
	 * Tests adding of alarm round.<p>
	 */
	public void testAddAlarmRound()
	{
		AlarmSection testSection = buildTestSection();
		testSection.addAlarmRound(buildAlarmingRound(1));

		assertEquals("Expected VISIBLE", View.VISIBLE, testSection.getVisibility());
		assertEquals(1, testSection.getChildCount());
	}

	/**
	 * Tests the removal of view of alarming while "OK" has been clicked.<p>
	 */
	public void testClickOk()
	{
		/**
		 * Prepare data
		 */
		AlarmSection testSection = buildTestSection();
		testSection.addAlarmRound(buildAlarmingRound(1));
		testSection.addAlarmRound(buildAlarmingRound(2));
		assertEquals(2, testSection.getChildCount());
		// :~)

		for (int i = 0; i < 2; i++) {
			ViewGroup testItem = (ViewGroup)testSection.getChildAt(0);
			testItem.findViewById(R.id.button_ok).performClick();
		}

		assertEquals("Expected GONE", View.GONE, testSection.getVisibility());
		assertEquals(0, testSection.getChildCount());
	}

	/**
	 * Tests the callback of alarming checked.<p>
	 */
	public void testAlarmCallback()
	{
		/**
		 * Prepare data
		 */
		RoundInfo testRound = buildAlarmingRound(3);

		AlarmSection testSection = buildTestSection();
		testSection.addAlarmRound(testRound);
		assertEquals(1, testSection.getChildCount());
		// :~)

		FlagCheckAlarmCallback callbackCheck = new FlagCheckAlarmCallback();
		testSection.setCheckAlarmCallback(callbackCheck);

		/**
		 * As performing the click from user
		 */
		ViewGroup testItem = (ViewGroup)testSection.getChildAt(0);
		testItem.findViewById(R.id.button_ok).performClick();
		// :~)

		assertEquals(testRound, callbackCheck.roundOfCallback);
	}

	private RoundInfo buildAlarmingRound(int roundId)
	{
		DetailTickingRound testRound = new DetailTickingRound(roundId, 0);
		testRound.setName("TestSession");
		testRound.setRoundSequence(roundId);
		testRound.setNumberOfRounds(roundId + 2);
		testRound.setIconType(IconType.Type1);

		return testRound;
	}
	private AlarmSection buildTestSection()
	{
		Context context = getInstrumentation().getTargetContext();

		ColorLoader.initLoader(context, context.hashCode());

		LayoutInflater layoutInflater = LayoutInflater.from(context);
		return (AlarmSection)layoutInflater.inflate(R.layout.alarm_section, null);
	}
}

class FlagCheckAlarmCallback implements AlarmSection.CheckAlarmCallback {
	RoundInfo roundOfCallback = null;

	FlagCheckAlarmCallback() {}

	public void checkAlarm(RoundInfo alarmingRound)
	{
		Log.d("Test", "Got callback of checking of alarming: " + alarmingRound.getId());

		roundOfCallback = alarmingRound;
	}
}
