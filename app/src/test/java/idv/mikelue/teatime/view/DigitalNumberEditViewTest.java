package idv.mikelue.teatime.view;

import mockit.Verifications;
import mockit.Mocked;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DigitalNumberEditViewTest {
	@Mocked(methods={"<init>", "setText", "setSelection"})
	private DigitalNumberEditView mockEditView;

	public DigitalNumberEditViewTest() {}

	/**
	 * Tests the fixing of changed text.<p>
	 */
	@Test(dataProvider="OnTextChange")
	public void onTextChange(
		String changedText, int start, int lengthBefore, int lengthAfter,
		final boolean hasFixed, final String expectedResult, final int expectedPosition
	) {
		DigitalNumberEditView testEditView = new DigitalNumberEditView(null, null);

		testEditView.onTextChanged(
			changedText, start, lengthBefore, lengthAfter
		);

		new Verifications() {{
			mockEditView.setText(withEqual(expectedResult));
			times = hasFixed ? 1 : 0;

			if (hasFixed) {
				mockEditView.setSelection(expectedPosition);
				times = 1;
			}
		}};
	}
	@DataProvider(name="OnTextChange")
	private Object[][] getOnTextChange()
	{
		return new Object[][] {
			{ "23", -1, -1, -1, false, null, -1 },
			{ "273", 0, 2, 1, true, "23", 1 },
			{ "273", 1, 2, 1, true, "27", 2 },
			{ "138", 2, 2, 1, true, "13", 2 },
			{ "", 0, 0, 0, true, "00", 0 },
			{ "4", 0, 0, 0, true, "04", 0 },
			{ "4", 1, 0, 0, true, "40", 1 },
			{ "6", 1, 0, 1, true, "60", 1 }
		};
	}
}
