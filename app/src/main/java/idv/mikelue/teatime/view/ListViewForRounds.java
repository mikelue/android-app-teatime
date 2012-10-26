package idv.mikelue.teatime.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View.MeasureSpec;
import android.widget.ListView;

import idv.mikelue.teatime.R;

/**
 * This view shows list of rounds of a session and gives the scrollable
 * view.<p>
 *
 * With {@link RoundCursorAdapter#RoundActionListener RoundActionListener},
 * client can handle events of data modification without carrying out details of UI components.<p>
 *
 * @see #setActionListener
 */
public class ListViewForRounds extends ListView {
	private final static String TAG = ListView.class.getSimpleName();

	private int sizeOfBottomBar = -1;
	public ListViewForRounds(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		sizeOfBottomBar = (int)TypedValue.applyDimension(
			TypedValue.COMPLEX_UNIT_DIP, CustomizedAttr.getBottomBarHeight(attrs),
			getContext().getResources().getDisplayMetrics()
		);
	}

	/**
	 * The height of this view rules out the height of bottom bar of
	 * activity.<p>
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(
			widthMeasureSpec,
			MeasureSpec.makeMeasureSpec(
				MeasureSpec.getSize(heightMeasureSpec) - sizeOfBottomBar,
				MeasureSpec.getMode(heightMeasureSpec)
			)
		);
	}
}
