package idv.mikelue.teatime;

import java.util.Set;

import idv.mikelue.teatime.model.TickingRound;

/**
 * This interface defines the publisher used to get ticking state of rounds.<p>
 */
public interface TickingPublisher {
	/**
	 * Implemented by Activity to get changing of ticking round.<p>
	 *
	 * This observer should expected the last time receiving that
	 * a round having true value from {@link TickingRound#isEnded()}.<p>
	 */
	public interface RoundObserver {
		/**
		 * Service calls this method while there is a round changed.<p>
		 *
		 * @param tickingRound The changed round
		 */
		public void roundChanged(TickingRound tickingRound);
	}

	/**
	 * Registers a observer for round changing. This method is called while the
	 * Service is connected to Activity.<p>
	 *
	 * @param observer The round observer
	 *
	 * @see #unregisterRoundObserver
	 */
	public void registerRoundObserver(RoundObserver observer);

	/**
	 * Unregisters a observer for round changing. This method would be called
	 * while the Service is disconnected from Activity<p>
	 *
	 * @param registeredObserver The registered round observer
	 *
	 * @see #unregisterRoundObserver
	 */
	public void unregisterRoundObserver(RoundObserver registeredObserver);

	/**
	 * Gets the rounds which are ticking.<p>
	 *
	 * @return List of {@link TickingRound}s
	 */
	public Set<TickingRound> getTickingRounds();
}
