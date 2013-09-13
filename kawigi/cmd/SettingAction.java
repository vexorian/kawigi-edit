package kawigi.cmd;
import kawigi.properties.*;
import kawigi.widget.*;
import kawigi.editor.*;
import javax.swing.*;
import java.awt.event.*;

/**
 *	Action implementation for setting actions.
 *
 *	This class really serves two purposes.  The first is to implement actions
 *	that are related to settings but aren't settings themselves (for instance,
 *	launching the config dialog and the OK and Cancel buttons on the config
 *	dialog).
 *
 *	The other purpose is that it's the base class of all setting actions.  As
 *	part of this, there are a set of static variables and methods that are
 *	shared by all setting instances.
 *
 *	The intended behavior is that if the settings dialog is up, all settings are
 *	set on a temporary prefs object, and committed when the "OK" button is
 *	pushed.  Otherwise, the setting is applied immediately.  Therefore, if
 *	buttons bound to setting actions are put on the main UI somewhere, they will
 *	be effective immediately on being used, but settings done on the dialog are
 *	cancellable.
 **/
@SuppressWarnings("serial")
public class SettingAction extends DefaultAction
{
	/**
	 *	Temporary storage for settings until the settings dialog is committed.
	 **/
	protected static PrefProxy tempPrefs;
	/**
	 *	Reference to the config dialog.
	 **/
	protected static JDialog dialog;

	/**
	 *	Returns the temporary prefs if there is one, otherwise the real
	 *	KawigiEdit prefs.
	 **/
	protected static PrefProxy getCurrentPrefs()
	{
		if (tempPrefs == null)
			return PrefFactory.getPrefs();
		else
			return tempPrefs;
	}

	/**
	 *	Returns true if settings shouldn't be committed yet.
	 *
	 *	Even though things set to the temporary prefs won't be committed par se,
	 *	in order to immediately be effective, some settings need to notify other
	 *	objects (for instance, syntax highlighting settings require a
	 *	repopulation of some structures in the View classes), but they should
	 *	only do that if delayNotify() returns false.
	 **/
	protected static boolean delayNotify()
	{
		return tempPrefs != null;
	}

	/**
	 *	Constructs a new SettingAction for the given ActID.
	 **/
	public SettingAction(ActID cmdid)
	{
		super(cmdid);
	}

	/**
	 *	Executes the non-setting setting commands.
	 **/
	public void actionPerformed(ActionEvent e)
	{
		switch (cmdid)
		{
			case actLaunchConfig:
				if (dialog == null)
				{
					dialog = new JDialog(Dispatcher.getWindow(), "KawigiEdit Configuration", true);
					dialog.getContentPane().add(UIHandler.loadMenu(MenuID.ConfigPanel, Dispatcher.getGlobalDispatcher()));
					dialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
					dialog.pack();
					dialog.addWindowListener(
							new WindowAdapter() {
								public void windowClosing(WindowEvent e) {
									Dispatcher.getGlobalDispatcher().runCommand(ActID.actCancelConfig);
								}
							});
				}
				if (tempPrefs == null)
					tempPrefs = new ChainedPrefs(PrefFactory.getPrefs());
				Dispatcher.getGlobalDispatcher().UIRefresh();
				dialog.setVisible(true);
				break;
			case actCommitConfig:
				tempPrefs.commit();
				doUpdates();
				// fallthrough...
			case actCancelConfig:
				tempPrefs = null;
				if (dialog != null)
				{
					dialog.setVisible(false);
				}
				break;
		}
	}

	/**
	 *	Returns true if this action is available.
	 **/
	public boolean isEnabled()
	{
		return true;
	}

	/**
	 *	Does all the commital actions that need to happen assuming all settings
	 *	were changed at once.
	 **/
	public void doUpdates()
	{
		if (Dispatcher.getProblemTimer() != null)
		{
			boolean show = getCurrentPrefs().getBoolean("kawigi.timer.show");
			if (show)
				Dispatcher.getProblemTimer().start();
			else
				Dispatcher.getProblemTimer().stop();
		}
		ProblemTimer.resetPrefs();
		GenericView.getColors();
        CPPView.initColors();
		PythonView.initColors();
		CSharpView.initColors();
		JavaView.initColors();
		VBView.initColors();
		GenericView.resetTabStop();
		if (Dispatcher.getCompileComponent() != null)
			Dispatcher.getCompileComponent().updatePrefs();
		if (Dispatcher.getOutputComponent() != null)
			Dispatcher.getOutputComponent().updatePrefs();
		if (Dispatcher.getEditorPanel() != null)
			Dispatcher.getCodePane().resetPrefs();
		if (Dispatcher.getLocalCodeEditorPanel() != null)
			Dispatcher.getLocalCodePane().resetPrefs();
	}
}
