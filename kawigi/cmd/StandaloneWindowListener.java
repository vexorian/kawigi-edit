package kawigi.cmd;
import java.awt.event.*;
import kawigi.properties.*;

/**
 *	WindowListener implementation for KawigiEditStandalone.
 *	
 *	The main purpose here is to make the window commit preferences and exit
 *	when the window is closed.
 **/
public class StandaloneWindowListener implements WindowListener
{
	/**
	 *	Commits preferences, disposes the window, and exits the program.
	 **/
	public void windowClosing(WindowEvent e)
	{
		PrefFactory.getPrefs().commit();
		e.getWindow().dispose();
		System.exit(0);
	}
	
	/**
	 *	Empty.
	 *	
	 *	From the WindowListener interface.
	 **/
	public void windowActivated(WindowEvent e){}
	
	/**
	 *	Empty.
	 *	
	 *	From the WindowListener interface.
	 **/
	public void windowClosed(WindowEvent e){}
	
	/**
	 *	Empty.
	 *	
	 *	From the WindowListener interface.
	 **/
	public void windowDeactivated(WindowEvent e){}
	
	/**
	 *	Empty.
	 *	
	 *	From the WindowListener interface.
	 **/
	public void windowDeiconified(WindowEvent e){}
	
	/**
	 *	Empty.
	 *	
	 *	From the WindowListener interface.
	 **/
	public void windowIconified(WindowEvent e){}
	
	/**
	 *	Empty.
	 *	
	 *	From the WindowListener interface.
	 **/
	public void windowOpened(WindowEvent e){}
}
