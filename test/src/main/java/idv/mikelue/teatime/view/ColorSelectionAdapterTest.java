package idv.mikelue.teatime.view;

import android.database.Cursor;
import android.test.InstrumentationTestCase;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;

import idv.mikelue.teatime.R;
import idv.mikelue.teatime.model.Session.IconType;
import idv.mikelue.teatime.view.ColorLoader;
import idv.mikelue.teatime.view.ColorSelectionAdapter.IconActionListener;

public class ColorSelectionAdapterTest extends InstrumentationTestCase {
	public ColorSelectionAdapterTest() {}

	/**
	 * Tests the calling of listener on chosen "icon".<p>
	 */
	public void testIconActionListener()
	{
		final int indexOfTestIcon = 3;

		SensibleIconActionListener testListener = new SensibleIconActionListener();

		/**
		 * Perform the clicking of icon
		 */
		ColorSelectionAdapter testAdapter = new ColorSelectionAdapter(getInstrumentation().getTargetContext());
		testAdapter.setIconActionListener(testListener);

		ViewGroup testItem = (ViewGroup)testAdapter.getView(indexOfTestIcon, null, null);
		testItem.findViewById(R.id.session_icon).performClick();
		// :~)

		assertEquals(IconType.valueOfDatabaseValue(indexOfTestIcon + 1), testListener.chosenIconType);
	}
}

class SensibleIconActionListener implements IconActionListener {
	IconType chosenIconType = null;

	SensibleIconActionListener() {}

	@Override
	public void chooseIconColor(IconType chosenIconType)
	{
		this.chosenIconType = chosenIconType;
	}
}
