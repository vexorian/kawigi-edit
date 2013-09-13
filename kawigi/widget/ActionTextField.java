package kawigi.widget;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import kawigi.cmd.*;
import kawigi.util.StringsUtil;

/**
 *	Just a JTextField that can be configured and updated by an Action.
 **/
@SuppressWarnings("serial")
public class ActionTextField extends JTextField implements PropertyChangeListener, DocumentListener
{
	/**
	 *	The Action that this text field is configured against.
	 **/
	protected Action a;

	/**
	 *	Constructor without parameters.
	 **/
	public ActionTextField()
	{
	}

	/**
	 *	Constructor that uses an Action (if you want to use any other parameters
	 *	for the constructor, you should just use a JTextField).
	 **/
	public ActionTextField(Action a)
	{
		this();
		setAction(a);
	}

	/**
	 *	Changes the action for this field.
	 **/
	public void setAction(Action a)
	{
		if (null != this.a) {
			this.a.removePropertyChangeListener(this);
			removeActionListener(this.a);
		}

		this.a = a;

		if (a.getValue(DefaultAction.TEXT) != null)
			setText(a.getValue(DefaultAction.TEXT).toString());
		if (a.getValue(Action.SHORT_DESCRIPTION) != null)
			setToolTipText(a.getValue(Action.SHORT_DESCRIPTION).toString());
		if (a.getValue(Action.MNEMONIC_KEY) != null)
			setFocusAccelerator((char)((Integer)a.getValue(Action.MNEMONIC_KEY)).intValue());
		setEnabled(a.isEnabled());
		if (a instanceof DefaultAction)
			setVisible(((DefaultAction)a).isVisible());
		a.addPropertyChangeListener(this);
		getDocument().addDocumentListener(this);
		addActionListener(a);
	}

	public void setColumns(String columns)
	{
		setColumns(Integer.parseInt(columns));
	}

	/**
	 *	Processes property changes from the action.
	 **/
	public void propertyChange(PropertyChangeEvent e)
	{
		// Text fields should be able to have their text centralized here.
		if (e.getPropertyName().equals(DefaultAction.TEXT))
		{
			if (!StringsUtil.isEqual((CharSequence)e.getNewValue(), getText()))
				setText(e.getNewValue().toString());
		}
		else if (e.getPropertyName().equals(Action.SHORT_DESCRIPTION))
			setToolTipText((String)e.getNewValue());
		else if (e.getPropertyName().equals(Action.MNEMONIC_KEY))
			setFocusAccelerator((char)((Integer)e.getNewValue()).intValue());
		// They really should have made and exposed a constant for this:
		else if (e.getPropertyName().equals("enabled"))
			setEnabled(((Boolean)e.getNewValue()).booleanValue());
		// I want to be able to hide commands through Actions, on rare occasions.
		else if (e.getPropertyName().equals(DefaultAction.VISIBLE))
			setVisible(((Boolean)e.getNewValue()).booleanValue());
	}

	/**
	 *	Called when the text in the TextField changes.
	 **/
	public void changedUpdate(DocumentEvent e)
	{
		textChanged();
	}

	/**
	 *	Called when the text in the TextField changes.
	 **/
	public void insertUpdate(DocumentEvent e)
	{
		textChanged();
	}

	/**
	 *	Called when the text in the TextField changes.
	 **/
	public void removeUpdate(DocumentEvent e)
	{
		textChanged();
	}

	/**
	 *	Officially changes the TEXT property of the action.
	 **/
	protected void textChanged()
	{
		if (!getText().equals(a.getValue(DefaultAction.TEXT)))
			a.putValue(DefaultAction.TEXT, getText());
		Dispatcher.getGlobalDispatcher().UIRefresh();
	}
}
