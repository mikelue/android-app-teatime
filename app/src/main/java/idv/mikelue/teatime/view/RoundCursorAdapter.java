package idv.mikelue.teatime.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.TextView;

import idv.mikelue.teatime.R;
import idv.mikelue.teatime.model.Session.IconType;
import static idv.mikelue.teatime.util.TextFormatter.formatTime;

/**
 * This adapter constructs the view to display detailed information of
 * rounds.<p>
 */
public class RoundCursorAdapter extends SimpleCursorAdapter {
	/**
	 * Defines the action performed on item of round.<p>
	 */
	public interface RoundActionListener {
		/**
		 * Called if user clicks the "add" button.<p>
		 *
		 * @param roundIdOfSource The id of source round to be copied
		 */
		public void addBy(int roundIdOfSource);
		/**
		 * Called if user agrees to remove a round.<p>
		 *
		 * @param roundId The id of round to be removed
		 */
		public void remove(int roundId);
		/**
		 * Called if user resets a round.<p>
		 *
		 * @param roundId The id of round to be reset
		 */
		public void reset(int roundId);
		/**
		 * Called if user skips a round.<p>
		 *
		 * @param roundId The id of round to be skipped
		 */
		public void skip(int roundId);
		/**
		 * Called if user modifies time of a round.<p>
		 *
		 * @param roundId The id of round to be skipped
		 * @param newSeconds The new value of seconds
		 * @param affectSuccessiveRounds Whether the modification applies to concecutive rounds
		 */
		public void setTime(int roundId, int newSeconds, boolean affectSuccessiveRounds);
	};

	private Drawable evenItemBackground;
	private int colorLoaderId;
	private String textForNumberOfRounds = "N/A";
	private TextColorSetter textColorSetter;

	private View.OnClickListener buttonClickListener;

	public RoundCursorAdapter(Context context, Cursor cursor)
	{
		super(
			context, R.layout.round_item_layout, cursor,
			new String[] {
				"rd_sequence", "rd_seconds"
			},
			new int[] {
				R.id.round_sequence, R.id.round_time
			},
			SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
		);

		colorLoaderId = context.hashCode();

		textColorSetter = new TextColorSetter(
			context.getResources().getColor(R.color.round_text)
		);

		evenItemBackground = context.getResources().getDrawable(R.drawable.even_round_item_background);
		setViewBinder(new RoundViewBinder(textColorSetter));
	}

	/**
	 * Sets the listener for actions on item of round.<p>
	 *
	 * @param newActionListener The listener of round
	 */
	public void setActionListener(RoundActionListener newActionListener)
	{
		buttonClickListener = new RoundItemClickListener(newActionListener);
	}

	/**
	 * Sets the theme color of host session.<p>
	 *
	 * @param newColor The color of theme in session
	 */
	public void setThemeColor(IconType newColor)
	{
		textColorSetter.setThemeColorOfText(
			ColorLoader.getColor(colorLoaderId, newColor)
		);
	}

	/**
	 * Sets the number of rounds in session.<p>
	 *
	 * @param newNumberOfRounds The number of rounds
	 */
	public void setNumberOfRounds(int newNumberOfRounds)
	{
		textForNumberOfRounds = String.format("/%d", newNumberOfRounds);
	}

	/**
	 * Sets the background of item if the sequence is an odd number.<p>
	 */
	@Override
	public void bindView(View view, Context context, Cursor cursor)
	{
		super.bindView(view, context, cursor);

		if (cursor.getPosition() % 2 == 0) {
			view.setBackgroundDrawable(evenItemBackground);
		} else {
			view.setBackgroundDrawable(null);
		}

		/**
		 * Setup the listener to buttons and data to item
		 */
		RoundHolder holder = (RoundHolder)view.getTag();
		if (holder == null) {
			ViewGroup itemView = (ViewGroup)view;

			holder = new RoundHolder();
			holder.viewOfNumberOfRounds = (TextView)itemView.findViewById(R.id.number_of_rounds);

			itemView.findViewById(R.id.button_add_round).setOnClickListener(buttonClickListener);
			itemView.findViewById(R.id.button_remove_round).setOnClickListener(buttonClickListener);
			itemView.findViewById(R.id.button_setting).setOnClickListener(buttonClickListener);
			itemView.findViewById(R.id.round_time).setOnClickListener(buttonClickListener);

			itemView.setTag(holder);
		}

		holder.id = cursor.getInt(cursor.getColumnIndex("rd_id"));
		holder.seconds = cursor.getInt(cursor.getColumnIndex("rd_seconds"));
		// :~)

		/**
		 * Set the text for number of rounds
		 */
		holder.viewOfNumberOfRounds.setText(textForNumberOfRounds);
		textColorSetter.setTextColorForEndedRound(holder.viewOfNumberOfRounds, cursor);
		// :~)
	}
}

class RoundHolder {
	int id;
	int seconds;
	TextView viewOfNumberOfRounds;

	RoundHolder() {}
}

class RoundViewBinder implements SimpleCursorAdapter.ViewBinder {
	private TextColorSetter textColorSetter;

	/**
	 * Prepares this object with setter of text color.<p>
	 *
	 * @param newTextColorSetter The setter of text color
	 */
	RoundViewBinder(TextColorSetter newTextColorSetter)
	{
		textColorSetter = newTextColorSetter;
	}

	@Override
	public boolean setViewValue(View view, Cursor cursor, int columnIndex)
	{
		switch (view.getId()) {
			case R.id.round_sequence:
				AdjustableWidthTextView viewOfRoundSequence = (AdjustableWidthTextView)view;
				int sequenceForDisplaying = cursor.getPosition() + 1;
				viewOfRoundSequence.setText(String.valueOf(sequenceForDisplaying)); // Uses the calcualted number as sequence for displaying
				viewOfRoundSequence.adjustWidth(sequenceForDisplaying);
				textColorSetter.setTextColorForEndedRound(viewOfRoundSequence, cursor);
				break;
			case R.id.round_time:
				TextView viewOfRoundTime = (TextView)view;
				viewOfRoundTime.setText(formatTime(cursor.getInt(columnIndex)));
				textColorSetter.setTextColorForEndedRound(viewOfRoundTime, cursor);
				break;
			default:
				return false;
		}

		return true;
	}
}

/**
 * Utility to set the color of text.<p>
 */
class TextColorSetter {
	private int themeColorOfText = -1;
	private int defaultColorOfText = -1;

	TextColorSetter(int newDefaultColorOfText)
	{
		defaultColorOfText = newDefaultColorOfText;
	}

	/**
	 * Sets the theme color of text.<p>
	 *
	 * @param newThemeColorOfText The theme color of text
	 */
	void setThemeColorOfText(int newThemeColorOfText)
	{
		themeColorOfText = newThemeColorOfText;
	}

	private int indexOfLastUsedTime = -1;

	/**
	 * Sets the color of text by status of round.<p>
	 *
	 * @param textView The view to be set
	 * @param cursor Check the "rd_time_last_reached" value for ended status
	 */
	void setTextColorForEndedRound(TextView textView, Cursor cursor)
	{
		if (indexOfLastUsedTime == -1) {
			indexOfLastUsedTime = cursor.getColumnIndex("rd_time_last_reached");
		}

		if (!cursor.isNull(indexOfLastUsedTime)) {
			textView.setTextColor(themeColorOfText);
		} else {
			textView.setTextColor(defaultColorOfText);
		}
	}
}

class RoundItemClickListener implements View.OnClickListener {
	private RoundCursorAdapter.RoundActionListener actionListener;

	RoundItemClickListener(RoundCursorAdapter.RoundActionListener newActionListener)
	{
		actionListener = newActionListener;
	}

	@Override
	public void onClick(View view)
	{
		RoundHolder roundHolder = (RoundHolder)((View)view.getParent()).getTag();

		switch (view.getId()) {
			case R.id.button_add_round:
				actionListener.addBy(roundHolder.id);
				break;
			case R.id.button_remove_round:
				actionListener.remove(roundHolder.id);
				break;
			case R.id.button_setting:
				doSetting(view.getContext(), roundHolder.id);
				break;
			/**
			 * The value of seconds is bound by RoundCursorAdapter(in tag of button)
			 */
			case R.id.round_time:
				doSetTime(view.getContext(), roundHolder.id, roundHolder.seconds);
				break;
			// :~)
		}
	}

	private final static int CMD_RESET = 0;
	private final static int CMD_SKIP = 1;
	private void doSetting(final Context context, final int roundId)
	{
		new AlertDialog.Builder(context)
			.setItems(
				/**
				 * The sequence of buttons must corresponds CMD_RESET and CMD_SKIP
				 */
				new String[] {
					context.getResources().getString(R.string.button_reset),
					context.getResources().getString(R.string.button_skip)
				},
				// :~)
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						switch (which) {
							case CMD_RESET:
								actionListener.reset(roundId);
								break;
							case CMD_SKIP:
								actionListener.skip(roundId);
								break;
							default:
								Log.e("SetRound", "Unknwon command. Which of AlarmDialog: " + which);
						}
					}
				}
			)
			.show();
	}

	private ChangeTimeDialog viewOfDialog;
	private int currentRoundId;
	private void doSetTime(Context context, int roundId, int seconds)
	{
		AlertDialog dialog = getDialog(context);

		currentRoundId = roundId;
		viewOfDialog.setTime(seconds);

		dialog.show();
	}

	private AlertDialog changeTimeDialog;
	private AlertDialog getDialog(final Context context)
	{
		if (changeTimeDialog != null) {
			return changeTimeDialog;
		}

		/**
		 * Build the view of dialog
		 */
		viewOfDialog = (ChangeTimeDialog)LayoutInflater.from(context)
			.inflate(R.layout.change_time_dialog, null);
		// :~)

		/**
		 * Handles the clicking of OK/Cancel button for dialog
		 */
		DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				if (which == AlertDialog.BUTTON_POSITIVE) {
					if (viewOfDialog.hasChanged()) {
						actionListener.setTime(currentRoundId, viewOfDialog.getTime(), viewOfDialog.isAffectSuccessiveRounds());
					}
				}

				/**
				 * Hides the keyboard
				 */
				InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(viewOfDialog.getWindowToken(), 0);
				// :~)

				dialog.dismiss();
			}
		};
		// :~)

		changeTimeDialog = new AlertDialog.Builder(context)
			.setView(viewOfDialog)
			.setPositiveButton(context.getResources().getString(R.string.button_ok), dialogListener)
			.setNegativeButton(context.getResources().getString(R.string.button_cancel), dialogListener)
			.create();

		return changeTimeDialog;
	}
}
