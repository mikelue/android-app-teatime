package idv.mikelue.teatime.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import idv.mikelue.teatime.R;
import idv.mikelue.teatime.model.DetailTickingRound;
import static idv.mikelue.teatime.util.TextFormatter.formatStateOfSession;
import static idv.mikelue.teatime.util.TextFormatter.formatTime;

/**
 * This view provides methods to show ticking data in its layout of item.<p>
 */
public class TickingSection extends LinearLayout {
	/**
	 * This callback is used by client to handle the cancel of
	 * listener.<p>
	 */
	public interface CancelTickingListener {
		/**
		 * Cancels the ticking.<p>
		 *
		 * @param roundId The id of round to be canceled
		 */
		public void cancelTicking(int roundId);
	}

	private final static String TAG = TickingSection.class.getSimpleName();
	private TickingRoundPool roundPool = new TickingRoundPool();
	private CancelTickingListener cancelTickingListener = null;

	private int colorLoaderId;

	/**
	 * Delegate the clicking of cancel button to listener set from outside.<p>
	 */
	private OnClickListener clickCancelListener = new OnClickListener() {
		@Override
		public void onClick(View v)
		{
			int roundId = getTagOfView(v);

			removeView((View)v.getParent(), new DetailTickingRound(roundId, 0));

			if (cancelTickingListener == null) {
				return;
			}

			cancelTickingListener.cancelTicking(roundId);
		}
	};
	// :~)

	public TickingSection(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		colorLoaderId = context.hashCode();
	}

	/**
	 * Shows the round of ticking in this section.<p>
	 *
	 * If the round(identified by {@link TickingRound#getId}) has been shown in
	 * this section, the content of item would be refreshed.<p>
	 *
	 * If the {@link TickingRound#isEnded()} is true, the displaying of item would
	 * be removed.<p>
	 *
	 * In order to improve performance, the {@link DetailTickingRound} is
	 * shared with caller.<p>
	 *
	 * @param tickingRound The round of ticking
	 */
	public void showTickingRound(DetailTickingRound tickingRound)
	{
		TextView viewOfTickingTime = findOrInflateViewOfTime(tickingRound);
		View itemOfTickingRound = (View)viewOfTickingTime.getParent();

		/**
		 * Remove the ended round showed in item and hide this section if there
		 * is no ticking round
		 */
		if (tickingRound.isEnded()) {
			removeView(itemOfTickingRound, tickingRound);
		/**
		 * Refresh the displaying of time
		 */
		} else {
			viewOfTickingTime.setText(formatTime(tickingRound.getRemainSeconds()));
		}
		// :~)

		/**
		 * Show this section for the information of first ticking round
		 */
		if (roundPool.getNumberOfRounds() > 0 && getVisibility() == View.GONE) {
			setVisibility(View.VISIBLE);
		}
		// :~)
	}

	/**
	 * Removes all of the tiem showed for ticking and hide this section.<p>
	 */
	public void clean()
	{
		for (DetailTickingRound round: roundPool.getTickingRounds()) {
			DetailTickingRound newRound = new DetailTickingRound(round);
			newRound.setRemainSeconds(0);
			showTickingRound(newRound);
		}
	}

	@Override
	public void setVisibility(int visibility)
	{
		super.setVisibility(visibility);

		getViewOfHeader().setVisibility(visibility); // Setup the visibility of header
	}

	/**
	 * Sets the listener to handle cancel of ticking round.<p>
	 *
	 * @param newListener The listener implemented by client
	 */
	public void setCancelTickingListener(CancelTickingListener newListener)
	{
		cancelTickingListener = newListener;
	}

	private void removeView(View itemOfTickingRound, DetailTickingRound tickingRound)
	{
		/**
		 * Remove the ended round showed in item and hide this section if there
		 * is no ticking round
		 */
		removeView(itemOfTickingRound);
		roundPool.removeTickingRound(tickingRound);

		if (roundPool.getNumberOfRounds() == 0 && getVisibility() == View.VISIBLE) {
			setVisibility(View.GONE);
			return;
		}
		// :~)
	}

	private View viewOfHeader;
	private View getViewOfHeader()
	{
		if (viewOfHeader == null) {
			viewOfHeader = findViewById(R.id.section_ticking_header);
		}

		return viewOfHeader;
	}

	private TextView findOrInflateViewOfTime(DetailTickingRound tickingRound)
	{
		/**
		 * Find the item has been displayed in this section
		 */
		if (roundPool.hasRound(tickingRound)) {
			return roundPool.getTextViewOfRound(tickingRound);
		}
		// :~)

		/**
		 * Inflate a new view of item for ticking
		 */
		LayoutInflater inflater = LayoutInflater.from(getContext());
		ViewGroup itemView = (ViewGroup)inflater.inflate(R.layout.ticking_item_layout, this, false);

		TextView viewOfSessionName = (TextView)itemView.findViewById(R.id.text_session_name);
		viewOfSessionName.setText(tickingRound.getName());

		TextView viewOfTickingTime = (TextView)itemView.findViewById(R.id.ticking_time);
		viewOfTickingTime.setTextColor(ColorLoader.getColor(colorLoaderId, tickingRound.getIconType()));

		TextView viewOfRoundInfo = (TextView)itemView.findViewById(R.id.text_session_state);
		viewOfRoundInfo.setText(formatStateOfSession(tickingRound.getRoundSequence(), tickingRound.getNumberOfRounds()));
		viewOfRoundInfo.setTextColor(ColorLoader.getColor(colorLoaderId, tickingRound.getIconType()));

		View cancelButton = itemView.findViewById(R.id.button_cancel);
		cancelButton.setOnClickListener(clickCancelListener);
		setTagOfView(cancelButton, tickingRound.getId());

		roundPool.putTickingRound(tickingRound, viewOfTickingTime); // Add the round and view of time to pool

		addView(itemView, roundPool.getIndexOfRoundByRemainTime(tickingRound) + 1);
		// :~)

		return viewOfTickingTime;
	}

	private void setTagOfView(View itemView, int roundId)
	{
		itemView.setTag(new Integer(roundId));
	}
	private int getTagOfView(View itemView)
	{
		return (Integer)itemView.getTag();
	}
}

/**
 * This list provides the sequence of ticking rounds which sorts by
 * their {@link DetailTickingRound#getRemainSeconds()}.<p>
 */
class TickingRoundPool {
	private final static int INIT_CAPACITY = 3;

	private List<DetailTickingRound> sortedRounds = new ArrayList<DetailTickingRound>(INIT_CAPACITY);
	private Map<Integer, TextView> addedRoundIdAndTimeView = new HashMap<Integer, TextView>(INIT_CAPACITY);

	private Comparator<DetailTickingRound> roundComparator = new TimeComparator();

	TickingRoundPool() {}

	/**
	 * Adds a ticking round into this pool. This method also cache the text
	 * view of time to be refreshed in future.<p>
	 *
	 * This method do nothing if the round has been added.<p>
	 *
	 * @param tickingRound The round to be added
	 * @param viewOfTime The view to display the current time of round
	 *
	 * @see #removeTickingRound
	 */
	void putTickingRound(DetailTickingRound tickingRound, TextView viewOfTime)
	{
		if (hasRound(tickingRound)) {
			return;
		}

		addedRoundIdAndTimeView.put(tickingRound.getId(), viewOfTime);

		sortedRounds.add(tickingRound);
		Collections.sort(sortedRounds, roundComparator);
	}
	/**
	 * Removes a ticking round from this list.<p>
	 *
	 * @param tickingRound The round to be removed
	 *
	 * @see #putTickingRound
	 */
	void removeTickingRound(DetailTickingRound tickingRound)
	{
		addedRoundIdAndTimeView.remove(tickingRound.getId());
		sortedRounds.remove(tickingRound);
	}

	boolean hasRound(DetailTickingRound tickingRound)
	{
		return addedRoundIdAndTimeView.containsKey(tickingRound.getId());
	}

	/**
	 * Gets the view of time for a round.<p>
	 *
	 * @param tickingRound The round is going to be refreshed
	 */
	TextView getTextViewOfRound(DetailTickingRound tickingRound)
	{
		return addedRoundIdAndTimeView.get(tickingRound.getId());
	}

	List<DetailTickingRound> getTickingRounds()
	{
		return new ArrayList(sortedRounds);
	}

	/**
	 * Gets the index of round, which is sorted by remain time.<p>
	 *
	 * @param targetRound The round to find the position sorted by remain time
	 *
	 * @return The sequence of round in list
	 */
	int getIndexOfRoundByRemainTime(DetailTickingRound targetRound)
	{
		return sortedRounds.indexOf(targetRound);
	}
	/**
	 * Gets the number of rounds in list.<p>
	 *
	 * @return The number of rounds
	 */
	int getNumberOfRounds()
	{
		return sortedRounds.size();
	}
}

/**
 * Compares the rounds by their remain seconds.<p>
 */
class TimeComparator implements Comparator<DetailTickingRound> {
	TimeComparator() {}

	@Override
	public int compare(DetailTickingRound o1, DetailTickingRound o2)
	{
		if (o1.getRemainSeconds() > o2.getRemainSeconds()) {
			return 1;
		}
		if (o1.getRemainSeconds() < o2.getRemainSeconds()) {
			return -1;
		}

		return 0;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null) { return false; }
		if (obj == this) { return true; }
		if (obj.getClass() != getClass()) {
		  return false;
		}

		return true;
	}
}
