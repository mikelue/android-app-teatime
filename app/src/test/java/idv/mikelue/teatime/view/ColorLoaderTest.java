package idv.mikelue.teatime.view;

import java.util.Map;

import mockit.Deencapsulation;
import mockit.Mocked;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import idv.mikelue.teatime.test.AbstractMockAndroidEnvTestBase;

public class ColorLoaderTest extends AbstractMockAndroidEnvTestBase {
	@Mocked(methods={"<init>"})
	private ColorLoader mockedLoaderInstance;

	public ColorLoaderTest() {}

	/**
	 * Tests the initialization of loade, which a new loader is added to the
	 * pool.<p>
	 */
	@Test
	public void initLoader()
	{
		final int testLoaderId = 23;

		ColorLoader.initLoader(null, testLoaderId);

		Assert.assertTrue(
			Deencapsulation.<Map<Integer, ColorLoader>>getField(ColorLoader.class, "loaderPool")
				.containsKey(testLoaderId)
		);
	}

	/**
	 * Tests the releasing of loader, which a loader is removed from pool.<p>
	 */
	@Test(dependsOnMethods="initLoader")
	public void releaseLoader()
	{
		final int testLoaderId = 23;

		ColorLoader.initLoader(null, testLoaderId);
		ColorLoader.releaseLoader(testLoaderId);

		Assert.assertFalse(
			Deencapsulation.<Map<Integer, ColorLoader>>getField(ColorLoader.class, "loaderPool")
				.containsKey(testLoaderId)
		);
	}
}
