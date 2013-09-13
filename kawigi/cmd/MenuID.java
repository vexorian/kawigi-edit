package kawigi.cmd;
import kawigi.properties.*;
import java.io.*;

/**
 *	Enumeration corresponding to .ui files in the rc/ui directory.
 *	
 *	The .ui files are XML representations of a graphical user interface that are
 *	parsed and instantiated by the UIHandler class.
 *	
 *	Each ui file <i>can</i> be customized - open the contestapplet.conf file (in
 *	Windows XP, this is usually in C:\Documents and Settings\<username>\), add
 *	a line that says something like:
 *	<pre>
 *	kawigi.ui.[a name from this enum]=[full file path to .ui file]
 *	</pre>
 *	Then that file will be parsed instead of the one from the KawigiEdit jar for
 *	that UI element.  This way you can customize any UI in KawigiEdit if you
 *	want.  Perhaps in a future version of KawigiEdit, there will be a way to do
 *	this through the UI, as well as some other options (like no text on buttons,
 *	or large icons, which are there but aren't used yet).
 **/
public enum MenuID
{
	/**
	 *	The Find/Replace dialog
	 **/
	FindReplaceDialog("FindReplaceDlg.ui"),
	/**
	 *	The window for KawigiEditStandalone.
	 **/
	StandaloneFrame("Standalone.ui"),
	/**
	 *	The main KawigiEdit panel shown in the applet.
	 **/
	PluginPanel("Plugin.ui"),
	/**
	 *	Panel on the configuration dialog.
	 **/
	ConfigPanel("Config.ui"),
	/**
	 *	This is a placeholder for the Snippets menu.
	 *	
	 *	What really happens here is before you add any snippets, this is the
	 *	blank snippet database menu.  When you add a snippet, you are prompted
	 *	to save your snippets and kawigi.ui.RootSnippetCategory is set to point
	 *	to that file, so that it will be loaded as customized UI in the future.
	 *	From then on, whenever you add a snippet, the snippet UI file is resaved
	 *	in that location.  The nice thing about this system is that I only have
	 *	to write the saving code, and the loading is taken care of by the
	 *	UIHandler.  Also, relative to KawigiEdit 1, this format is nice because
	 *	it can be edited in notepad or any other editor (if you're careful to
	 *	properly escape XML control characters, like &lt;, &gt;, &quot; and &amp;.
	 **/
	RootSnippetCategory("Snippets.ui"),
	/**
	 *	The right click menu for the editor.
	 **/
	EditorContextMenu("CtxMenu.ui"),
	/**
	 *	The dialog for adding snippets.
	 **/
	SnippetDialog("SnippetDialog.ui");
	
	/**
	 *	The name of the file of the default .ui file in rc/ui.
	 **/
	public String filename;
	
	/**
	 *	Constructs the instances of MenuID.
	 **/
	MenuID(String file)
	{
		filename = file;
	}
	
	/**
	 *	Returns the name of the file that stores the customized version of this
	 *	ui file.
	 **/
	public String getCustomUI()
	{
		return PrefFactory.getPrefs().getProperty("kawigi.ui." + toString());
	}
	
	/**
	 *	Returns an InputStream which either comes from a file if this UI is
	 *	customized or from the jar (or classpath) otherwise.
	 **/
	public InputStream getXMLStream()
	{
		String custom = getCustomUI();
		if (custom != null)
			try
			{
				return new FileInputStream(custom);
			}
			catch (FileNotFoundException ex)
			{
			}
		return getClass().getClassLoader().getResourceAsStream("rc/ui/" + filename);
	}
}
