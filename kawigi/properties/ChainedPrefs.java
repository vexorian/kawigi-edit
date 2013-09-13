package kawigi.properties;
import java.util.*;

/**
 *	This class is used by the settings dialog.
 *	
 *	The idea is to temporarily store changed values of settings and then commit
 *	them all at once (or not commit them if the dialog is cancelled).
 **/
public class ChainedPrefs extends AbstractPrefs
{
	/**
	 *	The prefs that current values are taken from if they aren't set here,
	 *	and that we commit changes to when commit() is called.
	 **/
	private PrefProxy chainPrefs;
	/**
	 *	Keys and values of properties set on this Prefs object.
	 **/
	private Properties props;
	
	/**
	 *	Constructs a new ChainedPrefs to proxy the given PrefProxy.
	 **/
	public ChainedPrefs(PrefProxy chainPrefs)
	{
		this.chainPrefs = chainPrefs;
		props = new Properties();
	}
	
	/**
	 *	Gets the current value of a property.
	 *	
	 *	If the property has been set on this object, it returns the value that
	 *	it was set to, otherwise it returns the value of the property in the
	 *	chained PrefProxy.
	 **/
	public String getProperty(String property)
	{
		if (props.containsKey(property))
			return props.getProperty(property);
		else
			return chainPrefs.getProperty(property);
	}
	
	/**
	 *	Locally sets the value of a property.
	 *	
	 *	This value isn't immediately committed to the chained PrefProxy.
	 **/
	public void setProperty(String property, String value)
	{
		props.setProperty(property, value);
	}
	
	/**
	 *	Saves the values of properties previously set on this PrefProxy, and
	 *	causes the chained PrefProxy to also commit.
	 **/
	public void commit()
	{
		Enumeration<?> en = props.propertyNames();
		while (en.hasMoreElements())
		{
			String key = en.nextElement().toString();
			chainPrefs.setProperty(key, props.getProperty(key));
		}
		chainPrefs.commit();
	}
}
