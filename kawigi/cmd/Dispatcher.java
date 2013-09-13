package kawigi.cmd;
import kawigi.editor.*;
import kawigi.widget.*;
import kawigi.properties.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.*;
import java.util.*;
import java.text.SimpleDateFormat;

/**
 *	This class is the switchboard of KawigiEdit's command infrastructure, as of
 *	version 2.0.  If you need an Action, it will usually come from here.  If you
 *	want to execute an action, or find an action so you can change properties on
 *	it, this is also your class.  And if you need global access to several
 *	important UI components (like the main CodePane), you can get it from this
 *	class.
 *
 *	There is a lot of references to "subdispatchers" in the kawigi.cmd package.
 *	There is one global dispatcher that holds commands that don't need access
 *	to a control on which to act, and it has a set of dispatchers (called
 *	subdispatchers that are bound to objects (currently always CodePanes) on
 *	which they act.
 **/
public class Dispatcher implements FocusListener, WindowListener, HierarchyListener
{
	/**
	 *	Map of ActIDs to Actions that have already been instantiated on this
	 *	dispatcher.
	 **/
	protected Map<ActID, DefaultAction> actionMap;
	/**
	 *	The all-important singleton global dispatcher.
	 **/
	protected static Dispatcher globalDispatcher;
	/**
	 *	The top-level window containing KawigiEdit.
	 **/
	//private static JFrame window;
	/**
	 *	Global editor panels required by several actions.
	 **/
	private static EditorPanel editorPanel, localCodeEditorPanel, testEditorPanel, templateEditorPanel;
	/**
	 * Flag set when KE itself does some edits of source code as opposed to
	 * user-initiated edits.
	 */
	private static boolean autoCodeEditing;
	/**
	 *	Global output displays.
	 **/
	private static SimpleOutputComponent outputComp, compileComp;
	/**
	 *	Log display.
	 **/
	private static SimpleOutputComponent logComp;
	/**
	 *	Global reference to the TabbedPane that has everything on it.
	 **/
	private static JTabbedPane tabs;
	/**
	 *	Global reference to the problem timer.
	 **/
	private static ProblemTimer timer;
	/**
	 *	Community file chooser.
	 **/
	private static JFileChooser globalFileChooser;
	/**
	 *	True if this is the global dispatcher.
	 **/
	private boolean global;
	/**
	 *	The object for commands to act on if this is a subdispatcher.
	 **/
	protected Component context;
	/**
	 *	A list of subdispatchers if this is the global dispatcher.
	 **/
	private java.util.List<Dispatcher> contextualDispatchers;
    /**
     *  Stores the last time the log was updated.
     */
    private static long lastLogTime = 0;
    /**
     *  Stores the maximum log message width.
     */
    private static int maxLogMessageWidth = 20;
    /**
     *  Allows some processes to temporarily disable file sync and avoid conflicts.
     */
    private static boolean fileSyncEnabled = true;
	/**
	 *	Instantiates the global dispatcher.
	 **/
	private Dispatcher()
	{
		global = true;
		context = null;
		contextualDispatchers = new ArrayList<Dispatcher>();
		actionMap = new HashMap<ActID, DefaultAction>();
	}

	/**
	 *	Instantiates a local subdispatcher that acts on context.
	 **/
	protected Dispatcher(Component context)
	{
		this.context = context;
		global = false;
		actionMap = new HashMap<ActID, DefaultAction>();
		globalDispatcher.contextualDispatchers.add(this);
		context.addFocusListener(globalDispatcher);
	}

	/**
	 *	Returns true if this dispatcher is the global dispatcher.
	 **/
	public boolean isGlobal()
	{
		return global;
	}

	/**
	 *	Accessor for the global dispatcher.
	 **/
	public static Dispatcher getGlobalDispatcher()
	{
		if (globalDispatcher == null)
			globalDispatcher = new Dispatcher();
		return globalDispatcher;
	}

	/**
	 *	Creates and returns a local subdispatcher that acts on context.
	 **/
	public Dispatcher createSubDispatcher(Component context)
	{
		return new Dispatcher(context);
	}

	/**
	 *	Deletes subdispatcher from internal structures and forgets it.
	 *  Method trusts to caller and doesn't check disp if it's null or
	 *  if it's really was created with createSubDispatcher.
	 **/
	public void eraseSubDispatcher(Dispatcher disp)
	{
		globalDispatcher.contextualDispatchers.remove(disp);
		disp.context.removeFocusListener(globalDispatcher);
	}

	/**
	 *	Sets the main window KawigiEdit is on.
	 **/
	public static void setWindow(JFrame window)
	{
		//Dispatcher.window = window;
	}

	/**
	 *	Gets the main window.
	 *
	 *	If it hasn't been set, it tries to find the top-level window containing
	 *	the editor.
	 **/
	public static JFrame getWindow()
	{
		/*if (window == null)
		{
			// Try walking the component hierarchy to see if we can reach a top-level
			// window.
			Container c = editorPanel;
			while (c != null && !(c instanceof JFrame))
				c = c.getParent();
			if (c != null)
				window = (JFrame)c;
		}
		return window;*/

		if (editorPanel != null) {
			Container cont = editorPanel.getTopLevelAncestor();
			if (cont instanceof JFrame)
				return (JFrame)cont;
		}

		return null;
	}

	/**
	 *	Gets the EditorPanel for the code editor.
	 **/
	public static EditorPanel getEditorPanel()
	{
		return editorPanel;
	}

	/**
	 *	Sets the EditorPanel for the code editor.
	 **/
	public static void setEditorPanel(EditorPanel panel)
	{
		editorPanel = panel;
		hookMainWindow();
	}
	
	/**
	 * Adds our listener to the window with plugin (either Arena window or
	 * our out-of-the-arena window).
	 */
	public static void hookMainWindow()
	{
		// We need to catch operations on main window to make auto file synchronization possible
		JFrame wind = getWindow();
		if (wind == null) {
			editorPanel.addHierarchyListener(getGlobalDispatcher());
		}
		else {
			wind.addWindowListener(getGlobalDispatcher());
		}
	}

	/**
	 *	Gets the CodePane for the code editor.
	 **/
	public static CodePane getCodePane()
	{
		return editorPanel.getCodePane();
	}

	/**
	 *	Gets the EditorPanel for the Local Code tab.
	 **/
	public static EditorPanel getLocalCodeEditorPanel()
	{
		return localCodeEditorPanel;
	}

	/**
	 *	Sets the EditorPanel for the Local Code tab.
	 **/
	public static void setLocalCodeEditorPanel(EditorPanel panel)
	{
		localCodeEditorPanel = panel;
	}

	/**
	 *	Gets the CodePane for the Local Code tab.
	 **/
	public static CodePane getLocalCodePane()
	{
		return localCodeEditorPanel.getCodePane();
	}

	/**
	 *	Gets the EditorPanel for the Testing Code editor.
	 **/
	public static EditorPanel getTestEditorPanel()
	{
		return testEditorPanel;
	}

	/**
	 *	Sets the EditorPanel for the Testing Code editor.
	 **/
	public static void setTestEditorPanel(EditorPanel panel)
	{
		testEditorPanel = panel;
	}

	/**
	 *	Gets the CodePane for the Testing Code editor.
	 **/
	public static CodePane getTestCodePane()
	{
		return testEditorPanel.getCodePane();
	}

	/**
	 *	Returns the component that displays stuff printed from test runs.
	 **/
	public static SimpleOutputComponent getOutputComponent()
	{
		return outputComp;
	}

	/**
	 *	Sets the component that displays stuff printed from test runs.
	 **/
	public static void setOutputComponent(SimpleOutputComponent comp)
	{
		outputComp = comp;
	}
	
	/**
	 *	Returns the component that displays log messages.
	 **/
	public static SimpleOutputComponent getLogComponent()
	{
		return logComp;
	}

	/**
	 *	Sets the component that displays log messages.
	 **/
	public static void setLogComponent(SimpleOutputComponent comp)
	{
		logComp = comp;
	}
	
	/**
	 * Prints a line to the log component (If it exists)
	 **/
	public static void logln(String ln)
	{
	    if (logComp != null) {
	        long t = System.currentTimeMillis();
	        if (lastLogTime != 0 && lastLogTime + 10000 < t) {
                char[] chars = new char[maxLogMessageWidth];
                Arrays.fill(chars, '-');
	            logComp.println(new String(chars));
	        }
	        lastLogTime = t;
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            String s = sdf.format(new Date(t)) + " : " + ln;
            maxLogMessageWidth = Math.max(maxLogMessageWidth, s.length());
	        logComp.println(s);
	    }
	}
	
	/**
	 *	Returns the component that displays compile output.
	 **/
	public static SimpleOutputComponent getCompileComponent()
	{
		return compileComp;
	}

	/**
	 *	Sets the component that displays compile output.
	 **/
	public static void setCompileComponent(SimpleOutputComponent comp)
	{
		compileComp = comp;
	}

	/**
	 *	Returns the JTabbedPane that all the major stuff is on.
	 **/
	public static JTabbedPane getTabbedPane()
	{
		return tabs;
	}

	/**
	 *	Sets the JTabbedPane that all the major stuff is on.
	 **/
	public static void setTabbedPane(JTabbedPane tabbedPane)
	{
		tabs = tabbedPane;
	}

	/**
	 *	Sets the problem timer control that's normally on the left side of the
	 *	window.
	 **/
	public static void setProblemTimer(ProblemTimer problemTimer)
	{
		timer = problemTimer;
	}

	/**
	 *	Returns the problem timer.
	 **/
	public static ProblemTimer getProblemTimer()
	{
		return timer;
	}

	/**
	 *	Sets the EditorPanel for the template editor.
	 **/
	public static void setTemplateEditor(EditorPanel editor)
	{
		templateEditorPanel = editor;
	}

	/**
	 *	Returns the EditorPanel for the template editor.
	 **/
	public static EditorPanel getTemplateEditor()
	{
		return templateEditorPanel;
	}

	/**
	 *	Returns the CodePane for the template editor.
	 **/
	public static CodePane getTemplateCodePane()
	{
		return templateEditorPanel.getCodePane();
	}

	/**
	 *	Returns a "community JFileChooser instance.
	 **/
	public static JFileChooser getFileChooser()
	{
		if (globalFileChooser == null)
		{
			globalFileChooser = new JFileChooser(PrefFactory.getPrefs().getWorkingDirectory());
		}
		return globalFileChooser;
	}

	/**
	 * Get last time when source code was changed by user.
	 */
	public static long getLastEditTime()
	{
		return getCodePane().getLastEditTime();
	}
	
	/**
	 * Set last time when source code was changed to 0.
	 */
	public static void resetLastEditTime()
	{
		getCodePane().resetLastEditTime();
	}
	
	/**
	 * Mark when the source code was last changed.
	 */
	public static void sourceCodeChanged()
	{
		getCodePane().sourceCodeChanged();
	}

	/**
	 * Set flag showing that source code is edited be KE, not by user.
	 */
	public static void setAutoCodeEditing(boolean value)
	{
		autoCodeEditing = value;
	}

	/**
	 * Check if KE is in process of automatic changing to source code.
	 */
    public static boolean isAutoCodeEditing()
    {
    	return autoCodeEditing;
	}
	
	/**
	 *	Gets the Action instance for the given ActID.
	 **/
	public DefaultAction getAction(ActID actid)
	{
		return getAction(actid, true);
	}

	/**
	 *	Gets the Action instance for the given ActID.
	 *
	 *	There are a few things that might happen here:
	 *	<ol>
	 *		<li>If this is called on the global dispatcher and the ActID
	 *		represents a global action, then the action will just be created if
	 *		one hasn't been made for this ActID yet, or it will just be returned
	 *		if it already exists.</li>
	 *		<li>If this is called on a subdispatcher but the actid is global,
	 *		getAction will be called on the global dispatcher.</li>
	 *		<li>If the ActID isn't a global action, but this was called on the
	 *		global dispatcher, a couple things might happen:
	 *		<ol>
	 *			<li>If useGlobal is false, the dispatcher will search for the
	 *			most relevant subdispatcher that already defines this ActID.</li>
	 *			<li>If useGlobal is true, a global version of the action will be
	 *			created which will similarly route commands as necessary.  This
	 *			is so that things like editor commands could be put on a toolbar
	 *			over the editor.</li>
	 *		</ol>
	 *		</li>
	 *		<li>If this is a local dispatcher and the actid is a local command,
	 *		the dispatcher will first see if it already has an action mapped
	 *		to this ActID, otherwise it will attempt to create one using the
	 *		ActID and a context object.</li>
	 *	</ol>
	 **/
	public DefaultAction getAction(ActID actid, boolean useGlobal)
	{
		if (isGlobal() && actid.isGlobal())
		{
			if (actionMap.containsKey(actid))
				return actionMap.get(actid);
			Class<? extends DefaultAction> cl = actid.actionClass;
			try
			{
				Constructor<? extends DefaultAction> c = cl.getConstructor(ActID.class);
				DefaultAction act = c.newInstance(actid);
				actionMap.put(actid, act);
				return act;
			}
			catch (Throwable t)
			{
			}
			try
			{
				DefaultAction act = cl.newInstance();
				actionMap.put(actid, act);
				return act;
			}
			catch (Throwable t)
			{
			}
		}
		else if (actid.isGlobal())
			return globalDispatcher.getAction(actid);
		else if (isGlobal() && !useGlobal)
		{
			for (Dispatcher d : contextualDispatchers)
				if (d.actionMap.containsKey(actid))
					return d.getAction(actid);
		}
		else if (isGlobal() && useGlobal)
		{
			if (!actionMap.containsKey(actid))
				actionMap.put(actid, new GlobalActionAdapter(actid));
			return actionMap.get(actid);
		}
		else
		{
			if (actionMap.containsKey(actid))
				return actionMap.get(actid);
			Class<? extends DefaultAction> cl = actid.actionClass;
			try
			{
				Constructor<? extends DefaultAction> c = cl.getConstructor(ActID.class, context.getClass());
				DefaultAction act = c.newInstance(actid, context);
				actionMap.put(actid, act);
				context.addFocusListener(act);
				return act;
			}
			catch (Throwable t)
			{
				t.printStackTrace();
			}
			try
			{
				DefaultAction act = cl.newInstance();
				actionMap.put(actid, act);
				context.addFocusListener(act);
				return act;
			}
			catch (Throwable t)
			{
			}
		}
		return null;
	}

	/**
	 *	Launches an event on the given actid (i.e. - does what would happen if
	 *	you clicked on the button or something like that)
	 **/
	public void runCommand(ActID actid)
	{
		DefaultAction action = getAction(actid);
		if (action.isEnabled()) {
			action.actionPerformed(new ActionEvent(this, actid.ordinal(), actid.toString(), System.currentTimeMillis(), 0));
		} else {
			reportError(new Exception(actid + " is currently disabled."));
		}
	}

	/**
	 *	Brings up a message dialog to notify the user about an error, and also
	 *	prints the stack trace of the error.
	 **/
	public static void reportError(Throwable t)
	{
		try
		{
			JOptionPane.showMessageDialog(getWindow(), t, "Command dispatch error", JOptionPane.ERROR_MESSAGE);
		}
		catch (HeadlessException ex)
		{
		}
		t.printStackTrace();
	}

	/**
	 *	Refreshes the settings of all currently mapped actions on this
	 *	dispatcher.  If this is the global dispatcher, also refreshes all
	 *	subdispatchers.
	 **/
	public void UIRefresh()
	{
		for (DefaultAction action : actionMap.values())
			action.UIRefresh();
		if (global)
		{
			for (Dispatcher d : contextualDispatchers)
				d.UIRefresh();
		}
	}
	
	/**
	 *	Notifies the global dispatcher that a new "context" has gotten focus.
	 **/
	public void focusGained(FocusEvent e)
	{
		if (global)
			for (int i=0; i<contextualDispatchers.size(); i++)
				if (contextualDispatchers.get(i).context == e.getSource())
				{
					contextualDispatchers.add(0, contextualDispatchers.remove(i));
					break;
				}
	}

	/**
	 *   Enables/Disables file sync, intended for temporary operations in which
	 *   you do not want local files or source code to be suddenly rewritten.
	 **/
	public static void setFileSyncEnabled(boolean en)
	{
	    fileSyncEnabled = en;
	}
	
	/**
	 *  Allows classes to request file sync using the dispatcher.
	 **/
	public static void requestFileSync() {
	    if (fileSyncEnabled) {
	        LocalTestAction.requestFileSync();
	    }
	}

	
	/**
	 *	Does nothing - part of the FocusListener interface.
	 **/
	public void focusLost(FocusEvent e) {}

	public void windowActivated(WindowEvent e) {
		requestFileSync();
		getCodePane().requestFocusInWindow();
	}

	public void windowDeactivated(WindowEvent e) {
		requestFileSync();
	}
	
	public void windowClosed(WindowEvent e) {}

	public void windowClosing(WindowEvent e) {}

	public void windowDeiconified(WindowEvent e) {}

	public void windowIconified(WindowEvent e) {}

	public void windowOpened(WindowEvent e) {}
	
	/**
	 * Part of HierarchyListener interface. Used to catch the moment of showing up
	 * of newly set up problem.
	 * 
	 * @param e		Event fired
	 */
	public void hierarchyChanged(HierarchyEvent e) {
		if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && editorPanel.isShowing()) {
			JFrame wind = Dispatcher.getWindow();
			if (wind != null) {
				wind.removeWindowListener(this);
				wind.addWindowListener(this);
				editorPanel.removeHierarchyListener(this);
				
				windowActivated(null);
			}
		}
	}
}
