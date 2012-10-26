package idv.mikelue.teatime.controller;

import android.support.v4.app.LoaderManager;

import mockit.Cascading;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.Mockit;
import mockit.Verifications;
import org.testng.Assert;
import org.testng.annotations.Test;

import idv.mikelue.teatime.database.StopgapRoundDao;
import idv.mikelue.teatime.model.Session;
import idv.mikelue.teatime.test.AbstractMockAndroidEnvTestBase;
import static idv.mikelue.teatime.SessionActivity.NEW_SESSION_ID;

/**
 * Tests the interal status of this controller.<p>
 */
public class RoundEditorControllerTest extends AbstractMockAndroidEnvTestBase {
	@Mocked @Cascading
	private LoaderManager mockLoaderManager;
	@Mocked
	private StopgapRoundDao mockStopgapDao;

	public RoundEditorControllerTest() {}

	/**
	 * While adding a new session over this controller:
	 *
	 * 1. Assert that the status is "changed"
	 * 2. The calling of loader is not activated
	 */
	@Test
	public void initNewSession()
	{
		RoundEditorController testController = buildTestController(new Session(NEW_SESSION_ID));
		testController.init();

		Assert.assertTrue(testController.hasChanged());

		new Verifications() {{
			mockLoaderManager.initLoader(anyInt, null, null);
			times = 0;

			mockStopgapDao.buildRoundsForNewSession();
			times = 1;
		}};
	}

	/**
	 * While editing a new session over this controller, the controller should
	 * start a loading to synchronize data from persisted database.<p>
	 */
	@Test
	public void initEditSession()
	{
		RoundEditorController testController = buildTestController(new Session(10));
		testController.init();

		Assert.assertFalse(testController.hasChanged());

		new Verifications() {{
			mockLoaderManager.initLoader(anyInt, null, null);
			times = 1;

			mockStopgapDao.buildRoundsForNewSession();
			times = 0;
		}};
	}

	private RoundEditorController buildTestController(Session session)
	{
		Mockit.stubOutClass(
			RoundEditorController.class,
			"<init>", "refreshAdapter"
		);

		RoundEditorController testController = new RoundEditorController(
			null, null, null, null
		);
		Deencapsulation.setField(testController, session);
		Deencapsulation.setField(testController, mockLoaderManager);
		Deencapsulation.setField(testController, mockStopgapDao);

		return testController;
	}
}
