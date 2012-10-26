package idv.mikelue.teatime.test;

import android.content.res.Resources;
import android.test.InstrumentationTestCase;

/**
 * Base environment for testing of instrumented context.<p>
 */
public abstract class AbstractInstrumentationTestBase extends InstrumentationTestCase {
	protected AbstractInstrumentationTestBase() {}

	protected Resources getTargetContextResources()
	{
		return getInstrumentation().getTargetContext().getResources();
	}
}
