package idv.mikelue.teatime.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import idv.mikelue.teatime.R;
import idv.mikelue.teatime.database.TeaTimeDao;
import idv.mikelue.teatime.model.Session;
import idv.mikelue.teatime.model.Session.IconType;
import idv.mikelue.teatime.view.ColorLoader;
import idv.mikelue.teatime.view.ColorSelectionAdapter;
import static idv.mikelue.teatime.SessionActivity.NEW_SESSION_ID;
import static idv.mikelue.teatime.view.DialogUtil.showOkOrCancelDialog;
import static idv.mikelue.teatime.view.DialogUtil.showOkOrCancelDialogOwnedByActivity;
import static idv.mikelue.teatime.view.WarningToast.toastSimpleMessage;

/**
 * This controller co-ordinates the modification of data of session.<p>
 */
public class SessionEditorController {
	private final static String TAG = "SessionEditCtrl";

	private RoundEditorController roundCtrl;
	private TeaTimeDao teaTimeDao;

	private TextView viewOfSessionName;
	private ImageView viewOfSessionTheme;

	private final Session ordinarySession;

	private IconType currentSessionColorValue;
	private int colorLoaderId;

	/**
	 * Constructs with environment and needed services.<p>
	 *
	 * @param activity The host of this controller
	 * @param session The data of session
	 * @param newPersistDao The DAO to access persisted data
	 * @param newRoundCtrl The controller of round to make change of theme color
	 */
	public SessionEditorController(
		Activity activity, Session session,
		TeaTimeDao newPersistDao, RoundEditorController newRoundCtrl
	) {
		roundCtrl = newRoundCtrl;
		teaTimeDao = newPersistDao;
		colorLoaderId = activity.hashCode();

		viewOfSessionName = (TextView)activity.findViewById(R.id.text_session_name);
		viewOfSessionTheme = (ImageView)activity.findViewById(R.id.session_icon);

		ordinarySession = session;
		currentSessionColorValue = ordinarySession.getIconType();

		setupViews(activity);
		setupRemoveButton(activity);
		setupColorSelection(activity);
		setupSaveAndCancelButton(activity);
	}

	/**
	 * Checks on whether the data of session(includes rounds) has been changed.<p>
	 */
	boolean hasChanged()
	{
		return roundCtrl.hasChanged() ||
			currentSessionColorValue != ordinarySession.getIconType() ||
			!ordinarySession.getName().contentEquals(viewOfSessionName.getText());
	}

	String getSessionNameOfView()
	{
		return viewOfSessionName.getText().toString();
	}

	void removeSession()
	{
		teaTimeDao.removeSession(ordinarySession.getId());
	}

	void saveSession()
	{
		String newNameOfSession = getSessionNameOfView();

		/**
		 * Insert/Update data into persisted database
		 */
		if (ordinarySession.getId() == NEW_SESSION_ID) {
			insertNewSession(newNameOfSession);
		} else {
			updateSession(newNameOfSession);
		}
		// :~)
	}
	void setSessionTheme(Context context, IconType newIconType)
	{
		viewOfSessionTheme.setBackgroundDrawable(
			ColorLoader.getBackground(colorLoaderId, newIconType)
		);
		currentSessionColorValue = newIconType;
		roundCtrl.setThemeColor(newIconType);
	}

	boolean isNewSession()
	{
		return ordinarySession.getId() == NEW_SESSION_ID;
	}

	private void insertNewSession(String nameOfSession)
	{
		Session newSession = new Session(ordinarySession.getId());
		newSession.setName(nameOfSession);
		newSession.setIconType(currentSessionColorValue);

		Cursor rounds = roundCtrl.getRounds();
		teaTimeDao.addNewSession(newSession, rounds);
		rounds.close();
	}
	private void updateSession(String nameOfSession)
	{
		/**
		 * Updates the data into persisted database
		 */
		Session updatedSession = new Session(ordinarySession.getId());
		updatedSession.setName(nameOfSession);
		updatedSession.setIconType(currentSessionColorValue);

		Cursor rounds = null;
		if (roundCtrl.hasChanged()) {
			rounds = roundCtrl.getRounds();
		}

		teaTimeDao.modifySession(updatedSession, rounds);

		if (rounds != null) {
			rounds.close();
		}
		// :~)
	}

	private void setupViews(Context context)
	{
		/**
		 * Setup content of views
		 */
		viewOfSessionName.setText(ordinarySession.getName());
		viewOfSessionTheme.setBackgroundDrawable(
			ColorLoader.getBackground(
				colorLoaderId,
				ordinarySession.getIconType()
			)
		);
		// :~)
	}

	/**
	 * Registers the listener to button of removal of this session.<p>
	 */
	private void setupRemoveButton(Activity viewContainer)
	{
		/**
		 * If user ensure to remove this session, this handler brings user back to
		 * main activity.
		 */
		viewContainer.findViewById(R.id.button_remove_session).setOnClickListener(
			new RemoveSessionClickListener(this)
		);
	}

	private void setupSaveAndCancelButton(Activity viewContainer)
	{
		viewContainer.findViewById(R.id.button_cancel).setOnClickListener(new CancelSessionClickListener(this));
		viewContainer.findViewById(R.id.button_save).setOnClickListener(new SaveSessionClickListener(this));
	}

	private void setupColorSelection(Activity viewContainer)
	{
		viewContainer.findViewById(R.id.session_icon).setOnClickListener(new ColorPickerListener(this));
	}
}

class SaveSessionClickListener implements View.OnClickListener {
	private SessionEditorController sessionCtrl;

	SaveSessionClickListener(SessionEditorController newSessionCtrl)
	{
		sessionCtrl = newSessionCtrl;
	}

	@Override
	public void onClick(View button)
	{
		Activity activity = (Activity)button.getContext();

		if (!sessionCtrl.hasChanged()) {
			activity.finish();
			return;
		}

		if (!checkOnData(activity)) {
			return;
		}

		showOkOrCancelDialogOwnedByActivity(
			activity, R.string.message_save_session_modification,
			new DialogOnClickListener()
		);
	}

	private boolean checkOnData(Context context)
	{
		if (sessionCtrl.getSessionNameOfView().trim().length() == 0) {
			/**
			 * Toast error message to user
			 */
			toastSimpleMessage(
				context,
				R.string.message_need_session_name
			);
			// :~)

			return false;
		}

		return true;
	}

	/**
	 * Handles the click of saving.<p>
	 */
	private class DialogOnClickListener implements DialogInterface.OnClickListener {
		DialogOnClickListener() {}

		@Override
		public void onClick(DialogInterface dialog, int which)
		{
			if (which == DialogInterface.BUTTON_NEGATIVE) {
				return;
			}

			sessionCtrl.saveSession();
			Activity activity = ((AlertDialog)dialog).getOwnerActivity();

			/**
			 * Toast message to user
			 */
			toastSimpleMessage(
				activity.getApplicationContext(),
				activity.getResources().getString(
					R.string.message_session_has_been_saved,
					sessionCtrl.getSessionNameOfView()
				)
			);
			// :~)

			activity.finish();
		}
	};
}

class CancelSessionClickListener implements View.OnClickListener {
	private SessionEditorController sessionCtrl;

	CancelSessionClickListener(SessionEditorController newSessionCtrl)
	{
		sessionCtrl = newSessionCtrl;
	}

	@Override
	public void onClick(View button)
	{
		Activity activity = (Activity)button.getContext();

		if (!sessionCtrl.hasChanged()) {
			activity.finish();
			return;
		}

		showOkOrCancelDialogOwnedByActivity(activity, R.string.message_cancel_session_modification, new DialogOnClickListener());
	}

	/**
	 * Handles the click of cancelation.<p>
	 */
	private static class DialogOnClickListener implements DialogInterface.OnClickListener {
		DialogOnClickListener() {}

		@Override
		public void onClick(DialogInterface dialog, int which)
		{
			if (which == DialogInterface.BUTTON_NEGATIVE) {
				return;
			}

			/**
			 * Cancel this activity
			 */
			((AlertDialog)dialog).getOwnerActivity().finish();
			// :~)
		}
	};
}

class RemoveSessionClickListener implements View.OnClickListener {
	private SessionEditorController sessionCtrl;

	RemoveSessionClickListener(SessionEditorController newSessionCtrl)
	{
		sessionCtrl = newSessionCtrl;
	}

	@Override
	public void onClick(View button)
	{
		Activity activity = (Activity)button.getContext();

		if (sessionCtrl.isNewSession()) {
			activity.finish();
			return;
		}

		showOkOrCancelDialogOwnedByActivity(activity, R.string.message_remove_session, new DialogOnClickListener());
	}

	/**
	 * Handles the click of removal to session.<p>
	 */
	private class DialogOnClickListener implements DialogInterface.OnClickListener {
		DialogOnClickListener() {}

		@Override
		public void onClick(DialogInterface dialog, int which)
		{
			if (which == DialogInterface.BUTTON_NEGATIVE) {
				return;
			}

			/**
			 * Remove and toast message to user
			 */
			sessionCtrl.removeSession();
			Activity activity = ((AlertDialog)dialog).getOwnerActivity();

			toastSimpleMessage(
				activity.getApplicationContext(),
				activity.getResources().getString(
					R.string.message_remove_session_successfully,
					sessionCtrl.getSessionNameOfView()
				)
			);
			// :~)

			activity.finish();
		}
	};
}

class ColorPickerListener implements View.OnClickListener {
	private AlertDialog colorDialog = null;

	private SessionEditorController sessionCtrl;

	ColorPickerListener(SessionEditorController newSessionCtrl)
	{
		sessionCtrl = newSessionCtrl;
	}

	@Override
	public void onClick(View button)
	{
		Activity activity = (Activity)button.getContext();
		showColorSelectionDialog(activity);
	}

	/**
	 * Shows the dialog for selection of theme color of session.<p>
	 */
	private void showColorSelectionDialog(Context context)
	{
		if (colorDialog == null) {
			colorDialog = buildColorSelectionDialog(context);
		}

		colorDialog.show();
	}
	private AlertDialog buildColorSelectionDialog(final Context context)
	{
		/**
		 * Setup the view for color selection on dialog
		 */
		ColorSelectionAdapter colorAdapter = new ColorSelectionAdapter(context);
		colorAdapter.setIconActionListener(
			new ColorSelectionAdapter.IconActionListener() {
				@Override
				public void chooseIconColor(IconType chosenIconType)
				{
					sessionCtrl.setSessionTheme(context, chosenIconType);
					colorDialog.dismiss();
				}
			}
		);

		GridView viewOfColors = (GridView) LayoutInflater.from(context).inflate(
			R.layout.color_selection, null
		);
		viewOfColors.setAdapter(colorAdapter);
		// :~)

		return new AlertDialog.Builder(context)
			.setTitle(R.string.message_choose_a_color)
			.setView(viewOfColors)
			.create();
	}
}
