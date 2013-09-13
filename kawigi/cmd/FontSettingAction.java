package kawigi.cmd;
import kawigi.properties.*;
import java.awt.*;
import java.awt.event.*;

/**
 *	Setting action implementation for font settings.
 *	
 *	This action is meant to be set on a kawigi.widget.FontPanel.
 **/
@SuppressWarnings("serial")
public class FontSettingAction extends SettingAction
{
	/**
	 *	Constructs a new FontSettingAction with the given ActID.
	 **/
	public FontSettingAction(ActID cmdid)
	{
		super(cmdid);
	}
	
	/**
	 *	Does nothing :-)
	 *	
	 *	FontPanels shouldn't fire actions.
	 **/
	public void actionPerformed(ActionEvent e)
	{
	}
	
	/**
	 *	Overriden to get the right value for the FONT property.
	 **/
	public Object getValue(String key)
	{
		PrefProxy prefs = getCurrentPrefs();
		if (key.equals(FONT))
		{
			if (prefs.getProperty(cmdid.preference) == null)
				return cmdid.defaultValue;
			else
				return prefs.getFont(cmdid.preference);
		}
		return super.getValue(key);
	}
	
	/**
	 *	Overridden to properly set the FONT property.
	 **/
	public void putValue(String key, Object value)
	{
		PrefProxy prefs = getCurrentPrefs();
		if (key.equals(FONT))
		{
			prefs.setFont(cmdid.preference, (Font)value);
			if (!delayNotify())
			{
				// Do notifications:
				switch (cmdid)
				{
					case actTestFont:
						if (Dispatcher.getCompileComponent() != null)
							Dispatcher.getCompileComponent().updatePrefs();
						if (Dispatcher.getOutputComponent() != null)
							Dispatcher.getOutputComponent().updatePrefs();
						break;
					case actCodeFont:
						if (Dispatcher.getCodePane() != null)
							Dispatcher.getCodePane().resetPrefs();
						if (Dispatcher.getLocalCodePane() != null)
							Dispatcher.getLocalCodePane().resetPrefs();
						break;
				}
			}
		}
		super.putValue(key, value);
	}
}
