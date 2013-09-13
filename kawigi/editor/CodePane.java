package kawigi.editor;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.EditorKit;
import javax.swing.text.Keymap;
import javax.swing.text.View;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import kawigi.cmd.*;
import kawigi.properties.PrefFactory;
import kawigi.properties.PrefProxy;

/**
 *	This class was originally just a hack on JTextPane to stop it from wrapping
 *	lines, as there is no setting or method to do this normally.
 *
 *	Then I needed it to override several classes to use my View classes (which
 *	allow me to do syntax highlighting).
 *
 *	Since then, it's become a much more core piece of KawigiEdit - and then it
 *	was reduced again to be somewhere more in the middle of several other
 *	components.
 **/
@SuppressWarnings("serial")
public class CodePane extends JTextPane implements MouseListener, DocumentListener
{
	private UndoManager undo;
	private Dispatcher subdispatcher;
	private FindReplaceContext findContext;

	/**
	 * Last time when source code was edited
	 */
	private long lastEditTime;
	
	/**
	 *	Creates a new <code>NoWrapJTextPane</code> and sets it to use various
	 *	versions of my EditorKit implementation based on a few mime types.
	 *
	 *	The special mime types it will now know are text/Java, text/C++,
	 *	text/C#, text/VB.
	 **/
	public CodePane()
	{
		//This is a convenient way to do this :-)
		doContentType("java", JavaView.class);
		doContentType("cpp", CPPView.class);
		doContentType("csharp", CSharpView.class);
		doContentType("vb", VBView.class);
        doContentType("py", PythonView.class);
		undo = new UndoManager();
		getStyledDocument().addUndoableEditListener(undo);
		addMouseListener(this);
		setDragEnabled(true);

		resetPrefs();

		Keymap defaultKeymap = getKeymap();
		subdispatcher = Dispatcher.getGlobalDispatcher().createSubDispatcher(this);
		findContext = new FindReplaceContext(subdispatcher);
		Keymap myKeymap = new KawigiEditKeyMap(subdispatcher);
		myKeymap.setResolveParent(defaultKeymap);
		setKeymap(myKeymap);
		ActionMap actionMap = getActionMap();
		InputMap inputMap = getInputMap();
		KeyStroke[] keystrokes = myKeymap.getBoundKeyStrokes();
		for (int i=0; i<keystrokes.length; i++)
			if ((keystrokes[i].getModifiers() & (InputEvent.CTRL_MASK | InputEvent.ALT_MASK)) != 0)
			{
				DefaultAction act = (DefaultAction)myKeymap.getAction(keystrokes[i]);
				inputMap.put(keystrokes[i], act.getID().toString());
				actionMap.put(act.getID().toString(), act);
			}
		subdispatcher.getAction(ActID.actInsertSnippet);
	}

	/**
	 *	Notifies the CodePane that preferences might have changed that affect it.
	 **/
	public void resetPrefs()
	{
		PrefProxy prefs = PrefFactory.getPrefs();
		setBackground(prefs.getColor("kawigi.editor.background", Color.black));
		setForeground(prefs.getColor("kawigi.editor.foreground", Color.white));
		setCaretColor(prefs.getColor("kawigi.editor.foreground", Color.white));
		setFont(prefs.getFont("kawigi.editor.font", new Font("Monospaced", Font.PLAIN, 12)));
		setSelectionColor(prefs.getColor("kawigi.editor.SelectionColor", new Color(204, 204, 255)));
		setSelectedTextColor(prefs.getColor("kawigi.editor.SelectedTextColor", Color.black));
	}

	/**
	 *	Given an ExtendedLanguage, creates an EditorKit that uses the
	 *	appropriate view class and sets it to use that editor kit for the mime
	 *	type associated with that lanugage (i.e. - text/<language name>).
	 **/
	private void doContentType(String languageName, Class<? extends View> viewClass)
	{
		setEditorKitForContentType("text/" + languageName, new ConfigurableEditorKit(viewClass));
	}

	/**
	 *	It's brutally annoying that I have to write this, and sometimes it
	 *	doesn't even quite work (I haven't had particular problems with it for
	 *	a while now, though..
	 **/
	public boolean getScrollableTracksViewportWidth()
	{
		return getParent() instanceof JViewport && ((JViewport)getParent()).getWidth() >= getUI().getPreferredSize(this).width;	//all this to just not wrap?  Why isn't there some good way to set this already?
	}

	/**
	 *	Tweak of the default block scolling increment.
	 *
	 *	It <strong>should</strong> scroll by the height (or width) minus the
	 *	font size. This makes wheel-scrolling a much nicer experience than it is
	 *	by default.
	 **/
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
	{
		return super.getScrollableBlockIncrement(visibleRect, orientation, direction)-getFont().getSize();
	}

	/**
	 *	Tweak of the default unit scrolling increment.
	 *
	 *	The default seemed to scroll one pixel at a time, which I found rather
	 *	useless.  It took way too long to scroll a few lines.  So now it scrolls
	 *	roughly 1 1/2 lines at a time.
	 *
	 *	I mentioned this plugin to NeverMore once, and he drilled me with
	 *	questions about it.  Then he got ready to ask me something that I could
	 *	tell was important to him that he was missing in PopsEdit - and it was
	 *	wheel scrolling.  People get attached to their mouse wheels, what can I
	 *	say?  I said it worked and he was ecstatic.  Not only that, but the
	 *	scrolling increment had been tweaked to be "just right", since the
	 *	latest version was the one where I added this tweak.  It was funny to
	 *	see him so impressed, I wasn't aware that PopsEdit had wheel scrolling
	 *	turned off.
	 **/
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
	{
		return 3*getFont().getSize()/2;
	}

	/**
	 *	By default, use my GenericView to render the text.
	 *
	 *  This will be changed whenever someone looks at the problem in a specific
	 *	language. If you're experimenting with views, you can change the default
	 *	view here for when you run it standalone.
	 **/
	protected EditorKit createDefaultEditorKit()
	{
		//Hey!  Use my EditorKit!
		return new ConfigurableEditorKit(GenericView.class);
	}

	/**
	 *	Undoes the last edit.
	 **/
	public void undo()
	{
		try
		{
			undo.undo();
		}
		catch (CannotUndoException ex)
		{
		}
	}

	/**
	 *	Redoes the last undone edit.
	 **/
	public void redo()
	{
		try
		{
			undo.redo();
		}
		catch (CannotRedoException ex)
		{
		}
	}

	/**
	 *	Listens to mouse events on the text pane, so it knows when to create the
	 *	popup menu.
	 **/
	public void mousePressed(MouseEvent e)
	{
		if (e.isPopupTrigger())
		{
			requestFocusInWindow();
			JPopupMenu popup = (JPopupMenu)UIHandler.loadMenu(MenuID.EditorContextMenu, subdispatcher);
			popup.show(this, e.getX(), e.getY());
		}
	}

	/**
	 *	Listens to mouse events on the text pane, so it knows when to create the
	 *	popup menu.
	 **/
	public void mouseReleased(MouseEvent e)
	{
		if (e.isPopupTrigger())
		{
			requestFocusInWindow();
			JPopupMenu popup = (JPopupMenu)UIHandler.loadMenu(MenuID.EditorContextMenu, subdispatcher);
			popup.show(this, e.getX(), e.getY());
		}
	}

	/**
	 *	Required by the MouseListener interface.
	 **/
	public void mouseClicked(MouseEvent e){}

	/**
	 *	Required by the MouseListener interface.
	 **/
	public void mouseEntered(MouseEvent e){}

	/**
	 *	Required by the MouseListener interface.
	 **/
	public void mouseExited(MouseEvent e){}

	/**
	 *	This has to be called whenever the Document changes.
	 **/
	public void readdUndoListener()
	{
		undo.discardAllEdits();
		// Make a trick to be sure that UndoManager is added as listener but
		// not to add it twice or more
		getStyledDocument().removeUndoableEditListener(undo);
		getStyledDocument().addUndoableEditListener(undo);

		// We need to know when last edit on source was made
		getStyledDocument().removeDocumentListener(this);
		getStyledDocument().addDocumentListener(this);
	}

	/**
	 *	Returns true if there are undoable edits in the undo stack.
	 **/
	public boolean canUndo()
	{
		return undo.canUndo();
	}

	/**
	 *	Returns true if there are redoable edits that have been undone.
	 **/
	public boolean canRedo()
	{
		return undo.canRedo();
	}

	/**
	 *	Returns the object handling state for Find/Replace commands on this
	 *	CodePane.
	 **/
	public FindReplaceContext getFindReplaceContext()
	{
		return findContext;
	}

	/**
	 *	Returns the local dispatcher for commands that act on this CodePane.
	 **/
	public Dispatcher getDispatcher()
	{
		return subdispatcher;
	}

	/**
	 * Part of DocumentListener interface. Fires when source code is changed.
	 */
	public void changedUpdate(DocumentEvent e) {
		sourceCodeChanged();
	}

	/**
	 * Part of DocumentListener interface. Fires when source code is changed.
	 */
	public void insertUpdate(DocumentEvent e) {
		sourceCodeChanged();
	}

	/**
	 * Part of DocumentListener interface. Fires when source code is changed.
	 */
	public void removeUpdate(DocumentEvent e) {
		sourceCodeChanged();
	}
	
	/**
	 * Mark when the source code was last changed. This is possible due to DocumentListener
	 * interface. This listener is assigned readdUndoListener() method. 
	 */
	public void sourceCodeChanged()
	{
		if (!Dispatcher.isAutoCodeEditing()) {
		    lastEditTime = System.currentTimeMillis();
		}
	}
	
	/**
	 * Get last time when source code was changed by user.
	 * 
	 * @return		Last editing time
	 */
	public long getLastEditTime()
	{
		return lastEditTime;
	}
	
	/**
	 * Set last time when source code was changed to 0.
	 */
	public void resetLastEditTime()
	{
		lastEditTime = 0;
	}
}
