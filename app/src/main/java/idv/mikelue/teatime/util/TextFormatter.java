package idv.mikelue.teatime.util;

/**
 * Utilities to format text.<p>
 */
public class TextFormatter {
	private TextFormatter() {}

	/**
	 * Generates the text used to show the {@code <last round>/<number of rounds>}.<p>
	 *
	 * @param lastEndedRound The last sequence of round
	 * @param numberOfRounds The number of rounds
	 *
	 * @return number/numberOfRounds
	 */
	public static String formatStateOfSession(int lastEndedRound, int numberOfRounds)
	{
		return String.format("%d/%d", lastEndedRound, numberOfRounds);
	}

	/**
	 * Generates the text used to show the time of round(MM:SS).<p>
	 *
	 * @param seconds The time in second
	 *
	 * @return +-MM:SS
	 *
	 * @see #formatTime(int, String)
	 */
	public static String formatTime(int seconds)
	{
		return formatTime(seconds, "");
	}

	/**
	 * Generates the text used to show the time of round(MM:SS).<p>
	 *
	 * @param seconds The time in second
	 * @param positiveSymbol The symbol used to show positive value
	 *
	 * @return +-MM:SS
	 *
	 * @see #formatTime(int)
	 */
	public static String formatTime(int seconds, String positiveSymbol)
	{
		int absSeconds = Math.abs(seconds);

		String minus = seconds >= 0 ? positiveSymbol : "-";

		return String.format("%s%02d:%02d", minus, absSeconds / 60, absSeconds % 60);
	}

	/**
	 * Generates the text used to show the minutes of time(2 digits).<p>
	 *
	 * @param seconds The value of time
	 *
	 * @return zero-padding 2 digits
	 */
	public static String formatAsMinutes(int seconds)
	{
		return String.format("%02d", seconds / 60);
	}

	/**
	 * Generates the text used to show the seconds of time(2 digits).<p>
	 *
	 * @param seconds The value of time
	 *
	 * @return zero-padding 2 digits
	 */
	public static String formatAsSeconds(int seconds)
	{
		return String.format("%02d", seconds % 60);
	}
}
