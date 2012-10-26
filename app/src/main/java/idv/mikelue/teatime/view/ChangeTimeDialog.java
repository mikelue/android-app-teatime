package idv.mikelue.teatime.view;

import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import idv.mikelue.teatime.R;
import static idv.mikelue.teatime.model.RoundInfo.MAX_SECONDS;
import static idv.mikelue.teatime.model.RoundInfo.MIN_SECONDS;
import static idv.mikelue.teatime.util.TextFormatter.formatAsMinutes;
import static idv.mikelue.teatime.util.TextFormatter.formatAsSeconds;
import static idv.mikelue.teatime.util.TextFormatter.formatTime;

/**
 * This view provides functionalities for changing time.<p>
 *
 * The control of modification is done in this object, client uses
 * {@link #getTime()} and {@link #isAffectSuccessiveRounds()} to retrieve the result of modification.<p>
 */
public class ChangeTimeDialog extends RelativeLayout {
	private final static String TAG = ChangeTimeDialog.class.getSimpleName();

	private final int colorForPostiveValue;
	private final int colorForNegativeValue;

	private EditText editorOfMins;
	private EditText editorOfSecs;
	private TextView timeVector;
	private CheckBox affectConsecutiveRounds;

	private boolean affectSuccessiveRounds = false;
	private int ordinarySeconds = MIN_SECONDS;
	private int seconds = MIN_SECONDS;

	public ChangeTimeDialog(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		colorForPostiveValue = context.getResources().getColor(R.color.time_positive);
		colorForNegativeValue = context.getResources().getColor(R.color.time_negative);
	}

	@Override
	protected void onFinishInflate()
	{
		super.onFinishInflate();

		editorOfMins = (EditText)findViewById(R.id.editor_minutes);
		editorOfSecs = (EditText)findViewById(R.id.editor_seconds);
		timeVector = (TextView)findViewById(R.id.changing_time);
		affectConsecutiveRounds = (CheckBox)findViewById(R.id.affect_successive_rounds);

		setupTextEditors();
		setupButtons();
	}

	/**
	 * Checks on whether the time has been changed.<p>
	 *
	 * @return true if the time has been changed
	 */
	public boolean hasChanged()
	{
		return ordinarySeconds != seconds;
	}

	/**
	 * Sets the value of time to be changed.<p>
	 *
	 * @param newSeconds The seconds of time
	 */
	public void setTime(int newSeconds)
	{
		ordinarySeconds = newSeconds;
		seconds = ordinarySeconds;

		refreshView();
	}
	/**
	 * Gets the value of time.<p>
	 *
	 * @return The value of seconds
	 */
	public int getTime()
	{
		return seconds;
	}

	/**
	 * Sets the boolean for affecting successive rounds.<p>
	 *
	 * @param affecting Affects successive rounds
	 */
	public void setAffectSuccessiveRounds(boolean affecting)
	{
		affectConsecutiveRounds.setChecked(affecting);
	}

	/**
	 * Gets the flag whether to affect successive rounds.<p>
	 *
	 * @return true means affecting is wanted
	 */
	public boolean isAffectSuccessiveRounds()
	{
		return affectConsecutiveRounds.isChecked();
	}

	// Ensure that the listener of text editor won't be called twice
	private boolean changeBySelf = false;
	private void refreshView()
	{
		correctSeconds();

		/**
		 * Refresh the editors of time
		 */
		changeBySelf = true;
		editorOfMins.setTextKeepState(formatAsMinutes(seconds));
		editorOfSecs.setTextKeepState(formatAsSeconds(seconds));
		changeBySelf = false;
		// :~)

		/**
		 * Refresh the view of vector of time
		 */
		String textOfTime = "";

		int differenceOfChanging = seconds - ordinarySeconds;
		if (differenceOfChanging < 0) {
			timeVector.setTextColor(colorForNegativeValue);
		} else if (differenceOfChanging > 0) {
			timeVector.setTextColor(colorForPostiveValue);
		}

		if (differenceOfChanging != 0) {
			textOfTime = formatTime(differenceOfChanging, "+");
		}

		timeVector.setText(textOfTime);
		// :~)
	}

	/**
	 * Checks on and corrects the seconds if the new value after changing
	 * is not between valid range.<p>
	 */
	private void correctSeconds()
	{
		if (seconds > MAX_SECONDS) {
			seconds = MAX_SECONDS;
		} else if (seconds < MIN_SECONDS) {
			seconds = MIN_SECONDS;
		}
	}

	private void setupTextEditors()
	{
		TextWatcher timeEditorWatcher = new ValueUpdateWatcher();

		editorOfMins.addTextChangedListener(timeEditorWatcher);
		editorOfSecs.addTextChangedListener(timeEditorWatcher);
	}

	private void setupButtons()
	{
		/**
		 * Setup long click action for increase button
		 */
		setupLongClickAction(
			findViewById(R.id.button_increase_time),
			new LongPressAction(
				new Handler(),
				new LongPressAction.PressingActionListener() {
					@Override
					public void stillPress()
					{
						increaseSeconds();
					}
				}
			)
		);
		// :~)

		/**
		 * Setup long click action for decrease button
		 */
		setupLongClickAction(
			findViewById(R.id.button_decrease_time),
			new LongPressAction(
				new Handler(),
				new LongPressAction.PressingActionListener() {
					@Override
					public void stillPress()
					{
						decreaseSeconds();
					}
				}
			)
		);
		// :~)
	}

	private void increaseSeconds()
	{
		++seconds;
		refreshView();
	}
	private void decreaseSeconds()
	{
		--seconds;
		refreshView();
	}

	private void setupLongClickAction(View targetButton, LongPressAction pressingAction)
	{
		targetButton.setOnLongClickListener(pressingAction.getLongClickListener());
		targetButton.setOnTouchListener(pressingAction.getTouchListener());
	}

	private class ValueUpdateWatcher implements TextWatcher {
		ValueUpdateWatcher() {}

		@Override
		public void afterTextChanged(Editable s)
		{
			if (changeBySelf)  {
				return;
			}

			/**
			 * Update the value of seconds changed by EditText
			 */
			String mins = editorOfMins.getText().toString();
			String secs = editorOfSecs.getText().toString();

			int newSeconds = Integer.parseInt(mins) * 60 + Integer.parseInt(secs);

			if (seconds != newSeconds) {
				Log.d(TAG, String.format("Refresh time from [%d] to [%d]", seconds, newSeconds));
				seconds = newSeconds;
				refreshView();
			}
			// :~)

			/**
			 * If the cursor in editor of minutes has reached last position,
			 * the editor of seconds would be focused.
			 */
			if (editorOfMins.isFocused() &&
				editorOfMins.getSelectionStart() == 2 && editorOfMins.getSelectionEnd() == 2
			) {
				editorOfSecs.requestFocus();
				editorOfSecs.setSelection(0);
			}
			// :~)
		}
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {}
	}
}

/**
 * This action build listeners used to perform periodical action and stop
 * calling action while the user has relased touch.<p>
 */
class LongPressAction {
	interface PressingActionListener {
		void stillPress();
	}

	private final static int INIT_DEALY = 300;
	private final static int MIN_DEALY = 5;

	private final Handler handler;
	private final PressingActionListener pressingListener;
	private int delayOfNextAction = -1;
	private boolean pressing = false;

	/**
	 * Starts the periodical action.
	 */
	private View.OnLongClickListener longClickListener = new View.OnLongClickListener() {
		@Override
		public boolean onLongClick(View v)
		{
			/**
			 * Begin periodical action
			 */
			delayOfNextAction = INIT_DEALY;
			pressing = true;
			postNextAction();
			// :~)
			return true;
		}
	};

	/**
	 * Stops the periodical action.
	 */
	private View.OnTouchListener touchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event)
		{
			if (!pressing || event.getAction() != MotionEvent.ACTION_UP) {
				/**
				 * Perform a action while user press the button
				 */
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					pressingListener.stillPress();
				}
				// :~)

				return false;
			}

			pressing = false;
			return true;
		}
	};

	private Runnable stillPressRunnable = new Runnable() {
		@Override
		public void run()
		{
			if (!pressing) {
				return;
			}

			pressingListener.stillPress();
			postNextAction();
		}
	};

	LongPressAction(Handler newHandler, PressingActionListener newListener)
	{
		handler = newHandler;
		pressingListener = newListener;
	}

	/**
	 * Gets the listener of long click.<p>
	 */
	public View.OnLongClickListener getLongClickListener()
	{
		return longClickListener;
	}

	/**
	 * Gets the listener of touch.<p>
	 */
	public View.OnTouchListener getTouchListener()
	{
		return touchListener;
	}

	private void postNextAction()
	{
		handler.postDelayed(stillPressRunnable, delayOfNextAction);

		if (delayOfNextAction > MIN_DEALY) {
			delayOfNextAction -= delayOfNextAction >> 2;
		}
	}
}
