package kawigi.cmd;

import java.awt.event.ActionEvent;

import kawigi.KawigiEdit;

/**
 *	Action implementation for actions appearing in plugin mode only.
 **/
@SuppressWarnings("serial")
public final class PluginAction extends DefaultAction {

	/**
	 *	Constructs a new PluginAction for the given ActID.
	 **/
	public PluginAction(ActID cmdid) {
		super(cmdid);
	}

	/**
	 *	Runs the action!
	 **/
	public void actionPerformed(ActionEvent e) {
		switch (cmdid)
		{
		case actOutsideMode:
			KawigiEdit.setOutsideMode(!KawigiEdit.getOutsideMode());
			break;
		}
		Dispatcher.getGlobalDispatcher().UIRefresh();
	}

}
