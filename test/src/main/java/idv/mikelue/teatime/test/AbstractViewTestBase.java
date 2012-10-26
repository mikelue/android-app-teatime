package idv.mikelue.teatime.test;

import android.content.Context;

import idv.mikelue.teatime.view.ColorLoader;

/**
 * Provides base environemnt to test view objects.<p>
 */
public abstract class AbstractViewTestBase extends AbstractInstrumentationTestBase {
	protected AbstractViewTestBase() {}

	@Override
	protected void setUp()
	{
		Context context = getInstrumentation().getTargetContext();
		ColorLoader.initLoader(context, context.hashCode());
	}
	@Override
	protected void tearDown()
	{
		ColorLoader.releaseLoader(getInstrumentation().getTargetContext().hashCode());
	}
}
