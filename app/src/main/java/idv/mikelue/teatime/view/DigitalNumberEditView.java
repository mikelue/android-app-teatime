package idv.mikelue.teatime.view;

import java.util.regex.Pattern;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Same as {@link DigitalNumberTextView}, but extends from {@link EditText}.<p>
 */
public class DigitalNumberEditView extends EditText {
	private final static String TAG = DigitalNumberEditView.class.getSimpleName();

	public DigitalNumberEditView(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		setTypeface(FontLoader.getLoader(context).getFontForDigitalNumber());
	}

	private final static Pattern formatCheck = Pattern.compile("\\d{2}");

	/**
	 * Fixes the text to provide smart modification of time value.<p>
	 */
	@Override
	protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter)
	{
		if (formatCheck.matcher(text).matches()) {
			return;
		}

		StringBuilder textBuilder = new StringBuilder(text);
		int newCursorPosition = -1;

		switch (text.length()) {
			case 0:
				textBuilder.append("00");
				newCursorPosition = 0;
				break;
			case 1:
				switch (lengthAfter) {
					/**
					 * Remove one of the two digits of ordinary
					 */
					case 0:
						textBuilder.insert(start, "0");
						newCursorPosition = start;
						break;
					// :~)
					/**
					 * Replace two digits by one digits
					 */
					case 1:
						textBuilder.append("0");
						newCursorPosition = 1;
						break;
					// :~)
				}
				break;
			case 3:
				int indexOfAffectedChar = start < 2 ? start + 1 : 2;

				/**
				 * Remove the additional character
				 */
				textBuilder.deleteCharAt(indexOfAffectedChar);
				newCursorPosition = indexOfAffectedChar;
				// :~)
				break;
		}

		setText(textBuilder.toString());
		setSelection(newCursorPosition);
	}
}
