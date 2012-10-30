package idv.mikelue.teatime.test;

import org.testng.annotations.BeforeClass;

import org.no_ip.mikelue.android.jmockit.MockLogToSlf4j;

/**
 * This base class provides mock environment of Andoird, which has following feature:
 *
 * <ol>
 * 	<li>Redirect {@link Log} to logging of SLF4J</li>
 * </ol>
 */
public abstract class AbstractMockAndroidEnvTestBase {
	protected AbstractMockAndroidEnvTestBase() {}

	/**
	 * Mocks the Log class of Android, redirecting the content of logging to
	 * SLF4J.<p>
	 */
	@BeforeClass
	protected void mockAndroidLog()
	{
		new MockLogToSlf4j();
	}
}
