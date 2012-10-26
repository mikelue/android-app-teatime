package idv.mikelue.teatime.view;

import java.util.Map;
import java.util.HashMap;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;

import idv.mikelue.teatime.model.Session.IconType;
import idv.mikelue.teatime.R;

/**
 * This loader provides better performance while views need {@link Drawable} in
 * small, repetitive setting of backgound or color.<p>
 *
 * This loader pre-loads all of the pre-defined colors/{@link Drawable}s,
 * which are used for view as theme of session({@link IconType}).
 *
 * However, in case of memory leak, the activities are responsible for
 * initializing({@link #initLoader}) and releasing({@link #releaseLoader}) resource in this loader.<p>
 *
 * In other classes, they use {@link #getColor}/{@link #getBackgound} to setup the theme of UI.
 */
public class ColorLoader {
	private final static String TAG = ColorLoader.class.getSimpleName();

	private final static Map<Integer, ColorLoader> loaderPool = new HashMap<Integer, ColorLoader>(2);

	/**
	 * Initiates a loader with customized id of loader.  Hash code of context
	 * seems a proper choose.<p>
	 *
	 * @param context The context used to load resources
	 * @param loaderId The id of loader
	 */
	public static void initLoader(Context context, int loaderId)
	{
		if (loaderPool.containsKey(loaderId)) {
			Log.w(TAG, String.format("Loader id [%d] has been initiated", loaderId));
			return;
		}

		loaderPool.put(loaderId, new ColorLoader(context));

		Log.d(TAG, String.format("Initiated loader for id: [%d]", loaderId));
	}

	/**
	 * Releases a loader by its id. <b>This method must be called before the
	 * initiating context finishs {@link Context#onDestroy}</b>.
	 *
	 * @param loaderId The id of loader
	 */
	public static void releaseLoader(int loaderId)
	{
		loaderPool.remove(loaderId);

		Log.d(TAG, String.format("Released loader for id: [%d]", loaderId));
	}

	/**
	 * Gets the color defined by type of icon.<p>
	 *
	 * @param loaderId The id of loader
	 * @param iconType The type of icon
	 *
	 * @return The id of color resource
	 */
	public static int getColor(int loaderId, IconType iconType)
	{
		return loaderPool.get(loaderId).getColorByIconType(iconType);
	}

	/**
	 * Gets the {@link Drawable} used in backgound, defined by type of icon.<p>
	 *
	 * @param loaderId The id of loader
	 * @param iconType The type of icon
	 *
	 * @return The pre-loaded Drawable object
	 */
	public static Drawable getBackground(int loaderId, IconType iconType)
	{
		return loaderPool.get(loaderId).getBackgroundByIconType(iconType);
	}

	/**
	 * Loads the all of defined backgrounds without uses the cache.<p>
	 *
	 * @param context The context used to load resources
	 *
	 * @return All of defined backgrounds
	 */
	public static Drawable[] loadFreshBackgrounds(Context context)
	{
		Resources resources = context.getResources();

		return new Drawable[] {
			resources.getDrawable(R.drawable.icon_background_1),
			resources.getDrawable(R.drawable.icon_background_2),
			resources.getDrawable(R.drawable.icon_background_3),
			resources.getDrawable(R.drawable.icon_background_4),
			resources.getDrawable(R.drawable.icon_background_5),
			resources.getDrawable(R.drawable.icon_background_6),
			resources.getDrawable(R.drawable.icon_background_7),
			resources.getDrawable(R.drawable.icon_background_8)
		};
	}

	private final int[] mappingOfIconType;
	private final Drawable[] mappingOfIconBackground;

	private ColorLoader(Context context)
	{
		Resources resources = context.getResources();

		mappingOfIconType = new int[] {
			resources.getColor(R.color.session_icon_type_1),
			resources.getColor(R.color.session_icon_type_2),
			resources.getColor(R.color.session_icon_type_3),
			resources.getColor(R.color.session_icon_type_4),
			resources.getColor(R.color.session_icon_type_5),
			resources.getColor(R.color.session_icon_type_6),
			resources.getColor(R.color.session_icon_type_7),
			resources.getColor(R.color.session_icon_type_8)
		};
		mappingOfIconBackground = loadFreshBackgrounds(context);
	}

	/**
	 * Gets the color by icon type of session.<p>
	 *
	 * @param iconType The theme of icon
	 */
	private int getColorByIconType(IconType iconType)
	{
		return mappingOfIconType[iconType.getDatabaseValue() - 1];
	}
	/**
	 * Gets the background by icon type of session.<p>
	 *
	 * @param iconType The theme of icon
	 */
	private Drawable getBackgroundByIconType(IconType iconType)
	{
		return mappingOfIconBackground[iconType.getDatabaseValue() - 1];
	}
}
