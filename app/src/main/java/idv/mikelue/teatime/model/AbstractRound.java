package idv.mikelue.teatime.model;

public abstract class AbstractRound implements RoundInfo {
	private final int roundId;
	private int roundSequence = -1;
	private int numberOfRounds = -1;
	private Session.IconType iconType = null;
	private String name = null;

	/**
	 * Constructs this object id of round.<p>
	 *
	 * @param newId The id of round
	 */
	protected AbstractRound(int newId)
	{
		roundId = newId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getId()
	{
		return roundId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getRoundSequence()
	{
		return roundSequence;
	}
	/**
	 * Sets sequence of round.<p>
	 *
	 * @param newRoundSequence new sequence of round
	 *
	 * @see #getRoundSequence
	 */
	public void setRoundSequence(int newRoundSequence)
	{
		roundSequence = newRoundSequence;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getNumberOfRounds()
	{
		return numberOfRounds;
	}
	/**
	 * Sets number of rounds in session.<p>
	 *
	 * @param newNumberOfRounds new number of rounds in session
	 *
	 * @see #getNumberOfRounds
	 */
	public void setNumberOfRounds(int newNumberOfRounds)
	{
		numberOfRounds = newNumberOfRounds;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Session.IconType getIconType()
	{
		return iconType;
	}
	/**
	 * Sets type of icon.<p>
	 *
	 * @param newIconType new type of icon
	 *
	 * @see #getIconType
	 */
	public void setIconType(Session.IconType newIconType)
	{
		iconType = newIconType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName()
	{
		return name;
	}
	/**
	 * Sets name of session.<p>
	 *
	 * @param newName new name of session
	 *
	 * @see #getName
	 */
	public void setName(String newName)
	{
		name = newName;
	}

	/**
	 * The hash code of this object is as same as value from {@link #getId()}.<p>
	 *
	 * @return The id of round
	 */
	@Override
	public int hashCode()
	{
		return getId();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null) { return false; }
		if (obj == this) { return true; }

		if (obj.getClass() != getClass()) {
			return false;
		}

		AbstractRound rhs = (AbstractRound) obj;
		return getId() == rhs.getId();
	}
}
