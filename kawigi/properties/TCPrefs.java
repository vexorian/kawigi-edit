package kawigi.properties;
import java.io.*;
import com.topcoder.client.contestApplet.common.LocalPreferences;

/**
 *	Preferences implementation for plugin mode, where the preference values are
 *	stored in TopCoder's LocalPreferences.
 *	
 *	These preferences are stored along with settings for the TopCoder contest
 *	applet in a text file called "contestapplet.conf", which by default in
 *	Windows XP is stored at C:\Documents and Settings\<username>\, or in most
 *	non-Microsoft platforms is stored in the home directory.
 *	
 *	See the notes in the AbstractPrefs class for sematics of how this works.
 **/
public class TCPrefs extends AbstractPrefs
{
	/**
	 *	Reference to TopCoder's singleton LocalPreferences instance.
	 **/
	LocalPreferences prefs;
	
	/**
	 *	Constructs a TCPrefs object and gets TopCoder's preferences.
	 **/
	public TCPrefs()
	{
		this.prefs = LocalPreferences.getInstance();
	}
	
	/**
	 *	Gets the value of a property as a String.
	 **/
	public String getProperty(String property)
	{
		return prefs.getProperty(property);
	}
	
	/**
	 *	Sets the value of a property to a String value.
	 **/
	public void setProperty(String property, String value)
	{
		prefs.setProperty(property, value);
	}
	
	/**
	 *	Tells the TopCoder applet to save its preferences.
	 **/
	public void commit()
	{
		try
		{
			prefs.savePreferences();
		}
		catch (IOException ex)
		{
		}
	}
}
