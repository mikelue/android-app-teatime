package idv.mikelue.teatime.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

public class DigitalNumberTextView extends TextView {
	private final static String TAG = DigitalNumberTextView.class.getSimpleName();

	public DigitalNumberTextView(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		setTypeface(FontLoader.getLoader(context).getFontForDigitalNumber());
	}
}
