package idv.mikelue.teatime.ticking;

/**
 * Defines accuracy of ticking.<p>
 */
public interface TickingAccuracy {
	/**
	 * The rate of ticking({@value} milliseconds).<p>
	 */
	public final static int TICKING_RATE = 500;
	/**
	 * The ratio of ticking in 1 second.<p>
	 */
	public final static int TICKING_RATIO_OF_1_SECOND = 1000 / TICKING_RATE;
}
