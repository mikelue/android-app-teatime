package idv.mikelue.teatime.test;

import java.io.StringWriter;
import java.io.PrintWriter;

import android.util.Log;

import mockit.Mock;
import mockit.MockUp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This base class provides mock environment of Andoird, which has following feature:
 *
 * <ol>
 * 	<li>Redirect {@link Log} to logging of SLF4J</li>
 * </ol>
 */
public abstract class AbstractMockAndroidEnvTestBase {
	private Logger logger = LoggerFactory.getLogger(AbstractMockAndroidEnvTestBase.class);

	protected AbstractMockAndroidEnvTestBase() {}

	/**
	 * Mocks the Log class of Android, redirecting the content of logging to
	 * SLF4J.<p>
	 */
	@BeforeClass
	protected void mockAndroidLog()
	{
		logger.debug("Mock Log object of Android");

		new MockUp<Log>() {
			@Mock
			boolean isLoggable(String tag, int level)
			{
				Logger logger = LoggerFactory.getLogger(tag);

				switch (level) {
					case Log.ASSERT:
					case Log.ERROR:
						return logger.isErrorEnabled();
					case Log.WARN:
						return logger.isWarnEnabled();
					case Log.INFO:
						return logger.isInfoEnabled();
					case Log.DEBUG:
						return logger.isDebugEnabled();
					case Log.VERBOSE:
						return logger.isTraceEnabled();
				}

				logger.warn("Unknown Android Level[{}] for isLoggable(\"{}\", {})", tag, level);
				return true;
			}

			@Mock
			int println(int priority, String tag, String msg)
			{
				Logger logger = LoggerFactory.getLogger(tag);

				switch (priority) {
					case Log.ASSERT:
						logger.error("[Terrible Failure<DropBoxManager>] {}", msg);
						break;
					case Log.ERROR:
						logger.error(msg);
						break;
					case Log.WARN:
						logger.warn(msg);
						break;
					case Log.INFO:
						logger.info(msg);
						break;
					case Log.DEBUG:
						logger.debug(msg);
						break;
					case Log.VERBOSE:
						logger.trace(msg);
						break;
					default:
						logger.trace("<<Unknown Android Log Level[{}]>> {}", priority, msg);
						break;
				}

				return priority;
			}

			@Mock
			String getStackTraceString(Throwable tr)
			{
				StringWriter stringOutput = new StringWriter();
				tr.printStackTrace(new PrintWriter(stringOutput));
				return stringOutput.toString();
			}

			@Mock
			int e(String tag, String msg)
			{
				return println(Log.ERROR, tag, msg);
			}
			@Mock
			int e(String tag, String msg, Throwable tr)
			{
				return outputLog(Log.ERROR, tag, msg, tr);
			}
			@Mock
			int w(String tag, String msg)
			{
				return println(Log.WARN, tag, msg);
			}
			@Mock
			int w(String tag, String msg, Throwable tr)
			{
				return outputLog(Log.WARN, tag, msg, tr);
			}
			@Mock
			int w(String tag, Throwable tr)
			{
				return w(tag, getStackTraceString(tr));
			}
			@Mock
			int wtf(String tag, String msg)
			{
				return println(Log.ASSERT, tag, msg);
			}
			@Mock
			int wtf(String tag, String msg, Throwable tr)
			{
				return outputLog(Log.ASSERT, tag, msg, tr);
			}
			@Mock
			int wtf(String tag, Throwable tr)
			{
				return wtf(tag, getStackTraceString(tr));
			}
			@Mock
			int i(String tag, String msg)
			{
				return println(Log.INFO, tag, msg);
			}
			@Mock
			int i(String tag, String msg, Throwable tr)
			{
				return outputLog(Log.INFO, tag, msg, tr);
			}
			@Mock
			int d(String tag, String msg)
			{
				return println(Log.DEBUG, tag, msg);
			}
			@Mock
			int d(String tag, String msg, Throwable tr)
			{
				return outputLog(Log.DEBUG, tag, msg, tr);
			}
			@Mock
			int v(String tag, String msg)
			{
				return println(Log.VERBOSE, tag, msg);
			}
			@Mock
			int v(String tag, String msg, Throwable tr)
			{
				return outputLog(Log.VERBOSE, tag, msg, tr);
			}

			private int outputLog(int priority, String tag, String msg, Throwable tr)
			{
				Logger logger = LoggerFactory.getLogger(tag);

				switch (priority) {
					case Log.ASSERT:
						logger.error("[Terrible Failure<DropBoxManager>] {} {}", msg, tr);
						break;
					case Log.ERROR:
						logger.error(msg, tr);
						break;
					case Log.WARN:
						logger.warn(msg, tr);
						break;
					case Log.INFO:
						logger.info(msg, tr);
						break;
					case Log.DEBUG:
						logger.debug(msg, tr);
						break;
					case Log.VERBOSE:
						logger.trace(msg, tr);
						break;
					default:
						logger.error("<<Unknown Android Log Level[{}]>> {} {}", priority, msg, tr);
						break;
				}

				return priority;
			}
		};
	}
}
