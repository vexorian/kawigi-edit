package kawigi.cmd;
import kawigi.properties.*;
import kawigi.editor.*;
import java.awt.event.*;
/**
 *	Setting Action implementation for boolean-state settings
 **/
@SuppressWarnings("serial")
public class BooleanSettingAction extends SettingAction
{
	/**
	 *	Creates a new BooleanSettingAction with the given ActID.
	 **/
	public BooleanSettingAction(ActID cmdid)
	{
		super(cmdid);
	}
	
	/**
	 *	Toggles the value stored for this setting.
	 **/
	public void actionPerformed(ActionEvent e)
	{
		putValue(SELECTED, !((Boolean)getValue(SELECTED)));
		Dispatcher.getGlobalDispatcher().UIRefresh();
	}
	
	/**
	 *	Overridden to override the "SELECTED" property, which is recognized by
	 *	kawigi.widget.ActionState* as the selected state of the button.
	 **/
	public Object getValue(String key)
	{
		PrefProxy prefs = getCurrentPrefs();
		if (key.equals(SELECTED))
		{
			if (prefs.getProperty(cmdid.preference) == null)
				return cmdid.defaultValue;
			else
				return prefs.getBoolean(cmdid.preference);
		}
		return super.getValue(key);
	}
	
	/**
	 *	Stores a new value for the state of the property.
	 **/
	public void putValue(String key, Object value)
	{
		PrefProxy prefs = getCurrentPrefs();
		if (key.equals(SELECTED))
		{
			prefs.setBoolean(cmdid.preference, (Boolean)value);
			if (!delayNotify())
			{
				// Do notifications:
				switch (cmdid)
				{
					case actShowTimer:
						if (Dispatcher.getProblemTimer() != null)
						{
							boolean show = (Boolean)value;
							if (show)
								Dispatcher.getProblemTimer().start();
							else
								Dispatcher.getProblemTimer().stop();
						}
						break;
					case actDoMatching:
						GenericView.resetTabStop();
						break;
				}
			}
		}
		super.putValue(key, value);
	}
}
