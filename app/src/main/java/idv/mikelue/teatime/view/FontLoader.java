package idv.mikelue.teatime.view;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;

/**
 * This loader provides better performan while need {@link} global singleton
 */
public class FontLoader {
	private static FontLoader globalFontLoader = null;
	/**
	 * Gets the font loader. If the loader hasn't been initialized, this method uses
	 * {@link Context#getApplicationContext} to construct the fonts.<p>
	 *
	 * This method acts as singleton pattern.<p>
	 */
	public static FontLoader getLoader(Context context)
	{
		if (globalFontLoader == null) {
			globalFontLoader = new FontLoader(context.getApplicationContext().getAssets());
		}

		return globalFontLoader;
	}

	public static void releaseLoader()
	{
		globalFontLoader = null;
	}

	private final Typeface fontForDigitalNumber;

	private FontLoader(AssetManager assetMgr)
	{
		fontForDigitalNumber = Typeface.createFromAsset(assetMgr, "fonts/LcdBold.ttf");
	}

	/**
	 * Gets the font used to display digital numbers.<p>
	 *
	 * @return The font used to display digital numbers
	 */
	public Typeface getFontForDigitalNumber()
	{
		return fontForDigitalNumber;
	}
}
