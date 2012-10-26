package idv.mikelue.teatime.view;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import mockit.Cascading;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import idv.mikelue.teatime.test.AbstractMockAndroidEnvTestBase;
import static idv.mikelue.teatime.model.RoundInfo.MAX_SECONDS;
import static idv.mikelue.teatime.model.RoundInfo.MIN_SECONDS;

public class ChangeTimeDialogTest extends AbstractMockAndroidEnvTestBase {
	@Mocked(methods={"<init>", "refreshView"})
	private ChangeTimeDialog mockDialog;

	@Mocked
	private EditText mockEditorOfMins;
	@Mocked
	private EditText mockEditorOfSecs;

	public ChangeTimeDialogTest() {}

	/**
	 * Tests the correction of seconds that exceeds or lower than limits.<p>
	 */
	@Test(dataProvider="CorrectSeconds")
	public void correctSeconds(
		int testSeconds, int expectedValue
	) {
		ChangeTimeDialog testDialog = buildTestView();

		Deencapsulation.setField(testDialog, "seconds", testSeconds);
		Deencapsulation.invoke(testDialog, "correctSeconds");

		Assert.assertEquals(
			Deencapsulation.<Integer>getField(testDialog, "seconds"),
			new Integer(expectedValue)
		);
	}

	@DataProvider(name="CorrectSeconds")
	private Object[][] getCorrectSeconds()
	{
		return new Object[][] {
			/**
			 * The value of seconds is valid
			 */
			{ MIN_SECONDS + 1, MIN_SECONDS + 1 },
			{ MAX_SECONDS - 1, MAX_SECONDS - 1 },
			// :~)
			/**
			 * The value of seconds is exceeding or lowering limit
			 */
			{ MIN_SECONDS - 1, MIN_SECONDS },
			{ MAX_SECONDS + 1, MAX_SECONDS }
			// :~)
		};
	}

	private ChangeTimeDialog testDialogForValueUpdator = null;
	/**
	 * Tests the watcher for changing value of EditText.<p>
	 */
	@Test(dataProvider="ValueUpdator")
	public void valueUpdator(
		final String testMinText, final String testSecText,
		int expectedSeconds, final boolean needRefreshView
	) {
		if (testDialogForValueUpdator == null) {
			testDialogForValueUpdator = buildTestView();
		}

		/**
		 * Setup the value in EditText
		 */
		new NonStrictExpectations()
		{
			Editable mockTextOfMins;
			Editable mockTextOfSecs;

			{
				mockEditorOfMins.getText();
				result = mockTextOfMins;
				mockTextOfMins.toString();
				result = testMinText;

				mockEditorOfSecs.getText();
				result = mockTextOfSecs;
				mockTextOfSecs.toString();
				result = testSecText;
			}
		};
		// :~)

		TextWatcher testWatcher = Deencapsulation.newInnerInstance(
			"ValueUpdateWatcher", testDialogForValueUpdator
		);
		testWatcher.afterTextChanged(null);

		Assert.assertEquals(
			testDialogForValueUpdator.getTime(), expectedSeconds
		);

		new Verifications() {{
			/**
			 * Assert the calling to "refreshView" method
			 */
			invoke(testDialogForValueUpdator, "refreshView");
			times = needRefreshView ? 1 : 0;
			// :~)
		}};
	}
	@DataProvider(name="ValueUpdator")
	private Object[][] getValueUpdator()
	{
		return new Object[][] {
			{ "00", "50", 50, true },
			{ "06", "39", 399, true },
			{ "05", "99", 399, false },
			{ "10", "00", 600, true },
			{ "00", "00", 0, true },
			{ "00", "00", 0, false }
		};
	}

	private ChangeTimeDialog buildTestView()
	{
		ChangeTimeDialog testView = new ChangeTimeDialog(null, null);
		Deencapsulation.setField(testView, "editorOfMins", mockEditorOfMins);
		Deencapsulation.setField(testView, "editorOfSecs", mockEditorOfSecs);

		return testView;
	}
}
