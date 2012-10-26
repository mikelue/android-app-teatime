package idv.mikelue.teatime.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import idv.mikelue.teatime.R;

/**
 * Provides utility for dialog.<p>
 */
public class DialogUtil {
	public DialogUtil() {}

	/**
	 * Shows the Yes/No dialog, which is positive/negative of {@link AlertDialog}.<p>
	 *
	 * @param context The working context
	 * @param resId The resource id of text for dialog
	 * @param listener The listener used to handle clicking of buttons
	 */
	public static void showOkOrCancelDialog(Context context, int resId, DialogInterface.OnClickListener listener)
	{
		buildOkOrCancelDialog(context, resId, listener).show();
	}

	/**
	 * Shows the Yes/No dialog, which is positive/negative of {@link AlertDialog}.<p>
	 *
	 * The activity will be set into the dialog as owner activity.<p>
	 *
	 * @param activity The woning activity
	 * @param resId The resource id of text for dialog
	 * @param listener The listener used to handle clicking of buttons
	 */
	public static void showOkOrCancelDialogOwnedByActivity(Activity activity, int resId, DialogInterface.OnClickListener listener)
	{
		AlertDialog dialog = buildOkOrCancelDialog(activity, resId, listener);
		dialog.setOwnerActivity(activity);
		dialog.show();
	}

	/**
	 * Shows the Yes/No dialog, which is positive/negative of {@link AlertDialog}.<p>
	 *
	 * @param context The working context
	 * @param resId The resource id of text for dialog
	 * @param listener The listener used to handle clicking of buttons
	 */
	public static AlertDialog buildOkOrCancelDialog(Context context, int resId, DialogInterface.OnClickListener listener)
	{
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);

		dialogBuilder.setMessage(resId);
		dialogBuilder.setPositiveButton(R.string.button_ok, listener);
		dialogBuilder.setNegativeButton(R.string.button_cancel, listener);

		return dialogBuilder.create();
	}
}
