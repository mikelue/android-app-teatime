package idv.mikelue.teatime.view;

import android.util.AttributeSet;

/**
 * This utility provides customized attributes of XML in this project.<p>
 */
public class CustomizedAttr {
	private CustomizedAttr() {}

	/**
	 * The namespace of customized attribute.({@value}).<p>
	 */
	public final static String XML_NAMESPACE = "http://andoird.mikelue.idv/teatime";

	/**
	 * Gets the height(set by XML file of layout) of bottom bar.<p>
	 */
	public static int getBottomBarHeight(AttributeSet attrs)
	{
		return attrs.getAttributeIntValue(XML_NAMESPACE, "bottombar_height", -1);
	}
}
