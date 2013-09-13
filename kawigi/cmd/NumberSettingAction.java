package kawigi.cmd;
import kawigi.properties.*;
import kawigi.editor.*;
import kawigi.widget.*;
import java.awt.event.*;
import javax.swing.*;

/**
 *	Setting action implementation for number options that are set using a
 *	spinner.
 **/
@SuppressWarnings("serial")
public class NumberSettingAction extends SettingAction
{
	/**
	 *	Spinner model used for the spinner on this setting.
	 **/
	protected SpinnerModel model;
	
	/**
	 *	Constructs a new NumberSettingAction for the given ActID.
	 **/
	public NumberSettingAction(ActID cmdid)
	{
		super(cmdid);
	}
	
	/**
	 *	Does nothing.
	 *	
	 *	Spinners shouldn't be firing events :-)
	 **/
	public void actionPerformed(ActionEvent e)
	{
	}
	
	/**
	 *	Overridden to get the right value for the SPINNER_VALUE and
	 *	SPINNER_MODEL properties.
	 **/
	public Object getValue(String key)
	{
		PrefProxy prefs = getCurrentPrefs();
		if (key.equals(SPINNER_VALUE))
		{
			if (prefs.getProperty(cmdid.preference) == null)
				return ((int[])cmdid.defaultValue)[0];
			else
				return prefs.getInt(cmdid.preference);
		}
		else if (key.equals(SPINNER_MODEL))
		{
			if (super.getValue(key) == null)
			{
				if (model == null)
				{
					int[] def = (int[])cmdid.defaultValue;
					model = new SpinnerNumberModel(((Integer)getValue(SPINNER_VALUE)).intValue(), def[1], def[2], def[3]);
				}
				return model;
			}
		}
		return super.getValue(key);
	}
	
	/**
	 *	Overridden to implement putting the SPINNER_VALUE property in the right
	 *	place.
	 **/
	public void putValue(String key, Object value)
	{
		PrefProxy prefs = getCurrentPrefs();
		if (key.equals(SPINNER_VALUE))
		{
			prefs.setInt(cmdid.preference, (Integer)value);
			if (!delayNotify())
			{
				// Do notifications:
				switch (cmdid)
				{
					case actTimerDelay:
						ProblemTimer.resetPrefs();
						break;
					case actTabWidth:
						GenericView.resetTabStop();
						break;
				}
			}
		}
		super.putValue(key, value);
	}
}
