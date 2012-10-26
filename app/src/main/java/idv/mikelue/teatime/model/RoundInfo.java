package idv.mikelue.teatime.model;

/**
 * This type defines the information gather of a round.<p>
 */
public interface RoundInfo {
	/**
	 * The minimum seconds of a round.<p>
	 */
	public final static int MIN_SECONDS = 5; // 5 seconds of minimum
	/**
	 * The maximum seconds of a round.<p>
	 */
	public final static int MAX_SECONDS = 99 * 60 + 59; // 99 minutes and 59 seconds of maximum

	/**
	 * Gets id of round.<p>
	 *
	 * @return id of round
	 */
	public int getId();
	/**
	 * Gets name of session.<p>
	 *
	 * @return name of session
	 */
	public String getName();
	/**
	 * Gets sequence of round.<p>
	 *
	 * @return sequence of round
	 */
	public int getRoundSequence();
	/**
	 * Gets number of rounds in session.<p>
	 *
	 * @return number of rounds in session
	 */
	public int getNumberOfRounds();
	/**
	 * Gets type of icon.<p>
	 *
	 * @return type of icon
	 */
	public Session.IconType getIconType();
}
