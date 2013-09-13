package kawigi.cmd;
import kawigi.properties.*;
import java.awt.event.*;

/**
 *	Setting action implementation for string settings.
 **/
@SuppressWarnings("serial")
public class TextSettingAction extends SettingAction
{
	/**
	 *	Constructs a new TextSettingAction for the given ActID.
	 **/
	public TextSettingAction(ActID cmdid)
	{
		super(cmdid);
	}
	
	/**
	 *	Does nothing.  We can get events from text boxes, but they aren't really
	 *	meaningful on setting boxes.
	 **/
	public void actionPerformed(ActionEvent e)
	{
	}
	
	/**
	 *	Overridden to return the correct value for the TEXT property.
	 **/
	public Object getValue(String key)
	{
		PrefProxy prefs = getCurrentPrefs();
		if (key.equals(TEXT))
		{
			if (prefs.getProperty(cmdid.preference) == null)
				return cmdid.defaultValue;
			else
				return prefs.getProperty(cmdid.preference);
		}
		return super.getValue(key);
	}
	
	/**
	 *	Overridden to properly save the TEXT property's value.
	 **/
	public void putValue(String key, Object value)
	{
		PrefProxy prefs = getCurrentPrefs();
		if (key.equals(TEXT))
			prefs.setProperty(cmdid.preference, (String)value);
		super.putValue(key, value);
	}
}
