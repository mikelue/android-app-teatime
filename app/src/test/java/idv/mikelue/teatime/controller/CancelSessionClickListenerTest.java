package idv.mikelue.teatime.controller;

import android.app.Activity;
import android.widget.Button;
import android.view.View;

import mockit.Mocked;
import mockit.Verifications;
import mockit.NonStrictExpectations;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import idv.mikelue.teatime.view.DialogUtil;

public class CancelSessionClickListenerTest {
	@Mocked
	private Activity mockActivity;
	@Mocked
	private SessionEditorController mockController;
	@Mocked
	private Button mockButton;
	@Mocked
	private DialogUtil dialogUtil;

	public CancelSessionClickListenerTest() {}

	/**
	 * Tests whether or not the activity is finished directly.<p>
	 */
	@Test(dataProvider="HasChangedFlag")
	public void onClick(final boolean hasChanged)
	{
		/**
		 * Setup mocked environment
		 */
		setupMockButton();

		View.OnClickListener testListener = new CancelSessionClickListener(mockController);

		new NonStrictExpectations() {{
			mockController.hasChanged();
			result = hasChanged;

			/**
			 * Assert the showing of dialog
			 */
			DialogUtil.showOkOrCancelDialogOwnedByActivity(null, anyInt, null);
			times = hasChanged ? 1 : 0;
			// :~)
		}};
		// :~)

		testListener.onClick(mockButton);

		new Verifications() {{
			mockActivity.finish();
			times = hasChanged ? 0 : 1;
		}};
	}

	@DataProvider(name="HasChangedFlag")
	private Object[][] getHasChangedFlag()
	{
		return new Object[][] {
			{ true },
			{ false }
		};
	}

	private void setupMockButton()
	{
		new NonStrictExpectations() {{
			mockButton.getContext();
			result = mockActivity;
		}};
	}
}
