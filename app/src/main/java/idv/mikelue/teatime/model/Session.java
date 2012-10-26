package idv.mikelue.teatime.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.database.Cursor;

import idv.mikelue.teatime.R;

/**
 * Represents data of session.<p>
 */
public class Session {
	/**
	 * Defines the value of icon type(the color of icon for session).<p>
	 *
	 * Check color.xml for RGB value of defined colors.<p>
	 */
	public enum IconType {
		Type1(1),
		Type2(2),
		Type3(3),
		Type4(4),
		Type5(5),
		Type6(6),
		Type7(7),
		Type8(8);

		private final static Map<Integer, IconType> MAP_DATABASE_VALUE__ICON_TYPE;
		static {
			/**
			 * Initiate the map used to mapping values for displaying and persistence in database
			 */
			Map<Integer, IconType> tempMapForDatabaseValue = new HashMap<Integer, IconType>(IconType.values().length);

			for (IconType iconType: IconType.values()) {
				tempMapForDatabaseValue.put(iconType.getDatabaseValue(), iconType);
			}

			MAP_DATABASE_VALUE__ICON_TYPE = Collections.unmodifiableMap(tempMapForDatabaseValue);
			// :~)
		}

		/**
		 * Gets the value of enum by value of database.<p>
		 *
		 * @param databaseValue The value saved in database
		 *
		 * @see #valueOfColorId
		 */
		public static IconType valueOfDatabaseValue(int databaseValue)
		{
			return MAP_DATABASE_VALUE__ICON_TYPE.get(databaseValue);
		}

		private int databaseValue;
		IconType(int newDatabaseValue)
		{
			databaseValue = newDatabaseValue;
		}

		/**
		 * Gets the value used to saved into database.<p>
		 *
		 * @return The value in database
		 *
		 * @see #getColorId
		 */
		public int getDatabaseValue()
		{
			return databaseValue;
		}
	}

	private int id = -1;
	private String name = null;
	private IconType iconType = null;
	private int numberOfRounds = -1;

	/**
	 * Constructs a session for modification of existing row in database.<p>
	 *
	 * @param cursorForSession The cursor containing sole row to data of session
	 */
	public Session(Cursor cursorForSession)
	{
		cursorForSession.moveToFirst();

		id = cursorForSession.getInt(cursorForSession.getColumnIndex("ss_id"));
		setName(cursorForSession.getString(cursorForSession.getColumnIndex("ss_name")));
		setIconType(IconType.valueOfDatabaseValue(
			cursorForSession.getInt(cursorForSession.getColumnIndex("ss_icon_type"))
		));
	}

	/**
	 * Constructs a session by its id.<p>
	 *
	 * @param newId The id of session
	 */
	public Session(int newId)
	{
		id = newId;
	}

	/**
	 * Constructs a session by its name and type
	 *
	 * @param newName The name of session
	 * @param newIconType The icon type of session
	 */
	public Session(String newName, IconType newIconType)
	{
		setName(newName);
		setIconType(newIconType);
	}

	/**
	 * Gets the id of session.<p>
	 *
	 * @return The id of session
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * Gets name of session.<p>
	 *
	 * @return name of session
	 *
	 * @see #setName
	 */
	public String getName()
	{
		return name;
	}
	/**
	 * Sets name of session.<p>
	 *
	 * @param newName new name of session
	 *
	 * @see #getName
	 */
	public void setName(String newName)
	{
		name = newName.trim();
	}

	/**
	 * Gets number of rounds.<p>
	 *
	 * @return number of rounds
	 *
	 * @see #setNumberOfRounds
	 */
	public int getNumberOfRounds()
	{
		return numberOfRounds;
	}
	/**
	 * Sets number of rounds.<p>
	 *
	 * @param newNumberOfRounds new number of rounds
	 *
	 * @see #getNumberOfRounds
	 */
	public void setNumberOfRounds(int newNumberOfRounds)
	{
		numberOfRounds = newNumberOfRounds;
	}

	/**
	 * Gets type of icon for this session.<p>
	 *
	 * @return type of icon for this session
	 *
	 * @see #setIconType
	 */
	public IconType getIconType()
	{
		return iconType;
	}
	/**
	 * Sets type of icon for this session.<p>
	 *
	 * @param newIconType new type of icon for this session
	 *
	 * @see #getIconType
	 */
	public void setIconType(IconType newIconType)
	{
		iconType = newIconType;
	}
}
