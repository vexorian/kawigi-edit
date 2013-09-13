package kawigi.editor;
import kawigi.cmd.*;
import javax.swing.*;
import javax.swing.text.*;
import java.util.*;
import java.awt.event.*;

/**
 *	Keymap implementation used by KawigiEdit's CodePanes.
 *	
 *	If you want to map a keystroke in the editor to some specific action, this
 *	is the place to do that.  Also, I've long had a theory that if I got with
 *	some people and implemented one or two more keymaps to swap with this one,
 *	it would be reasonably feasible to implement a "Vi" mode on KawigiEdit,
 *	which is something I think some people would like.
 **/
public class KawigiEditKeyMap implements Keymap
{
	/**
	 *	Map of keystrokes to KawigiEdit actions.
	 **/
	private Map<KeyStroke,Action> keymap;
	/**
	 *	Action that happens if there is no specific binding.
	 **/
	private Action defaultAction;
	/**
	 *	Parent keymap that we go to if necessary.
	 **/
	private Keymap parent;
	
	/**
	 *	Constructs a KawigiEditKeyMap with the set of actions we bind to
	 *	keystrokes in the editor.
	 *	
	 *	Uses editorSubDispatcher to create all the necessary actions.
	 **/
	public KawigiEditKeyMap(Dispatcher editorSubDispatcher)
	{
		keymap = new HashMap<KeyStroke,Action>();
		defaultAction = null;
		
		// What follows is a list of keystrokes recognized by default by the code editors
		// in KawigiEdit and what they do:
		keymap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK), editorSubDispatcher.getAction(ActID.actCut));
		keymap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.SHIFT_MASK), editorSubDispatcher.getAction(ActID.actCut));
		keymap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK), editorSubDispatcher.getAction(ActID.actCopy));
		keymap.put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, InputEvent.CTRL_MASK), editorSubDispatcher.getAction(ActID.actCopy));
		keymap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK), editorSubDispatcher.getAction(ActID.actPaste));
		keymap.put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, InputEvent.SHIFT_MASK), editorSubDispatcher.getAction(ActID.actPaste));
		keymap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK), editorSubDispatcher.getAction(ActID.actSelectAll));
		keymap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK), editorSubDispatcher.getAction(ActID.actUndo));
		keymap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, InputEvent.ALT_MASK), editorSubDispatcher.getAction(ActID.actUndo));
		keymap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK), editorSubDispatcher.getAction(ActID.actRedo));
		keymap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, InputEvent.ALT_MASK | InputEvent.SHIFT_MASK), editorSubDispatcher.getAction(ActID.actUndo));
		keymap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK), editorSubDispatcher.getAction(ActID.actUndo));
		keymap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_MASK), editorSubDispatcher.getAction(ActID.actScrollUp));
		keymap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_MASK), editorSubDispatcher.getAction(ActID.actScrollDown));
		keymap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.CTRL_MASK), editorSubDispatcher.getAction(ActID.actDeleteNextWord));
		keymap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, InputEvent.CTRL_MASK), editorSubDispatcher.getAction(ActID.actDeletePreviousWord));
		keymap.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), editorSubDispatcher.getAction(ActID.actIndent));
		keymap.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_MASK), editorSubDispatcher.getAction(ActID.actOutdent));
		keymap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), editorSubDispatcher.getAction(ActID.actNewLine));
		keymap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_MASK), editorSubDispatcher.getAction(ActID.actNewLine));
		keymap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK), editorSubDispatcher.getAction(ActID.actFindDlg));
		keymap.put(KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_MASK), editorSubDispatcher.getAction(ActID.actReplaceDlg));
		keymap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), editorSubDispatcher.getAction(ActID.actFindNext));
		keymap.put(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK), editorSubDispatcher.getAction(ActID.actAddSnippetDlg));
		keymap.put(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK), editorSubDispatcher.getAction(ActID.actCtxMenu));
		keymap.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK), editorSubDispatcher.getAction(ActID.actInsertTestCode));
		keymap.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK), editorSubDispatcher.getAction(ActID.actTestCases));
		keymap.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK), editorSubDispatcher.getAction(ActID.actOutsideMode));
	}
	
	/**
	 *	Binds an action to a keystroke.
	 **/
	public void addActionForKeyStroke(KeyStroke key, Action a)
	{
		keymap.put(key, a);
	}
	
	/**
	 *	Returns a bound action for the given KeyStroke.
	 **/
	public Action getAction(KeyStroke key)
	{
		if (keymap.containsKey(key))
			return keymap.get(key);
		else if (parent != null)
			return parent.getAction(key);
		else
			return null;
	}
	
	/**
	 *	Gets all the actions that are bound by this KeyMap.
	 **/
	public Action[] getBoundActions()
	{
		Action[] ret = new Action[keymap.size()];
		Collection<Action> coll = keymap.values();
		coll.toArray(ret);
		return ret;
	}
	
	/**
	 *	Gets all the keystrokes that are bound by this KeyMap.
	 **/
	public KeyStroke[] getBoundKeyStrokes()
	{
		KeyStroke[] ret = new KeyStroke[keymap.size()];
		Set<KeyStroke> set = keymap.keySet();
		set.toArray(ret);
		return ret;
	}
	
	/**
	 *	Returns the default action for unbound keystrokes.
	 **/
	public Action getDefaultAction()
	{
		return defaultAction != null ? defaultAction : parent != null ? parent.getDefaultAction() : null;
	}
	
	/**
	 *	Returns the KeyStrokes that execute the given action.
	 **/
	public KeyStroke[] getKeyStrokesForAction(Action a)
	{
		Collection<KeyStroke> keystrokes = new ArrayList<KeyStroke>();
		for (KeyStroke key : keymap.keySet())
		{
			if (keymap.get(key) == a || a.equals(keymap.get(key)))
				keystrokes.add(key);
		}
		KeyStroke[] ret = new KeyStroke[keystrokes.size()];
		keystrokes.toArray(ret);
		return ret;
	}
	
	/**
	 *	Returns the name of this keymap.
	 **/
	public String getName()
	{
		return "KawigiEdit CodePane Keystrokes";
	}
	
	/**
	 *	Returns the parent of this keymap.
	 **/
	public Keymap getResolveParent() 
	{
		return parent;
	}
	
	/**
	 *	Returns true if a command is bound to the given key and it's currently
	 *	available.
	 **/
	public boolean isLocallyDefined(KeyStroke key)
	{
		return keymap.containsKey(key) && keymap.get(key).isEnabled();
	}
	
	/**
	 *	Clears the bindings in the keymap.
	 **/
	public void removeBindings()
	{
		keymap.clear();
	}
	
	/**
	 *	Clears a specific keystroke binding.
	 **/
	public void removeKeyStrokeBinding(KeyStroke keys)
	{
		keymap.remove(keys);
	}
	
	/**
	 *	Sets the default action.
	 **/
	public void setDefaultAction(Action a)
	{
 		defaultAction = a;
	}
 	
 	/**
 	 *	Sets the parent keymap.
 	 **/
 	public void setResolveParent(Keymap parent)
 	{
 		this.parent = parent;
 	}
}
