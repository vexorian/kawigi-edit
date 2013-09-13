package kawigi.widget;
import kawigi.cmd.*;
import javax.swing.*;
import java.beans.*;

/**
 *	Just a JLabel that can be configured and updated by an Action.
 **/
@SuppressWarnings("serial")
public class ActionLabel extends JLabel implements PropertyChangeListener
{
	/**
	 *	The Action that this label is configured against.
	 **/
	protected Action a;
	
	/**
	 *	Constructor without parameters.
	 **/
	public ActionLabel()
	{
	}

	/**
	 *	Constructor that uses an Action (if you want to use any other parameters
	 *	for the constructor, you should just use a JLabel).
	 **/
	public ActionLabel(Action a)
	{
		this();
		setAction(a);
	}

	/**
	 *	Changes the action for the label.
	 **/
	public void setAction(Action a)
	{
		if (null != this.a) {
			this.a.removePropertyChangeListener(this);
		}

		this.a = a;
		
		if (a.getValue(Action.NAME) != null)
			setText((String)a.getValue(Action.NAME));
		if (a.getValue(Action.SHORT_DESCRIPTION) != null)
			setToolTipText((String)a.getValue(Action.SHORT_DESCRIPTION));
		if (a.getValue(Action.MNEMONIC_KEY) != null)
			setDisplayedMnemonic(((Integer)a.getValue(Action.MNEMONIC_KEY)).intValue());
		if (a.getValue(Action.SMALL_ICON) != null)
			setIcon((Icon)a.getValue(Action.SMALL_ICON));
		setEnabled(a.isEnabled());
		if (a instanceof DefaultAction)
			setVisible(((DefaultAction)a).isVisible());
		a.addPropertyChangeListener(this);
	}
	
	/**
	 *	Processes property changes from the action.
	 **/
	public void propertyChange(PropertyChangeEvent e)
	{
		// Text fields should be able to have their text centralized here.
		if (e.getPropertyName().equals(Action.NAME))
			setText((String)e.getNewValue());
		else if (e.getPropertyName().equals(Action.SHORT_DESCRIPTION))
			setToolTipText((String)e.getNewValue());
		else if (e.getPropertyName().equals(Action.MNEMONIC_KEY))
			setDisplayedMnemonic(((Integer)e.getNewValue()).intValue());
		// They really should have made and exposed a constant for this:
		else if (e.getPropertyName().equals("enabled"))
			setEnabled(((Boolean)e.getNewValue()).booleanValue());
		// I want to be able to hide commands through Actions, on rare occasions.
		else if (e.getPropertyName().equals(DefaultAction.VISIBLE))
			setVisible(((Boolean)e.getNewValue()).booleanValue());
		else if (e.getPropertyName().equals(Action.SMALL_ICON))
			setIcon((Icon)e.getNewValue());			
	}
}
