package kawigi.cmd;

/**
 *	A set of global values to store the state of snippets.
 *	
 *	Instead of having all static fields, perhaps there should be an instance of
 *	this as an object associated with each CodePane, since snippet actions
 *	require one as a context anyways.  However, there isn't any particular
 *	advantage to that, especially since the use of all these fields by default
 *	is modal.
 **/
public class SnippetContext
{
	/**
	 *	Current category in the "Category" text box on the snippet dialog.
	 **/
	private static String category = "";
	/**
	 *	Current snippet name in the "Snippet Name" text box on the snippet
	 *	dialog.
	 *	
	 *	This is usually set to some default before launching the dialog.
	 **/
	private static String snippetName;
	/**
	 *	Stores the code to be inserted when running a snippet.
	 *	
	 *	Note: This is not the code to be saved as a snippet, that isn't stored
	 *	at all (it's just the current selection in a CodePane).  This is the
	 *	code to be inserted when actInsertSnippet is run.
	 *	
	 *	Explanation: When you pick a snippet from your snippet database, that
	 *	snippet saves its code to this variable, and then actInsertSnippet is
	 *	executed through the dispatcher.  The code is kept around, and while
	 *	actInsertSnippet technically shouldn't be called any other way, if it
	 *	were run in another way afterwards, it would insert the last-used
	 *	snippet.
	 **/
	private static String code;
	
	/**
	 *	Returns the current category from the snippet dialog.
	 **/
	public static String getCategory()
	{
		return category;
	}
	
	/**
	 *	Returns the current snippet name from the snippet dialog.
	 **/
	public static String getSnippetName()
	{
		return snippetName;
	}
	
	/**
	 *	Sets the category from the snippet dialog.
	 *	
	 *	Perhaps "Updates" would be a better verb, since this is usually set when
	 *	the user types in the text box.
	 **/
	public static void setCategory(String s)
	{
		category = s;
	}
	
	/**
	 *	Sets the snippet name from the snippet dialog.
	 *	
	 *	This generally called when the user types into the text box, or when
	 *	KawigiEdit guesses the name of the snippet before the dialog is shown.
	 **/
	public static void setSnippetName(String s)
	{
		snippetName = s;
	}
	
	/**
	 *	Returns the code that should be inserted by actInsertSnippet.
	 **/
	public static String getInsertCode()
	{
		return code;
	}
	
	/**
	 *	Sets the code that should be inserted by actInsertSnippet.  This is set
	 *	whenever you use a snippet.
	 **/
	public static void setInsertCode(String s)
	{
		code = s;
	}
}
