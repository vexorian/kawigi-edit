package kawigi.properties;
import kawigi.util.*;
import java.io.*;

/**
 *	This is one of those classes that sits in between standalone mode and plugin
 *	mode.
 **/
public class PrefFactory
{
	/**
	 *	The global PrefProxy.
	 **/
	private static PrefProxy prefs;
	
	/**
	 *	Returns the global PrefProxy.
	 *	
	 *	If the PrefProxy hasn't been created yet, it gets created here.  If we
	 *	are in standalone mode, this will return a StandAlonePrefs which uses
	 *	Java's properties.  If we are in plugin mode, this will return a TCPrefs
	 *	object which proxies TopCoder's LocalPreferences class.
	 *	
	 *	This allows the rest of KawigiEdit to assume it can get the values of
	 *	preference settings without needing to know about TopCoder's
	 *	LocalPreferences class.
	 **/
	public static PrefProxy getPrefs()
	{
		if (prefs == null)
			if (AppEnvironment.getEnvironment() == AppEnvironment.PluginMode)
				prefs = new TCPrefs();
			else //if (AppEnvironment.getEnvironment() == AppEnvironment.ApplicationMode)
				prefs = new StandAlonePrefs(new File("KawigiEdit.properties"));
		return prefs;
	}
}
