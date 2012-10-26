package idv.mikelue.teatime;

/**
 * This interface defines the methods to start and to stop
 * round.<p>
 */
public interface TickingController {
	/**
	 * Stops a round by round's id.<p>
	 *
	 * @param roundId The id of round
	 */
	public void stopTicking(int roundId);
}
