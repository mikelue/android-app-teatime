package idv.mikelue.teatime.view;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import idv.mikelue.teatime.R;
import idv.mikelue.teatime.model.RoundInfo;
import static idv.mikelue.teatime.util.TextFormatter.formatStateOfSession;

/**
 * This view provides methods to show alarm data in its layout of item.<p>
 *
 * With {@link CheckAlarmCallback}, its implementation is used to notify the
 * outside controller that user has check the "OK" button of alarming item.<p>
 */
public class AlarmSection extends LinearLayout {
	/**
	 * Defines the callback while a alarming is checked by user.<p>
	 */
	public interface CheckAlarmCallback {
		/**
		 * This method is called by {@link AlarmSection} while an alarming is
		 * checked by user.<p>
		 *
		 * @param alarmingRound The alarming which is checked
		 */
		public void checkAlarm(RoundInfo alarmRound);
	}

	private final static String TAG = AlarmSection.class.getSimpleName();

	private int animDuration = -1;

	private int colorLoaderId;

	private Map<Integer, RoundInfo> alarmingRounds = new HashMap<Integer, RoundInfo>(4);
	private Map<Integer, Animation> roundAnimation = new HashMap<Integer, Animation>(4);

	private CheckAlarmCallback alarmCallback = null;
	private OnClickListener clickOkListener = new OnClickListener() {
		@Override
		public void onClick(View v)
		{
			int roundId = getRoundIdFromView(v);
			RoundInfo round = alarmingRounds.get(roundId);

			removeAlarmView(roundId, (View)v.getParent());

			if (alarmCallback != null) {
				alarmCallback.checkAlarm(round);
			}
		}
	};

	public AlarmSection(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		colorLoaderId = context.hashCode();
	}

	/**
	 * Sets the seconds of alarming, which affect the time of animation.<p>
	 *
	 * @param newSeconds The seconds of alarming
	 */
	public void setAlarmingSeconds(int newSeconds)
	{
		// Spin one turn at least
		animDuration = newSeconds * 1000;
	}

	/**
	 * Adds an alarming round to this section.<p>
	 *
	 * @param newAlarmingRound The round which is ended
	 */
	public void addAlarmRound(RoundInfo newAlarmingRound)
	{
		alarmingRounds.put(newAlarmingRound.getId(), newAlarmingRound);

		if (getVisibility() == View.GONE) {
			setVisibility(View.VISIBLE);
		}

		/**
		 * Setup content of item of alarming
		 */
		LayoutInflater inflater = LayoutInflater.from(getContext());
		ViewGroup itemView = (ViewGroup)inflater.inflate(R.layout.alarm_item_layout, this, false);

		ImageView iconView = (ImageView)itemView.findViewById(R.id.session_icon);
		iconView.setBackgroundDrawable(ColorLoader.getBackground(colorLoaderId, newAlarmingRound.getIconType()));

		TextView viewOfSessionName = (TextView)itemView.findViewById(R.id.text_session_name);
		viewOfSessionName.setText(newAlarmingRound.getName());

		TextView viewOfSessionState = (TextView)itemView.findViewById(R.id.text_session_state);
		viewOfSessionState.setText(formatStateOfSession(
			newAlarmingRound.getRoundSequence(),
			newAlarmingRound.getNumberOfRounds()
		));
		viewOfSessionState.setTextColor(ColorLoader.getColor(colorLoaderId, newAlarmingRound.getIconType()));
		// :~)

		/**
		 * Setup the action of "OK" button
		 */
		itemView.findViewById(R.id.button_ok).setOnClickListener(clickOkListener);
		setRoundIdToView(itemView.findViewById(R.id.button_ok), newAlarmingRound.getId());
		// :~)

		addView(itemView);

		/**
		 * Setup the animation of alarming
		 */
		Animation animation = buildAnimation();
		roundAnimation.put(newAlarmingRound.getId(), animation);
		iconView.startAnimation(animation);
		// :~)
	}

	/**
	 * Cleans all of the diaplying items in this section.<p>
	 */
	public void clean()
	{
		for (int i = 0; i < getChildCount(); i++) {
			ViewGroup item = (ViewGroup)getChildAt(i);
			int roundId = (Integer)getRoundIdFromView(item.findViewById(R.id.button_ok));

			removeAlarmView(roundId, item);
		}
	}

	/**
	 * Sets the callback for finished alarming.<p>
	 *
	 * @param newAlarmCallback The implementation of alarming callback
	 */
	public void setCheckAlarmCallback(CheckAlarmCallback newAlarmCallback)
	{
		alarmCallback = newAlarmCallback;
	}

	/**
	 * Checks whether there is any alarming round in this section.<p>
	 *
	 * @return true if there is alaming round
	 */
	public boolean hasAlarmingRound()
	{
		return alarmingRounds.size() > 0;
	}

	private void removeAlarmView(int roundId, View view)
	{
		/**
		 * Remove the data corresponding to rounds
		 */
		RoundInfo round = alarmingRounds.remove(roundId);
		Animation animation = roundAnimation.remove(roundId);
		if (!animation.hasEnded()) {
			animation.cancel();
		}
		// :~)

		/**
		 * Hidden this section if there is no alrarming round
		 */
		if (alarmingRounds.size() == 0) {
			setVisibility(View.GONE);
		}

		removeView(view);
		// :~)
	}

	private Animation buildAnimation()
	{
		Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.icon_of_alarm);
		animation.restrictDuration(animDuration);

		return animation;
	}

	private void setRoundIdToView(View view, int roundId)
	{
		view.setTag(new Integer(roundId));
	}
	private Integer getRoundIdFromView(View view)
	{
		return (Integer)view.getTag();
	}
}
