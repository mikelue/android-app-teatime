package idv.mikelue.teatime.controller;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.Mockit;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import idv.mikelue.teatime.model.Session;

public class SessionEditorControllerTest {
	@Mocked
	private TextView mockViewOfSessionName;
	@Mocked
	private RoundEditorController mockRoundCtrl;

	public SessionEditorControllerTest() {}

	/**
	 * Tests the checking on whether or not the data has been changed.<p>
	 */
	@Test(dataProvider="HasChanged")
	public void hasChanged(
		final String testSessionName, Session.IconType testIconType, final boolean testRoundCtrlHasChaged,
		boolean expectedResult
	) {
		SessionEditorController testController = buildTestController();

		/**
		 * Setup the internal data of controller
		 */
		Session testSession = new Session(-1);
		testSession.setName("test-session-name");
		testSession.setIconType(Session.IconType.Type1);

		Deencapsulation.setField(testController, testSession);
		Deencapsulation.setField(testController, testIconType);

		new NonStrictExpectations() {{
			mockRoundCtrl.hasChanged();
			result = testRoundCtrlHasChaged;

			mockViewOfSessionName.getText();
			result = testSessionName;
		}};
		// :~)

		Assert.assertEquals(
			testController.hasChanged(),
			expectedResult
		);
	}
	@DataProvider(name="HasChanged")
	private Object[][] getHasChanged()
	{
		return new Object[][] {
			{ "test-session-name", Session.IconType.Type1, false, false },
			{ "changed-session-name", Session.IconType.Type1, false, true },
			{ "test-session-name", Session.IconType.Type2, false, true },
			{ "test-session-name", Session.IconType.Type1, true, true }
		};
	}

	private SessionEditorController buildTestController()
	{
		Mockit.stubOutClass(
			SessionEditorController.class,
			"<init>"
		);

		SessionEditorController testController = new SessionEditorController(null, null, null, null);
		Deencapsulation.setField(testController, mockRoundCtrl);
		Deencapsulation.setField(testController, mockViewOfSessionName);

		return testController;
	}
}
