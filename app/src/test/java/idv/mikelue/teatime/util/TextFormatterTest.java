package idv.mikelue.teatime.util;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TextFormatterTest {
	public TextFormatterTest() {}

	/**
	 * Tests the result of format time.<p>
	 */
	@Test(dataProvider="FormatTime")
	public void formatTime(
		int seconds, String positiveSymbol, String expectedResult
	) {
		Assert.assertEquals(
			TextFormatter.formatTime(seconds, positiveSymbol),
			expectedResult
		);
	}

	@DataProvider(name="FormatTime")
	private Object[][] getFormatTime()
	{
		return new Object[][] {
			{ 45, "", "00:45" },
			{ 90, "", "01:30" },
			{ -45, "", "-00:45" },
			{ -90, "", "-01:30" },
			{ 45, "+", "+00:45" },
			{ 90, "+", "+01:30" },
			{ -45, "+", "-00:45" },
			{ -90, "+", "-01:30" }
		};
	}
}
