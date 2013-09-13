package kawigi.widget;
import kawigi.cmd.*;
import javax.swing.*;
import java.beans.*;

/**
 *	This is a subclass of JCheckBox that allows the Action to set its visibility
 *	and selected state.
 *	
 *	Swing's JCheckBox already allows me to use an Action, but it didn't make all
 *	the properties I needed available.
 *	
 *	[rant]
 *	Swing has a great idea with the Action interface (and AbstractAction class),
 *	but the whole idea is underdevelopped and undersupported.  For one thing, at
 *	least in some situations, the Action should just be queried for information
 *	at runtime (for instance, if the Action is represented on a menu, all or
 *	most property values should be queried when the menu is dropped).  Of
 *	course, this doesn't really help much for action-enabled components that are
 *	always showing.  For another thing, there aren't nearly enough properties
 *	that can be overridden, and an Action behind a check box, radio button or
 *	toggle button should be able to specify state.  This is one of the things
 *	that should already exist in Swing's Actions (even if it doesn't apply to
 *	all control types) that I just had to add myself.  The reason it should be
 *	part of Swing Actions is that the selected state of multiple controls that
 *	map to the same action should all be the same.  Control Visibility is
 *	another thing, and I typically add visibility property support when I have
 *	to subclass a swing control for any other reason, although in the case of
 *	this class, the real purpose is to allow the selected state to be queried
 *	from the action.
 *	[/rant]
 **/
@SuppressWarnings("serial")
public class ActionStateCheckBox extends JCheckBox implements PropertyChangeListener
{
	/**
	 *	Constructs a new ActionStateCheckBox that binds data to the given
	 *	Action.
	 *	
	 *	While nothing bad will necessarily happen otherwise, things may not work
	 *	perfectly properly if the Action isn't a DefaultAction.
	 **/
	public ActionStateCheckBox(Action a)
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
