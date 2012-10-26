package idv.mikelue.teatime.model;

import org.testng.Assert;
import org.testng.annotations.Test;

import idv.mikelue.teatime.R;
import idv.mikelue.teatime.model.Session.IconType;

public class SessionTest {
	public SessionTest() {}

	/**
	 * Tests the mapping from value of database to {@link Session.IconType}.<p>
	 */
	@Test
	public void valueOfDatabaseValueForIconType()
	{
		Assert.assertEquals(IconType.valueOfDatabaseValue(7), IconType.Type7);
	}
}
