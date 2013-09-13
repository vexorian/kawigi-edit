package kawigi.properties;
import java.awt.*;
import java.io.File;

/**
 *	This is the superclass of PrefProxy implementations used in KawigiEdit.
 *
 *	The purpose here is to reduce duplicated code in converting the string
 *	representations of property values into other types.  Then, other actually
 *	distinct implementations only really need to implement getProperty,
 *	setProperty and commit.
 *
 *	Note the difference in semantics between this implementation and the way
 *	Java's and TopCoder's properties work and the way I want KawigiEdit's
 *	properties to work - in other implementations, the default value is just
 *	returned if there is no set property.  Here I'm setting it if it's not set.
 **/
public abstract class AbstractPrefs implements PrefProxy
{
	/**
	 *	AbstractPrefs constructor.
	 **/
	public AbstractPrefs()
	{
	}

	/**
	 *	Gets the given property as a boolean value.
	 **/
	public boolean getBoolean(String property)
	{
		if (getProperty(property) == null)
			return false;
		return Boolean.valueOf(getProperty(property));
	}

	/**
	 *	Gets the given property as a boolean value, setting it to the
	 *	defaultValue if it isn't set yet.
	 **/
	public boolean getBoolean(String property, boolean defaultValue)
	{
		if (getProperty(property) == null)
			setBoolean(property, defaultValue);
		return getBoolean(property);
	}

	/**
	 *	Sets a given property to a boolean value.
	 **/
	public void setBoolean(String property, boolean value)
	{
		setProperty(property, Boolean.toString(value));
	}

	/**
	 *	Gets the given property as a Font object.
	 *
	 *	Note - "property" refers to the property that gives the font face.
	 *	"property.size" is the property that gives the font size.
	 *	"property.bold" and "property.italic" are additional style
	 *	specifications.  I don't think there is a way through the UI to set
	 *	these, but one could technically set some font property to have these
	 *	styles by modifying the config files directly.
	 **/
	public Font getFont(String property)
	{
		String fontFace = getProperty(property);
		if (fontFace == null)
			return null;
		int fontSize;
		if (getProperty(property + ".size") == null)
			fontSize = 12;
		else
			fontSize = getInt(property + ".size");
		int style = 0;
		if (getBoolean(property + ".bold"))
			style |= Font.BOLD;
		if (getBoolean(property + ".italic"))
			style |= Font.ITALIC;
		return new Font(fontFace, style, fontSize);
	}

	/**
	 *	Returns the given property as a Font object, setting the property to the
	 *	value of the default value if it isn't set.
	 **/
	public Font getFont(String property, Font defaultValue)
	{
		String fontFace = getProperty(property, defaultValue.getFontName());
		int fontSize = getInt(property + ".size", defaultValue.getSize());
		int style = 0;
		if (getBoolean(property + ".bold", defaultValue.isBold()))
			style |= Font.BOLD;
		if (getBoolean(property + ".italic", defaultValue.isItalic()))
			style |= Font.ITALIC;
		return new Font(fontFace, style, fontSize);
	}

	/**
	 *	Sets the given property to the given Font value.
	 **/
	public void setFont(String property, Font value)
	{
		setProperty(property, value.getFontName());
		setInt(property + ".size", value.getSize());
		setBoolean(property + ".bold", value.isBold());
		setBoolean(property + ".italic", value.isItalic());
	}

	/**
	 *	Gets the given property as a color.
	 **/
	public Color getColor(String property)
	{
		if (getProperty(property) == null)
			return null;
		else
			return new Color(getInt(property));
	}

	/**
	 *	Gets the given property as a color, setting it to defaultValue if it
	 *	isn't set yet.
	 **/
	public Color getColor(String property, Color defaultValue)
	{
		if (getProperty(property) == null)
			setColor(property, defaultValue);
		return getColor(property);
	}

	/**
	 *	Sets the given property to the given Color value.
	 **/
	public void setColor(String property, Color value)
	{
		setInt(property, value.getRGB());
	}

	/**
	 *	Gets the given property as an int.
	 **/
	public int getInt(String property)
	{
		if (getProperty(property) == null)
			return 0;
		return Integer.parseInt(getProperty(property));
	}

	/**
	 *	Gets the given property as an int, setting the property to defaultValue
	 *	if it hasn't been set yet.
	 **/
	public int getInt(String property, int defaultValue)
	{
		if (getProperty(property) == null)
			setInt(property, defaultValue);
		return getInt(property);
	}

	/**
	 *	Sets the given property to the given integer value.
	 **/
	public void setInt(String property, int value)
	{
		setProperty(property, Integer.toString(value));
	}

	/**
	 *	Gets the given property as a double.
	 **/
	public double getDouble(String property)
	{
		if (getProperty(property) == null)
			return 0;
		return Double.parseDouble(property);
	}

	/**
	 *	Gets the given property as a double, setting it to defaultValue if it
	 *	hasn't been set yet.
	 **/
	public double getDouble(String property, double defaultValue)
	{
		if (getProperty(property) == null)
			setDouble(property, defaultValue);
		return getDouble(property);
	}

	/**
	 *	Sets the given property to the given double value.
	 **/
	public void setDouble(String property, double value)
	{
		setProperty(property, Double.toString(value));
	}

	/**
	 *	Gets the given property, setting it to defaultValue if it isn't set yet.
	 **/
	public String getProperty(String property, String defaultValue)
	{
		if (getProperty(property) == null)
			setProperty(property, defaultValue);
		return getProperty(property);
	}

	/**
	 *	Returns the value of the "kawigi.localpath" property as a File.
	 *
	 *	If there is no value for "kawigi.localpath", it will set it to the
	 *	default "testprograms" directory.  If the directory doesn't exist, it
	 *	will be created.
	 **/
	public File getWorkingDirectory()
	{
		if (getProperty("kawigi.localpath") == null)
			setWorkingDirectory(new File("testprograms"));

		File cwd = new File(getProperty("kawigi.localpath"));
		if (!cwd.exists())
			cwd.mkdir();
		return cwd;
	}

	/**
	 *	Sets the "kawigi.localpath" property to the path of the given directory.
	 **/
	public void setWorkingDirectory(File f)
	{
		setProperty("kawigi.localpath", f.getPath());
	}
}
