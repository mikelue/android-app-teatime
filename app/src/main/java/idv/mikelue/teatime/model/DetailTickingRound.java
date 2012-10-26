package idv.mikelue.teatime.model;

/**
 * Detailed information of ticking round.<p>
 *
 * This object is the superset of {@link TickingRound}.<p>
 */
public class DetailTickingRound extends AbstractRound {
	private final int timeOfRound;
	private int remainTime;
	private int sessionId;

	/**
	 * Constructs this round with round's data
	 *
	 * @param newRoundId The id of round
	 * @param newSeconds The seconds of round
	 */
	public DetailTickingRound(int newRoundId, int newSeconds)
	{
		super(newRoundId);
		timeOfRound = newSeconds;
		remainTime = timeOfRound;
	}

	/**
	 * Copy contructor. The {@link #getRemainSeconds()} won't be cloned.<p>
	 *
	 * @param sourceRound The source of round
	 */
	public DetailTickingRound(DetailTickingRound sourceRound)
	{
		this(sourceRound.getId(), sourceRound.timeOfRound);
	}

	/**
	 * Gets the data of session.<p>
	 *
	 * @return The object of session
	 */
	public Session getSession()
	{
		Session session = new Session(getSessionId());
		session.setName(getName());
		session.setIconType(getIconType());
		session.setNumberOfRounds(getNumberOfRounds());

		return session;
	}

	/**
	 * Build the object of {@link TickingRound}.<p>
	 *
	 * @return Safe-copied data of round for ticking
	 */
	public TickingRound getTickingRound()
	{
		return new TickingRound(getId(), getRemainSeconds());
	}

	/**
	 * Gets remain seconds of this round.<p>
	 *
	 * @return remain seconds of this round
	 *
	 * @see #setRemainSeconds
	 */
	public int getRemainSeconds()
	{
		return remainTime;
	}
	/**
	 * Sets remain seconds of this round.<p>
	 *
	 * @param newRemainTime The remain time
	 *
	 * @see #getRemainSeconds
	 */
	public void setRemainSeconds(int newRemainTime)
	{
		remainTime = newRemainTime;
	}

	/**
	 * Resets the remain time back to time of round.<p>
	 */
	public void resetTime()
	{
		remainTime = timeOfRound;
	}

	/**
	 * Gets id of session.<p>
	 *
	 * @return id of session
	 *
	 * @see #setSessionId
	 */
	public int getSessionId()
	{
		return sessionId;
	}
	/**
	 * Sets id of session.<p>
	 *
	 * @param newSessionId new id of session
	 *
	 * @see #getSessionId
	 */
	public void setSessionId(int newSessionId)
	{
		sessionId = newSessionId;
	}

	/**
	 * Checks whether the {@link #getRemainSeconds remain seconds} is 0.<p>
	 *
	 * @return true if the remain seconds is 0
	 */
	public boolean isEnded()
	{
		return remainTime <= 0;
	}
}
