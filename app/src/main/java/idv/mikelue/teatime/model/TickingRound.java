package idv.mikelue.teatime.model;

import idv.mikelue.teatime.ticking.TickingAccuracy;
import static idv.mikelue.teatime.ticking.TickingAccuracy.TICKING_RATIO_OF_1_SECOND;

/**
 * This bean represents the data of a ticking round.<p>
 *
 * This object is the subset of {@link DetailTickingRound}.<p>
 */
public class TickingRound {
	private final int roundId;
	private int remainTime;

	/**
	 * Constructs this round with round's data
	 *
	 * @param newRoundId The id of round
	 * @param newSeconds The seconds of round
	 */
	public TickingRound(int newRoundId, int newSeconds)
	{
		roundId = newRoundId;
		remainTime = newSeconds * TICKING_RATIO_OF_1_SECOND;
	}

	/**
	 * Copy constructor.<p>
	 *
	 * @param copiedSource The source of copying
	 */
	public TickingRound(TickingRound copiedSource)
	{
		this(copiedSource.getId(), copiedSource.getRemainSeconds());
	}

	/**
	 * Copy constructor from {@link RoundInfo}.<p>
	 *
	 * @param srcTickingRound The source data of round
	 */
	public TickingRound(DetailTickingRound srcTickingRound)
	{
		this(srcTickingRound.getId(), srcTickingRound.getRemainSeconds());
	}

	/**
	 * Gets id of round.<p>
	 *
	 * @return id of round
	 */
	public int getId()
	{
		return roundId;
	}

	/**
	 * Gets remain seconds of this round.<p>
	 *
	 * @return remain seconds of this round
	 */
	public int getRemainSeconds()
	{
		int resultInSeconds = remainTime / TICKING_RATIO_OF_1_SECOND;

		/**
		 * Prevent the ticking from ending too early because the integer
		 * division of TICKING_RATIO_OF_1_SECOND may be 0 even if the remain
		 * time is large than zero.
		 */
		if (resultInSeconds == 0 && remainTime > 0) {
			return 1;
		}
		// :~)

		return resultInSeconds;
	}

	/**
	 * Decreases the value of round by {@link TickingAccuracy#TICKING_RATE} milliseconds.<p>
	 */
	public void decrease()
	{
		if (remainTime == 0) {
			return;
		}

		--remainTime;
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

		TickingRound rhs = (TickingRound) obj;
		return getId() == rhs.getId();
	}
}
