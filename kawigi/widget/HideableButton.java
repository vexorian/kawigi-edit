package kawigi.widget;
import kawigi.cmd.*;
import javax.swing.*;
import java.beans.*;

/**
 *	Subclass of JButton that allows an Action to change the visibility of the
 *	button.
 *	
 *	The rant in the description of the ActionStateCheckBox is mostly applicable
 *	here.
 **/
@SuppressWarnings("serial")
public class HideableButton extends JButton implements PropertyChangeListener
{
	/**
	 *	Constructs a new HideableButton bound to the given action.
	 **/
	public HideableButton(Action a)
	{
		super(a);
		a.addPropertyChangeListener(this);
		if (a instanceof DefaultAction)
			setVisible(((DefaultAction)a).isVisible());
	}
	
	/**
	 *	Add one more property the Button's listener won't take care of.
	 **/
	public void propertyChange(PropertyChangeEvent e)
	{
		if (e.getPropertyName().equals(DefaultAction.VISIBLE))
			setVisible(((Boolean)e.getNewValue()).booleanValue());
	}
}