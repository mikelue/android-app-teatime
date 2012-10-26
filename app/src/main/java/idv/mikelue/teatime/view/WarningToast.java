package idv.mikelue.teatime.view;

import android.content.Context;
import android.widget.Toast;

/**
 * Utility to show a toast.<p>
 */
public class WarningToast {
	private WarningToast() {}

	/**
	 * Shows a toast with id of string resource.<p>
	 *
	 * @param resId The id of text resource
	 *
	 * @see #toastSimpleMessage(Context,  CharSequence)
	 */
	public static void toastSimpleMessage(Context context, int resId)
	{
		Toast toast = Toast.makeText(context, resId, Toast.LENGTH_SHORT);
		toast.show();
	}
	/**
	 * Shows a toast with id of string resource.<p>
	 *
	 * @param resId The id of text resource
	 *
	 * @see #toastSimpleMessage(Context, int)
	 */
	public static void toastSimpleMessage(Context context, CharSequence text)
	{
		Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
		toast.show();
	}
}
