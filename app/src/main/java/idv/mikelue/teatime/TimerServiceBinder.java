package idv.mikelue.teatime;

import android.os.Binder;

/**
 * This binder provides {@link TickingPublisher} and {@link TickingController} to
 * Activity for processing ticking.<p>
 */
public class TimerServiceBinder extends Binder {
	private TickingPublisher tickingPublisher;
	private TickingController tickingController;

	/**
	 * Constructs by must objects for processing ticking.<p>
	 *
	 * @param newTickingPublisher The publisher for ticking information
	 * @param newTickingController The controller for ticking
	 */
	public TimerServiceBinder(TickingPublisher newTickingPublisher, TickingController newTickingController)
	{
		tickingPublisher = newTickingPublisher;
		tickingController = newTickingController;
	}

	/**
	 * Gets the publisher of ticking.<p>
	 *
	 * @return The publisher
	 *
	 * @see #getTickingController
	 */
	public TickingPublisher getTickingPublisher()
	{
		return tickingPublisher;
	}

	/**
	 * Gets the controller of ticking.<p>
	 *
	 * @return The controller of ticking
	 *
	 * @see #getTickingPublisher
	 */
	public TickingController getTickingController()
	{
		return tickingController;
	}

	public void release()
	{
		tickingController = null;
		tickingPublisher = null;
	}
}
