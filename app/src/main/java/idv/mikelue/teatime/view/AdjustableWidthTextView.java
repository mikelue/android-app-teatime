package idv.mikelue.teatime.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;
import android.view.ViewGroup;
import android.util.TypedValue;
import android.util.DisplayMetrics;

/**
 * This text view provides {@link #adjustWidth method} to adjust its displaying
 * of content by length of text.<p>
 */
public class AdjustableWidthTextView extends DigitalNumberTextView {
	/**
	 * The width to which detection of content applied
	 */
	private final static int WIDTH_SINGLE_CHAR = 15;
	private final static int WIDTH_DOUBLE_CHARS = 30;
	// :~)

	private final static String TAG = AdjustableWidthTextView.class.getSimpleName();

	public AdjustableWidthTextView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	/**
	 * Adjust the width to display the sequence by length of content.<p>
	 *
	 * @param number The number to be displayed
	 */
	public void adjustWidth(int number)
	{
		ViewGroup.LayoutParams layoutParams = getLayoutParams();

		DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
		if (number >= 10) {
			layoutParams.width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, WIDTH_DOUBLE_CHARS, metrics);
		} else {
			layoutParams.width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, WIDTH_SINGLE_CHAR, metrics);
		}

		setLayoutParams(layoutParams);
	}
}
