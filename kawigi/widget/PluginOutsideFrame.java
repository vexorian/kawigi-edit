package kawigi.widget;

import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

import kawigi.KawigiEdit;
import kawigi.cmd.Dispatcher;

/**
 * Frame for hosting plugin controls when working in out-of-the-arena mode.
 */
@SuppressWarnings("serial")
public final class PluginOutsideFrame extends JFrame implements WindowListener, HierarchyListener
{
	public PluginOutsideFrame() {
		super("Outside "+KawigiEdit.versionString);
		setSize(900, 500);
		addWindowListener(this);
		
		JFrame wind = KawigiEdit.getArenaWindow();
		if (wind == null) {
			KawigiEdit.getMainPanel().addHierarchyListener(this);
		}
		else {
			wind.addWindowListener(this);
		}
	}

	public void windowActivated(WindowEvent e) {}

	public void windowClosed(WindowEvent e) {}

	/**
	 * Part of WindowListener interface. Used to catch the moment of closing of
	 * either this window of Arena window with the problem.
	 */
	public void windowClosing(WindowEvent e) {
		if (e.getSource() == KawigiEdit.getArenaWindow()) {
			setVisible(false);
			KawigiEdit.getMainPanel().addHierarchyListener(this);
		}
		else {
			KawigiEdit.setOutsideMode(false);
		}
	}

	public void windowDeactivated(WindowEvent e) {}

	public void windowDeiconified(WindowEvent e) {}

	public void windowIconified(WindowEvent e) {}

	/**
	 * Part of WindowListener interface. Used to catch the moment of opening of
	 * the Arena problem window to automatically open this out-of-the-arena
	 * frame and focus it.
	 */
	public void windowOpened(WindowEvent e) {
		if (e.getSource() == KawigiEdit.getArenaWindow() && KawigiEdit.getOutsideMode()) {
			setVisible(true);
			// A small trick to force this window to be in front when TC problem
			// is opened in outside mode
			setAlwaysOnTop(true);
			Dispatcher.getEditorPanel().requestFocusInWindow();
			setAlwaysOnTop(false);
		}
	}

	/**
	 * Part of HierarchyListener interface. Used to add this object as a listener
	 * to the Arena problem window.
	 */
	public void hierarchyChanged(HierarchyEvent e) {
		if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0
				&& KawigiEdit.getMainPanel().isShowing())
		{
			JFrame wind = KawigiEdit.getArenaWindow();
			if (wind != null) {
				wind.removeWindowListener(this);
				wind.addWindowListener(this);
				KawigiEdit.getMainPanel().removeHierarchyListener(this);
			}
		}
	}
}
