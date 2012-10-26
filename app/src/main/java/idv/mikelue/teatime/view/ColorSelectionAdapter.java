package idv.mikelue.teatime.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.graphics.drawable.Drawable;

import idv.mikelue.teatime.R;
import idv.mikelue.teatime.model.Session.IconType;

/**
 * This adapter gives the selection of colors as session's theme color.<p>
 *
 * With {@link ColorSelectionAdapter#IconActionListener IconActionListener},
 * client can receive the color that user chose by value of {@link IconType}.<p>
 *
 * @see #setIconActionListener
 */
public class ColorSelectionAdapter extends ArrayAdapter<Drawable> {
	/**
	 * Defines the callback while user click a color of icon.<p.
	 */
	public interface IconActionListener {
		/**
		 * Called while user click a icon.<p>
		 *
		 * @param chosenIconType The type of icon that user clicked
		 */
		public void chooseIconColor(IconType chosenIconType);
	}

	private IconActionListener actionListener;
	private View.OnClickListener iconClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View viewOfIcon)
		{
			if (actionListener == null) {
				return;
			}

			actionListener.chooseIconColor((IconType)viewOfIcon.getTag());
		}
	};

	public ColorSelectionAdapter(Context context)
	{
		super(
			context, R.layout.color_item, android.R.id.empty,
			ColorLoader.loadFreshBackgrounds(context)
		);
	}

	/**
	 * Sets the action listener of icons.<p>
	 *
	 * @param newListener The listener
	 */
	public void setIconActionListener(IconActionListener newListener)
	{
		actionListener = newListener;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewGroup itemView = (ViewGroup)super.getView(position, convertView, parent);

		View iconView = itemView.findViewById(R.id.session_icon);
		iconView.setBackgroundDrawable(
			getItem(position)
		);
		iconView.setOnClickListener(iconClickListener);
		iconView.setTag(
			IconType.valueOfDatabaseValue(position + 1)
		);

		return itemView;
	}
}
