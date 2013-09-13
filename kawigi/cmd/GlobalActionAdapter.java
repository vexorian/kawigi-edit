package kawigi.cmd;
import java.awt.event.*;

/**
 *	The idea here is to have an action that works as a global version of a
 *	local command.  This can be useful when commands are positioned in global
 *	places (say a menu bar or toolbar or a macro) but have local scope (because
 *	they act on a code pane, for instance).  This will use the Dispatcher to
 *	try and guess which local context they should act on.
 **/
@SuppressWarnings("serial")
public class GlobalActionAdapter extends DefaultAction
{
	/**
	 *	Constructs a new GlobalActionAdapter on the given ActID.
	 **/
	public GlobalActionAdapter(ActID cmdid)
	{
		super(cmdid);
	}
	
	/**
	 *	Tries to find the most relevant local version of this action.
	 **/
	public DefaultAction getCurrentAction()
	{
		return Dispatcher.getGlobalDispatcher().getAction(cmdid, false);
	}
	
	/**
	 *	Returns true if it can get a local version of this action and that
	 *	action is enabled.
	 **/
	public boolean isEnabled()
	{
		DefaultAction action = getCurrentAction();
		if (action == null)
			return false;
		else
			return action.isEnabled();
	}
	
	/**
	 *	Returns false if it can get a local version of this action and that
	 *	action is hidden.
	 **/
	public boolean isVisible()
	{
		DefaultAction action = getCurrentAction();
		if (action == null)
			return true;
		else
			return action.isVisible();
	}
	
	/**
	 *	Tries to get the value of a property from the current local version of
	 *	this action.
	 **/
	public Object getValue(String s)
	{
		DefaultAction action = getCurrentAction();
		if (action == null)
			return super.getValue(s);
		else
			return action.getValue(s);
	}
	
	/**
	 *	Tries to set the value of a property on the current local version of
	 *	this action.
	 **/
	public void putValue(String s, Object value)
	{
		DefaultAction action = getCurrentAction();
		if (action != null)
		{
			action.putValue(s, value);
		}
		super.putValue(s, value);
	}
	
	/**
	 *	Executes the current local version of this action.
	 **/
	public void actionPerformed(ActionEvent e)
	{
		DefaultAction action = getCurrentAction();
		if (action != null)
			action.actionPerformed(e);
	}
}
