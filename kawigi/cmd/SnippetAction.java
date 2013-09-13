package kawigi.cmd;
import kawigi.editor.*;
import kawigi.properties.*;
import kawigi.widget.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

/**
 *	This class executes all snippet-related commands, including using a snippet,
 *	launching the snippet dialog and all the controls on the snippet dialog.
 **/
@SuppressWarnings("serial")
public class SnippetAction extends DefaultAction
{
	/**
	 *	The CodePane that this action should act on.
	 **/
	protected CodePane context;
	
	/**
	 *	The global reference to the "Add Snippet" dialog, if it has been
	 *	created.
	 **/
	protected static JDialog addSnippetDialog;
	
	/**
	 *	Constructs a new SnippetAction for the given ActID to act on the given
	 *	CodePane.
	 **/
	public SnippetAction(ActID cmdid, CodePane context)
	{
		super(cmdid);
		this.context = context;
	}
	
	/**
	 *	Returns true if this command should be enabled.
	 **/
	public boolean isEnabled()
	{
		switch (cmdid)
		{
			case actAddSnippet:
				if (SnippetContext.getSnippetName() == null || SnippetContext.getSnippetName().length() == 0)
					return false;
				// fallthrough:
			case actSnippetCategory:
			case actSnippetName:
			case actAddSnippetDlg:
				return context.getSelectedText() != null;
			case actInsertSnippet:
				return SnippetContext.getInsertCode() != null;
			case actCancelSnippet:
				return addSnippetDialog != null && addSnippetDialog.isVisible();
		}
		return true;
	}
	
	/**
	 *	Executes this action.
	 **/
	public void actionPerformed(ActionEvent e)
	{
		switch (cmdid)
		{
			case actAddSnippet:
			case actSnippetCategory:
			case actSnippetName:
				// If someone hits enter in either text box or hits the "Add
				// Snippet" button, we commit the snippet.
				
				// Make sure we have at least a snippet name and code (category
				// is technically optional.
				if (SnippetContext.getSnippetName() != null && SnippetContext.getSnippetName().length() > 0 && context.getSelectedText() != null)
				{
					Snippet newSnippet = new Snippet(context.getSelectedText(), SnippetContext.getSnippetName());
					String category = SnippetContext.getCategory();
					Category rootCategory = (Category)UIHandler.loadMenu(MenuID.RootSnippetCategory, context.getDispatcher());
					// This add will recursively traverse (or create) categories
					// to fit this in the right place.
					rootCategory.add(newSnippet, category);
					// Now we need to save the snippets to disk.  This is done
					// through the UI customization framework described in
					// MenuID.java.  Basically, we see if there is a custom
					// .ui file set for the snippet menu, and if there isn't,
					// we prompt the user for where to save their snippets.
					// Then, we create that file and set KawigiEdit to load the
					// snippet menu from that file instead of using the default
					// one, which is just an empty c\root category.
					PrefProxy prefs = PrefFactory.getPrefs();
					if (prefs.getProperty("kawigi.ui.RootSnippetCategory") == null)
					{
						JFileChooser fileChooser = new JFileChooser(prefs.getWorkingDirectory());
						fileChooser.setDialogTitle("Where should your Snippets be saved?");
						fileChooser.setSelectedFile(new File(prefs.getWorkingDirectory(), "Snippets.ui"));
						if (fileChooser.showDialog(Dispatcher.getWindow(), "Save Snippets") == JFileChooser.APPROVE_OPTION)
						{
							File file = fileChooser.getSelectedFile();
							prefs.setProperty("kawigi.ui.RootSnippetCategory", file.getAbsolutePath());
							prefs.commit();
						}
					}
					if (prefs.getProperty("kawigi.ui.RootSnippetCategory") != null)
					{
						try
						{
							PrintWriter out = new PrintWriter(new File(prefs.getProperty("kawigi.ui.RootSnippetCategory")));
							rootCategory.write(out, "");
							out.close();
						}
						catch (IOException ex)
						{
							reportError(new Exception("Error Writing Snippets: " + ex.toString()), true);
						}
					}
				}
				// else silent ignore.
				// fallthrough, to close the dialog:
			case actCancelSnippet:
				if (addSnippetDialog != null && addSnippetDialog.isVisible())
				{
					addSnippetDialog.setVisible(false);
					addSnippetDialog.dispose();
					addSnippetDialog = null;
				}
				break;
			case actInsertSnippet:
				context.replaceSelection(SnippetContext.getInsertCode());
				break;
			case actAddSnippetDlg:
				// Look for a guess for the name of the snippet
				String code = context.getSelectedText();
				if (code != null)
				{
					if (code.indexOf('(') >= 0)
					{
						String name = code.substring(0, code.indexOf('(')).trim();
						if (name.indexOf('\n') >= 0)
							name = name.substring(name.lastIndexOf('\n')+1).trim();
						if (name.indexOf(' ') >= 0)
							name = name.substring(name.lastIndexOf(' ')+1).trim();
						SnippetContext.setSnippetName(name);
					}
					else
						SnippetContext.setSnippetName("");
					addSnippetDialog = (JDialog)UIHandler.loadMenu(MenuID.SnippetDialog, context.getDispatcher());
					addSnippetDialog.pack();
					addSnippetDialog.setVisible(true);
				}
				break;
		}
	}
	
	/**
	 *	Overridden to return the right values for the TEXT property.
	 **/
	public Object getValue(String key)
	{
		if (key.equals(TEXT))
		{
			if (cmdid == ActID.actSnippetName)
				return SnippetContext.getSnippetName();
			else if (cmdid == ActID.actSnippetCategory)
				return SnippetContext.getCategory();
		}
		return super.getValue(key);
	}
	
	/**
	 *	Overridden to properly save the TEXT property.
	 **/
	public void putValue(String key, Object value)
	{
		if (key.equals(TEXT))
		{
			if (cmdid == ActID.actSnippetName)
				SnippetContext.setSnippetName((String)value);
			else if (cmdid == ActID.actSnippetCategory)
				SnippetContext.setCategory((String)value);
		}
		super.putValue(key, value);
	}
}
