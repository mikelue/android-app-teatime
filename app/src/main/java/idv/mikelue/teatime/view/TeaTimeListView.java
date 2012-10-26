package idv.mikelue.teatime.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import idv.mikelue.teatime.R;

/**
 * This list view contains header for following infomration:
 * <ol>
 * 	<li>The alarm section {@link AlarmSection}</li>
 * 	<li>The ticking section {@link TickingSection}</li>
 * 	<li>The list of sessions</li>
 * </ol>
 */
public class TeaTimeListView extends ListView {
	private final static String TAG = TeaTimeListView.class.getSimpleName();

	public TeaTimeListView(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		setupHeaderView();
	}

	private void setupHeaderView()
	{
		Log.d(TAG, "Setup header of list view in TeaTime");

		setHeaderDividersEnabled(false);

		LayoutInflater layoutInflater = LayoutInflater.from(getContext());

		/**
		 * Add alarm section
		 */
		View alarmSection = layoutInflater.inflate(R.layout.alarm_section, this, false);
		addHeaderView(alarmSection);
		// :~)

		/**
		 * Add ticking section
		 */
		View tickingSection = layoutInflater.inflate(R.layout.ticking_section, this, false);
		addHeaderView(tickingSection);
		// :~)

		/**
		 * Add header for list of sessions
		 */
		addHeaderView(
			layoutInflater.inflate(R.layout.session_list_header, this, false)
		);
		// :~)
	}
}
