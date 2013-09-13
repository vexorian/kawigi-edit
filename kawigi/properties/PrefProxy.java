package kawigi.properties;
import java.awt.*;
import java.io.*;

/**
 *	Interface implemented by preference binding classes used by KawigiEdit.
 **/
public interface PrefProxy
{
	/**
	 *	Returns the value of a property as a boolean.
	 **/
	public boolean getBoolean(String property);
	
	/**
	 *	Returns the value of a property as a boolean, setting it to defaultValue
	 *	if it isn't set yet.
	 **/
	public boolean getBoolean(String property, boolean defaultValue);
	
	/**
	 *	Sets a property to a boolean value.
	 **/
	public void setBoolean(String property, boolean value);
	
	/**
	 *	Returns the value of a property as a Font.
	 **/
	public Font getFont(String property);
	
	/**
	 *	Returns the value of a property as a Font, setting it to defaultValue if
	 *	it isn't set yet.
	 **/
	public Font getFont(String property, Font defaultValue);
	
	/**
	 *	Sets a property to a Font value.
	 **/
	public void setFont(String property, Font value);
	
	/**
	 *	Returns the value of a property as a Color.
	 **/
	public Color getColor(String property);
	
	/**
	 *	Returns the value of a property as a Color, setting it to defaultValue
	 *	if it isn't set yet.
	 **/
	public Color getColor(String property, Color defaultValue);
	
	/**
	 *	Sets a property to a Color value.
	 **/
	public void setColor(String property, Color value);
	
	/**
	 *	Returns the value of a property as an integer.
	 **/
	public int getInt(String property);
	
	/**
	 *	Returns the value of a property as an integer, setting it to
	 *	defaultValue if it isn't set yet.
	 **/
	public int getInt(String property, int defaultValue);
	
	/**
	 *	Sets a property to an integer value.
	 **/
	public void setInt(String property, int value);
	
	/**
	 *	Returns the value of a property as a double.
	 **/
	public double getDouble(String property);
	
	/**
	 *	Returns the value of a property as a double, setting it to defaultValue
	 *	if it isn't set yet.
	 **/
	public double getDouble(String property, double defaultValue);
	
	/**
	 *	Sets a property to a double-precision value.
	 **/
	public void setDouble(String property, double value);
	
	/**
	 *	Returns the value of a property as a String.
	 **/
	public String getProperty(String property);
	
	/**
	 *	Returns the value of a property as a String, setting it to defaultValue
	 *	if it isn't set yet.
	 **/
	public String getProperty(String property, String defaultValue);
	
	/**
	 *	Sets a property to a String value.
	 **/
	public void setProperty(String property, String value);
	
	/**
	 *	Gets the directory that should be used locally by KawigiEdit.
	 **/
	public File getWorkingDirectory();
	
	/**
	 *	Sets the directory that should be used locally by KawigiEdit.
	 **/
	public void setWorkingDirectory(File f);
	
	/**
	 *	Saves the values stored as preferences.
	 **/
	public void commit();
}