package kawigi;
import kawigi.cmd.*;
import kawigi.util.*;
import javax.swing.*;
/**
 *	This is the executable class for running KawigiEdit as a standalone
 *	application instead of as a plugin.
 **/
public class KawigiEditStandalone
{
	/**
	 *	Creates and shows the KawigiEditStandalone window.
	 **/
	public static void main(String[] args)
	{
		AppEnvironment.setEnvironment(AppEnvironment.ApplicationMode);
		JFrame frame = (JFrame)UIHandler.loadMenu(MenuID.StandaloneFrame, Dispatcher.getGlobalDispatcher());
		Dispatcher.setWindow(frame);
		frame.setSize(500, 500);
		frame.addWindowListener(new StandaloneWindowListener());
		frame.setVisible(true);
	}
}
