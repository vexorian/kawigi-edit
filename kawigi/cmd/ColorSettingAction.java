package kawigi.cmd;
import kawigi.properties.*;
import kawigi.editor.*;
import kawigi.widget.*;
import java.awt.*;
import java.awt.event.*;

/**
 *	Setting action implementation for color settings.
 **/
@SuppressWarnings("serial")
public class ColorSettingAction extends SettingAction
{
	/**
	 *	Constructs a new ColorSettingAction for the given ActID.
	 **/
	public ColorSettingAction(ActID cmdid)
	{
		super(cmdid);
	}
	
	/**
	 *	Does nothing :-)
	 **/
	public void actionPerformed(ActionEvent e)
	{
	}
	
	/**
	 *	Overridden to override the COLOR property, which is recognized by
	 *	kawigi.widget.ColorSwatchDropdown as the value for the currently
	 *	selected color.
	 **/
	public Object getValue(String key)
	{
		PrefProxy prefs = getCurrentPrefs();
		if (key.equals(COLOR))
		{
			if (prefs.getColor(cmdid.preference) == null)
				return cmdid.defaultValue;
			else
				return prefs.getColor(cmdid.preference);
		}
		return super.getValue(key);
	}
	
	/**
	 *	Overridden to set the correct setting if the COLOR property is changed.
	 **/
	public void putValue(String key, Object value)
	{
		PrefProxy prefs = getCurrentPrefs();
		if (key.equals(COLOR))
		{
			prefs.setColor(cmdid.preference, (Color)value);
			if (!delayNotify())
			{
				// Do notifications:
				switch (cmdid)
				{
					case actTestBackground:
					case actTestForeground:
						if (Dispatcher.getCompileComponent() != null)
							Dispatcher.getCompileComponent().updatePrefs();
						if (Dispatcher.getOutputComponent() != null)
							Dispatcher.getOutputComponent().updatePrefs();
						break;
					case actTimerLEDColor:
					case actTimerBGColor:
					case actTimerUnlitColor:
						ProblemTimer.resetPrefs();
						break;
					case actForegroundColor:
					case actBackgroundColor:
					case actSelectionColor:
					case actSelectedTextColor:
						if (Dispatcher.getCodePane() != null)
							Dispatcher.getCodePane().resetPrefs();
						if (Dispatcher.getLocalCodePane() != null)
							Dispatcher.getLocalCodePane().resetPrefs();
						break;
					case actSyntaxKeywordColor:
					case actSyntaxTypeColor:
					case actSyntaxOperatorColor:
					case actSyntaxStringColor:
					case actSyntaxCommentColor:
					case actSyntaxDirectiveColor:
					case actSyntaxClassColor:
					case actSyntaxTagColor:
						GenericView.getColors();
						PythonView.initColors();
						CSharpView.initColors();
						JavaView.initColors();
						VBView.initColors();
                        CPPView.initColors();
						break;
					case actMatchingColor:
						GenericView.resetTabStop();
						break;
				}
			}
		}
		super.putValue(key, value);
	}
}
