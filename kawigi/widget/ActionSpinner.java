package kawigi.widget;
import kawigi.cmd.*;
import javax.swing.*;
import javax.swing.event.*;
import java.beans.*;

/**
 *	A subclass of JSpinner that can use Actions to store properties.
 **/
@SuppressWarnings("serial")
public class ActionSpinner extends JSpinner implements PropertyChangeListener, ChangeListener
{
	/**
	 *	The action.
	 **/
	private Action a;
	
	/**
	 *	Constructs a new ActionSpinner that uses the action as the source for
	 *	several properties, including its SpinnerModel and value.
	 **/
	public ActionSpinner(Action a)
	{
		this.a = a;
		SpinnerModel model = (SpinnerModel)a.getValue(DefaultAction.SPINNER_MODEL);
		if (model != null)
			setModel(model);
		Object o = a.getValue(DefaultAction.SPINNER_VALUE);
		if (o != null)
			setValue(o);
		addChangeListener(this);
		a.addPropertyChangeListener(this);
	}
	
	/**
	 *	Notifies the Action that the user has changed the value of this Spinner.
	 **/
	public void stateChanged(ChangeEvent e)
	{
		a.putValue(DefaultAction.SPINNER_VALUE, getValue());
	}
	
	/**
	 *	Processes property changes from the action.
	 **/
	public void propertyChange(PropertyChangeEvent e)
	{
		// Text fields should be able to have their text centralized here.
		if (e.getPropertyName().equals(DefaultAction.SPINNER_VALUE))
		{
			if (!e.getNewValue().equals(getValue()))
			{
				setValue(e.getNewValue());
			}
		}
		// For completeness this should be here, but I don't trust it enough
		// to say it's supported.
		else if (e.getPropertyName().equals(DefaultAction.SPINNER_MODEL))
		{
			if (!e.getNewValue().equals(getModel()))
				setModel((SpinnerModel)e.getNewValue());
		}
	}
}
