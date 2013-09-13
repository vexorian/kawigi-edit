package kawigi.widget;
import kawigi.cmd.*;
import javax.swing.*;
import java.beans.*;

/**
 *	This is a subclass of JRadioButton that allows the Action to set its
 *	visibility and selected state.
 *	
 *	See rant in ActionStateCheckBox for more information.
 **/
@SuppressWarnings("serial")
public class ActionStateRadioButton extends JRadioButton implements PropertyChangeListener
{
	/**
	 *	Constructs a new ActionstateRadioButton for the given Action.
	 **/
	public ActionStateRadioButton(Action a)
	{
		super(a);
		a.addPropertyChangeListener(this);
		if (a instanceof DefaultAction)
		{
			setVisible(((DefaultAction)a).isVisible());
			setSelected(((Boolean)((DefaultAction)a).getValue(DefaultAction.SELECTED)).booleanValue());
		}
	}
	
	/**
	 *	Add a couple more properties the normal listener won't take care of.
	 **/
	public void propertyChange(PropertyChangeEvent e)
	{
		if (e.getPropertyName().equals(DefaultAction.VISIBLE))
			setVisible(((Boolean)e.getNewValue()).booleanValue());
		if (e.getPropertyName().equals(DefaultAction.SELECTED))
			setSelected(((Boolean)e.getNewValue()).booleanValue());
	}
}
