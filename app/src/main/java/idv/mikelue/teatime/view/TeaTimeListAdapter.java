package idv.mikelue.teatime.view;

import java.util.Set;
import java.util.HashSet;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import idv.mikelue.teatime.R;
import idv.mikelue.teatime.model.Session.IconType;
import static idv.mikelue.teatime.util.TextFormatter.formatStateOfSession;
import static idv.mikelue.teatime.util.TextFormatter.formatTime;
import static idv.mikelue.teatime.TeaTimeActivity.DEFAULT_CONCURRENT_TICKING_ROUNDS;

/**
 * This adapter constructs the view to display detailed information of sessions.<p>
 *
 * With {@link TeaTimeListAdapter#SessionActionListener SessionActionListener},
 * client can handle actions of on session without carrying out details of UI components.<p>
 *
 * @see #setSessionActionListener
 */
public class TeaTimeListAdapter extends SimpleCursorAdapter {
	/**
	 * This listener handles the callback of clicking on session item.<p>
	 */
	public interface SessionActionListener {
		/**
		 * User clicks the button of start ticking.<p>
		 *
		 * @param roundId The id of round
		 */
		public void startTicking(int roundId);
		/**
		 * User clicks the button of setting session.<p>
		 *
		 * @param roundId The id of round
		 */
		public void settingSession(int roundId);
	}

	private final Set<Integer> ruledOutSessionIds = new HashSet<Integer>(DEFAULT_CONCURRENT_TICKING_ROUNDS << 1);
	private OnClickListener buttonListener;
	private SkippableCursorProxy skipptableCursorProxy = new SkippableCursorProxy("ss_id");

	public TeaTimeListAdapter(Context context)
	{
		super(
			context, R.layout.session_item_layout,
			null,
			new String[] {
				"ss_icon_type", "ss_name", "seconds_of_next_round"
			},
			new int[] {
				R.id.session_icon, R.id.text_session_name, R.id.text_session_state
			},
			CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
		);

		setViewBinder(new SessionViewBinder(
			context.hashCode()
		));
	}

	/**
	 * Sets the {@link SessionActionListener} to be called while the buutons on
	 * session item is clicked.<p>
	 *
	 * @param newActionListener The action listener of session
	 */
	public void setSessionActionListener(SessionActionListener newActionListener)
	{
		buttonListener = new SessionActionOnClickListener(newActionListener);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor)
	{
		/**
		 * Rule out the view for hidden rounds
		 */
		// Move the cursor to the position skipping hidden rounds
		skipptableCursorProxy.moveCursorToNonSkippedPosition();
		// :~)

		super.bindView(view, context, cursor);

		ViewGroup viewOfItem = (ViewGroup)view;
		SessionHolder sessionHolder = (SessionHolder)view.getTag();
		if (sessionHolder == null) {

			sessionHolder = new SessionHolder();
			sessionHolder.startTickingButton = viewOfItem.findViewById(R.id.button_start_ticking);
			sessionHolder.settingButton = viewOfItem.findViewById(R.id.button_setting);

			sessionHolder.startTickingButton.setOnClickListener(buttonListener);
			sessionHolder.settingButton.setOnClickListener(buttonListener);

			viewOfItem.setTag(sessionHolder);
		}

		sessionHolder.roundId = cursor.getInt(cursor.getColumnIndex("id_of_next_round"));
	}

	@Override
	public int getCount()
	{
		return skipptableCursorProxy == null ?
			super.getCount() : skipptableCursorProxy.getCountOfCursor();
	}

	@Override
	public void notifyDataSetChanged()
	{
		skipptableCursorProxy.reset();
		super.notifyDataSetChanged();
	}

	@Override
	public void changeCursor(Cursor cursor)
	{
		/**
		 * Prepare the proxy to provide hidden rows in Cursor
		 */
		skipptableCursorProxy.setCursor(cursor);
		// :~)

		super.changeCursor(cursor);
	}

	/**
	 * Rules out a round from displaying.<p>
	 *
	 * @param sessionId The id of session
	 */
	public void ruleOutSession(int sessionId)
	{
		skipptableCursorProxy.skipRow(sessionId);
	}

	/**
	 * Brings in a view of round which is showed before.<p>
	 *
	 * @param sessionId The id of session
	 */
	public void bringInSession(int sessionId)
	{
		skipptableCursorProxy.removeSkip(sessionId);
	}
}

/**
 * Setup the view of item for session.<p>
 */
class SessionViewBinder implements ViewBinder {
	private int colorLoaderId;

	SessionViewBinder(int newColorLoaderId)
	{
		colorLoaderId = newColorLoaderId;
	}

	@Override
	public boolean setViewValue(View view, Cursor cursor, int columnIndex)
	{
		switch (view.getId()) {
			/**
			 * Setup the color of icon of session
			 */
			case R.id.session_icon:
				view.setBackgroundDrawable(ColorLoader.getBackground(
					colorLoaderId,
					IconType.valueOfDatabaseValue(cursor.getInt(columnIndex))
				));
				break;
			// :~)
			/**
			 * Setup the name of session
			 */
			case R.id.text_session_name:
				TextView viewOfSessionName = (TextView)view;
				viewOfSessionName.setText(cursor.getString(columnIndex));
				break;
			// :~)
			/**
			 * Setup the state of session
			 */
			case R.id.text_session_state:
				TextView viewOfSessionState = (TextView)view;
				viewOfSessionState.setText(String.format(
					"%s %s",
					formatTime(cursor.getInt(columnIndex)),
					formatStateOfSession(
						cursor.getInt(cursor.getColumnIndex("next_sequence_of_round")),
						cursor.getInt(cursor.getColumnIndex("ss_number_of_rounds"))
					)
				));
				viewOfSessionState.setTextColor(ColorLoader.getColor(
					colorLoaderId,
					IconType.valueOfDatabaseValue(cursor.getInt(cursor.getColumnIndex("ss_icon_type")))
				));
				break;
			// :~)
			default:
				return false;
		}

		return true;
	}
}

class SessionHolder {
	View startTickingButton;
	View settingButton;
	int roundId;
}

/**
 * Dedicate actions on session itme with corresponding data to listener.
 */
class SessionActionOnClickListener implements OnClickListener {
	private TeaTimeListAdapter.SessionActionListener actionListener;

	SessionActionOnClickListener(TeaTimeListAdapter.SessionActionListener newActionListener)
	{
		actionListener = newActionListener;
	}

	@Override
	public void onClick(View v)
	{
		SessionHolder sessionHolder = (SessionHolder)((View)v.getParent()).getTag();

		switch (v.getId()) {
			case R.id.button_start_ticking:
				actionListener.startTicking(sessionHolder.roundId);
				break;
			case R.id.button_setting:
				actionListener.settingSession(sessionHolder.roundId);
				break;
		}
	}
}

/**
 * Manages the skipping data; provides {@link #moveCursor} to move the cursor
 * to proper position which skips the hidden item
 */
class SkippableCursorProxy {
	private final Set<Integer> skippedIds = new HashSet<Integer>(DEFAULT_CONCURRENT_TICKING_ROUNDS << 1);
	private final String columnNameOfId;

	private Cursor cursor;
	private int columnIndexOfId;
	private int shiftOfSkippedRows = 0;

	SkippableCursorProxy(String newColumnNameOfId)
	{
		columnNameOfId = newColumnNameOfId;
	}

	/**
	 * Adds a skipping of row while loading data from cursor.<p>
	 *
	 * @param idOfRow The id of row, which is defined by {@link #SkippableCursorProxy}
	 */
	void skipRow(int idOfRow)
	{
		skippedIds.add(idOfRow);
	}

	/**
	 * Removes the skipping of row
	 *
	 * @param idOfRow The id of row to be undone while skipping
	 */
	void removeSkip(int idOfRow)
	{
		skippedIds.remove(idOfRow);
	}

	/**
	 * Gets the count of {@link Cursor}, which excludes the skipped rows.<p>
	 *
	 * @return The number of rows without skipped ones
	 */
	int getCountOfCursor()
	{
		return cursor == null ? 0 : cursor.getCount() -
			skippedIds.size();
	}

	/**
	 * Moves the {@link Cursor} to proper position, which skips the set
	 * rows.<p>
	 */
	void moveCursorToNonSkippedPosition()
	{
		// Move the cursor to the position skipping hidden rounds
		cursor.moveToPosition(cursor.getPosition() + shiftOfSkippedRows);

		int rowId = cursor.getInt(columnIndexOfId);
		while (skippedIds.contains(rowId))
		{
			++shiftOfSkippedRows;
			cursor.moveToNext();

			rowId = cursor.getInt(columnIndexOfId);
		}
		// :~)
	}

	void setCursor(Cursor newCursor)
	{
		cursor = newCursor;

		columnIndexOfId = cursor == null ? -1 :
			cursor.getColumnIndex(columnNameOfId);
	}

	/**
	 * Reset the skipping.<p>
	 */
	void reset()
	{
		shiftOfSkippedRows = 0;
	}
}
