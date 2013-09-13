package kawigi.properties;
import java.io.*;
import java.util.*;

/**
 *	Preferences implementation for standalone, which uses Java's Properties
 *	class to store, load, and save preferences
 *	
 *	See the notes in the AbstractPrefs class for sematics of how this works.
 **/
public class StandAlonePrefs extends AbstractPrefs
{
	/**
	 *	File in which these preferences are stored.
	 **/
	private File prefsFile;
	/**
	 *	Java Properties object that stores preference values.
	 **/
	private Properties props;
	
	/**
	 *	Constructs a new StandAlonePrefs object which reads preferences from
	 *	the given file if possible.
	 **/
	public StandAlonePrefs(File prefsFile)
	{
		this.prefsFile = prefsFile;
		props = new Properties();
		try
		{
			FileInputStream in = new FileInputStream(prefsFile);
			props.loadFromXML(in);
			in.close();
		}
		catch (Exception ex)
		{
		}
	}
	
	/**
	 *	Returns the string value of the given property.
	 **/
	public String getProperty(String property)
	{
		return props.getProperty(property);
	}
	
	/**
	 *	Sets a property to the given string value.
	 **/
	public void setProperty(String property, String value)
	{
		props.setProperty(property, value);
	}
	
	/**
	 *	Saves the properties in Java's XML Properties format.
	 **/
	public void commit()
	{
		try
		{
			FileOutputStream out = new FileOutputStream(prefsFile);
			props.storeToXML(out, "KawigiEdit preferences file");
			out.close();
		}
		catch (Exception ex)
		{
		}
	}
}
