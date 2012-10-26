package idv.mikelue.teatime.ticking;

import java.util.HashSet;
import java.util.Set;
import static java.util.Collections.unmodifiableSet;

import idv.mikelue.teatime.model.TickingRound;
import static idv.mikelue.teatime.TeaTimeActivity.DEFAULT_CONCURRENT_TICKING_ROUNDS;

/**
 * This pool contains rounds which are ticking with {@link #performDecrease}
 * method to decrease all alive rounds in pool.<p>
 *
 * To make things simple, this object is dedicated to the management of ticking
 * rounds. <b>So the implementation is not thread-safe hence client has
 * responsibility to keep safe in multi-thread environment.</b><p>
 *
 * The type of collection used inside is {@link Set}, you may check the
 * document of {@link TickingRound#hashCode}.<p>
 */
public class TickingPool {
	private Set<TickingRound> aliveRounds = new HashSet<TickingRound>(DEFAULT_CONCURRENT_TICKING_ROUNDS);
	private Set<TickingRound> endRounds = new HashSet<TickingRound>(DEFAULT_CONCURRENT_TICKING_ROUNDS);

	public TickingPool() {}

	/**
	 * Adds a new ticking round into this pool. This method copies the data of
	 * newTickingRound in case of unexpeced modification from outside.<p>
	 *
	 * If the added round has existed, this method would throw {@link RuntimeException}.
	 *
	 * @param newTickingRound new ticking round
	 */
	public void addTickingRound(TickingRound newTickingRound)
	{
		if (aliveRounds.contains(newTickingRound)) {
			throw new RuntimeException("Add a duplicated ticking round. Id:[" + newTickingRound.getId() + "]");
		}

		aliveRounds.add(new TickingRound(newTickingRound));
	}

	/**
	 * Decrease time of all added rounds.<p>
	 *
	 * Client could use {@link #consumeEndedRounds} and {@link #getAliveRounds}
	 * to collect rounds with different states.<p>
	 *
	 * @see #consumeEndedRounds
	 * @see #getAliveRounds
	 */
	public void performDecrease()
	{
		for (TickingRound tickingRound: new HashSet<TickingRound>(aliveRounds)) {
			tickingRound.decrease();

			/**
			 * Move the ended rounds out from pool and put it into pool for end
			 * rounds
			 */
			if (tickingRound.isEnded()) {
				aliveRounds.remove(tickingRound);
				endRounds.add(tickingRound);
			}
			// :~)
		}
	}

	/**
	 * Gets the ended rounds and remove it from this pool.<p>
	 *
	 * @return The rounds which are end or empty list
	 *
	 * @see #getAliveRounds
	 */
	public Set<TickingRound> consumeEndedRounds()
	{
		Set<TickingRound> resultRounds = new HashSet<TickingRound>(endRounds.size());

		for (TickingRound endRound: new HashSet<TickingRound>(endRounds)) {
			endRounds.remove(endRound);
			resultRounds.add(new TickingRound(endRound));
		}

		return resultRounds;
	}

	/**
	 * Removes an alive round from pool.<p>
	 *
	 * @param roundId The id of round
	 *
	 * @return true if this pool contained the round(by its id)
	 */
	public boolean removeAliveRound(int roundId)
	{
		TickingRound removedRound = new TickingRound(roundId, -1);
		return aliveRounds.remove(removedRound);
	}

	/**
	 * Gets the alive rounds. This method copies data of round in case of
	 * unexpeced modification from outside.<p>
	 *
	 * @see #consumeEndedRounds
	 */
	public Set<TickingRound> getAliveRounds()
	{
		Set<TickingRound> resultRounds = new HashSet<TickingRound>(aliveRounds.size());

		for (TickingRound aliveRound: unmodifiableSet(aliveRounds)) {
			resultRounds.add(new TickingRound(aliveRound));
		}

		return resultRounds;
	}

	/**
	 * Gets the number of alive rounds. Since the {@link #getAliveRounds}
	 * returns with copied data, this method is suggested to <b>better
	 * performance</b> instead of {@link #getAliveRounds}{@link Set#size .size()}.<p>
	 *
	 * @return The number of alive rounds
	 */
	public int getNumberOfAliveRounds()
	{
		return aliveRounds.size();
	}
}
