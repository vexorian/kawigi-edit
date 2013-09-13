package kawigi.cmd;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.*;

/**
 *	Superclass of all KawigiEdit actions.
 **/
public abstract class DefaultAction extends AbstractAction implements FocusListener
{
	// Constant names for properties recognized specifically by KawigiEdit
	// actions that aren't from Java's Action interface.
	/**
	 *	Image property for icon if it should show a larger (24x24 instead of
	 *	16x16) icon.
	 **/
	public static final String LARGE_ICON = "LargeIcon";
	/**
	 *	Color property for the currently selected color in a color-selection
	 *	control.
	 **/
	public static final String COLOR = "Color";
	/**
	 *	Boolean visibility property to show or hide controls.
	 **/
	public static final String VISIBLE = "Visible";
	/**
	 *	String property to override the text of a text box.
	 **/
	public static final String TEXT = "Text";
	/**
	 *	Boolean property of the state of a check box or something similar.
	 **/
	public static final String SELECTED = "Selected";
	/**
	 *	Font property stored for a kawigi.widget.FontPanel
	 **/
	public static final String FONT = "Font";
	/**
	 *	SpinnerModel property (provides an object that implements SpinnerModel)
	 **/
	public static final String SPINNER_MODEL = "SpinnerModel";
	/**
	 *	Value of a spinner - the type depends on the SpinnerModel implementation
	 **/
	public static final String SPINNER_VALUE = "SpinnerValue";

	/**
	 *	The ActID for this Action.
	 **/
	protected ActID cmdid;

	/**
	 *	Visibility state
	 **/
	protected boolean visible;

	/**
	 *	Last set value for enable property.
	 **/
	protected boolean enableSet;

	/**
	 * Last set value for visible property.
	 **/
	protected boolean visibleSet;

	/**
	 * Flags if input focus is in the element related to this action
	 */
	private int nFocusGained = 0;

	/**
	 *	Constructor for DefaultAction - requires an ActID.
	 *
	 *	It also sets a bunch of properties based on the values in the ActID.
	 **/
	protected DefaultAction(ActID cmdid)
	{
		this.cmdid = cmdid;
		if (cmdid.label != null)
			putValue(NAME, cmdid.label);
		if (cmdid.tooltip != null)
			putValue(SHORT_DESCRIPTION, cmdid.tooltip);
		if (cmdid.iconFile != null) {
			java.net.URL rsrc = getClass().getClassLoader()
									.getResource(cmdid.iconFile.replaceAll("\\?", "16"));
			if (rsrc != null)
				putValue(SMALL_ICON, new ImageIcon(rsrc));
		}
		if (cmdid.accelerator != null)
			putValue(ACCELERATOR_KEY, cmdid.accelerator);
		if (cmdid.mnemonic != null)
			putValue(MNEMONIC_KEY, cmdid.mnemonic);
		visible = true;
		enabled = true;
		enableSet = false;
		visibleSet = false;
	}

	/**
	 *	Refreshes the values of all properties.
	 **/
	public void UIRefresh()
	{
		Object[] keys = getKeys();
		if (null != keys) {
			for (int i=0; i<keys.length; i++)
			{
				Object setVal = super.getValue(keys[i].toString());
				Object realVal = getValue(keys[i].toString());
				if (setVal != realVal && (setVal == null || realVal == null || !setVal.equals(realVal)))
					putValue(keys[i].toString(), realVal);
			}
		}
		// Yeah, this is a bit of a hack to make sure that controls know what
		// state they're in and Actions don't *have* to override isEnabled()
		// and isVisible().
		// This action leads to very strange behaviour: after changing first character in textbox
		// it loses its focus. I've tried to work around this problem by remembering
		// focus gaining. I have feeling that it can work inconsistently in
		// some situations, but I couldn't find such and I think this inconsistentness
		// is better than strange losing of focus when you don't expect it.
		boolean tempVisible = isVisible();
		if ((0 == nFocusGained || !tempVisible) && !visibleSet)
		{
			visibleSet = true;
			setVisible(!tempVisible);
		}
		setVisible(tempVisible);
		boolean tempEnabled = isEnabled();
		if ((0 == nFocusGained || !tempEnabled) && !enableSet)
		{
			enableSet = true;
			setEnabled(!tempEnabled);
		}
		setEnabled(tempEnabled);
	}

	/**
	 *	returns the ActID for this Action.
	 **/
	public ActID getID()
	{
		return cmdid;
	}

	/**
	 *	Override to allow the control to hide dynamically.
	 *
	 *	This won't work on any control, but it will work on many of the controls
	 *	in kawigi.widget.
	 **/
	public boolean isVisible()
	{
		return visible;
	}

	/**
	 *	Sets the visibility property.
	 **/
	public void setVisible(boolean newValue)
	{
		boolean oldValue = visible;
		if (oldValue != newValue)
		{
	    	visible = newValue;
	    	firePropertyChange(VISIBLE, Boolean.valueOf(oldValue), Boolean.valueOf(newValue));
		}
	}

	/**
	 *	Must override to define what the action <i>does</i> when it launches
	 *	an event.
	 **/
	public abstract void actionPerformed(ActionEvent e);

	/**
	 *	Prints a stack trace to the console and if it's not a warning, brings
	 *	up an error dialog and writes stack trace to log component.
	 **/
	protected void reportError(Throwable t, boolean warning)
	{
		t.printStackTrace();
		if (!warning) {
		    Dispatcher.logln(t.toString());
		    int lim = 5;
            for (StackTraceElement st : t.getStackTrace() ) {
                // limit to 5 lines:
                lim--;
                if (lim < 0) {
                    Dispatcher.logln( "(and more...)" );
                    break;
                }
                Dispatcher.logln(st.toString());
            }
            

			try {
				JOptionPane.showMessageDialog(Dispatcher.getWindow(), t, "Error: " + cmdid + " in " + getClass(), JOptionPane.ERROR_MESSAGE);
			} catch (HeadlessException ex) {
			}
		}
	}

	/**
	 *	Determines if this action is equal to another one.
	 **/
	public boolean equals(Object o)
	{
		return getClass().equals(o.getClass()) && ((DefaultAction)o).cmdid == cmdid;
	}

	/**
	 * Remembers that focus is gained to the element related to this action
	 *
	 * @param e         Event for focus gaining
	 **/
	public void focusGained(FocusEvent e)
	{
		++nFocusGained;
	}

	/**
	 * Remembers that focus is gone away from tje element related to this action
	 *
	 * @param e         Event for losing focus
	 **/
	public void focusLost(FocusEvent e)
	{
		--nFocusGained;
	}
}
