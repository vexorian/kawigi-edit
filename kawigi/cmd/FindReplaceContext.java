package kawigi.cmd;
import kawigi.properties.*;
import java.util.regex.*;
import javax.swing.*;

/**
 *	An object held by a CodePane that holds state used by Find/Replace Actions.
 **/
public class FindReplaceContext
{
	/**
	 *	Enum represnting the type of search string given.
	 *	
	 *	I was about to declare some final static ints, and then I thought
	 *	"Wait - That's what enums are really for!"
	 **/
	private static enum SearchType
	{
		/**
		 *	Search for the literal value of the search string.
		 **/
		Literal,
		/**
		 *	Search for the literal value of the search string, but allow * to
		 *	represent zero or more characters and ? to represent any one
		 *	character.
		 **/
		Wildcards,
		/**
		 *	Search for a regular expression match, using the syntax from the
		 *	Java regex specification (see javadocs for java.util.regex.Pattern).
		 **/
		Regex;
	}
	
	// Private variable stuff :-)
	private boolean wholeWord, caseSensitive;
	private String findString, replaceString;
	private boolean showingReplace;
	private SearchType state;
	private JDialog dialog;
	private Dispatcher disp;
	
	/**
	 *	Constructs a new FindReplaceContext which operates on commands in the
	 *	given subdispatcher.
	 **/
	public FindReplaceContext(Dispatcher disp)
	{
		this.disp = disp;
		PrefProxy prefs = PrefFactory.getPrefs();
		wholeWord = prefs.getBoolean("kawigi.find.wholeword", false);
		caseSensitive = prefs.getBoolean("kawigi.find.casesensitive", false);
		findString = "";
		replaceString = "";	
		showingReplace = false;
		state = Enum.valueOf(SearchType.class, prefs.getProperty("kawigi.find.searchtype", "Literal"));
		if (state == null)
			state = SearchType.Literal;
	}
	
	/**
	 *	Returns the current search string.
	 **/
	public String getSearchString()
	{
		return findString;
	}
	
	/**
	 *	Returns the current replacement string.
	 **/
	public String getReplacement()
	{
		return replaceString;
	}
	
	/**
	 *	Sets the current search string.
	 **/
	public void setSearchString(String s)
	{
		findString = s;
	}
	
	/**
	 *	Sets the current replacement string.
	 **/
	public void setReplacement(String s)
	{
		replaceString = s;
	}
	
	/**
	 *	Gets the Find/Replace dialog and shows it.
	 *	
	 *	Shows the Replace dialog if replace is true, otherwise shows the Find
	 *	dialog.
	 **/
	public void showing(boolean replace)
	{
		showingReplace = replace;
		JDialog dialog = getDialog();
		dialog.setVisible(true);
		Dispatcher.getGlobalDispatcher().UIRefresh();
		dialog.pack();
	}
	
	/**
	 *	Returns true if the replace options should be showing.
	 **/
	public boolean isShowingReplace()
	{
		return showingReplace;
	}
	
	/**
	 *	Toggles the "Whole Word" option.
	 **/
	public void toggleWholeWord()
	{
		wholeWord = !wholeWord;
	}
	
	/**
	 *	Toggles the "Case Sensitive" option.
	 **/
	public void toggleCaseSensitive()
	{
		caseSensitive = !caseSensitive;
	}
	
	/**
	 *	Returns true if the "Whole Word" option is on.
	 **/
	public boolean getWholeWord()
	{
		return wholeWord;
	}
	
	/**
	 *	Returns true if the "Case Sensitive" option is on.
	 **/
	public boolean getCaseSensitive()
	{
		return caseSensitive;
	}
	
	/**
	 *	Returns true if literal string matching should be used.
	 **/
	public boolean useLiteral()
	{
		return state == SearchType.Literal;
	}
	
	/**
	 *	Returns true if wildcard matching should be used.
	 **/
	public boolean useWildcards()
	{
		return state == SearchType.Wildcards;
	}
	
	/**
	 *	Returns true if regular expression matching should be used.
	 **/
	public boolean useRegex()
	{
		return state == SearchType.Regex;
	}
	
	/**
	 *	Sets the search mode to literal string matching.
	 **/
	public void setUseLiteral()
	{
		state = SearchType.Literal;
	}
	
	/**
	 *	Sets the search mode to use wildcards.
	 **/
	public void setUseWildcards()
	{
		state = SearchType.Wildcards;
	}
	
	/**
	 *	Sets the search mode to regular expressions.
	 **/
	public void setUseRegex()
	{
		state = SearchType.Regex;
	}
	
	/**
	 *	Gets a regular expression pattern representing the current search
	 *	string.
	 *	
	 *	This method is the real magic behind KawigiEdit's search and replace.
	 *	Regardless of the specified search type, all searching is done with
	 *	regular expressions underneath.
	 *	
	 *	If the search type <i>isn't</i> regex, the search pattern is
	 *	appropriately escaped (see the escapePattern method).  If the search
	 *	type is wildcard, all the *'s in the escaped pattern are converted to
	 *	".*" and all the ?'s in the escaped pattern are converted to ".".  Of
	 *	course, since ? and * are both control characters in regular expressions
	 *	as well, we have to search for \* and \? to do this munging.
	 *	
	 *	Conveniently, case sensitivity can be set as a flag when compiling the
	 *	pattern.
	 *	
	 *	"Whole Word" is implemented by adding \b (word boundary matcher) to the
	 *	beginning and end of the final resulting pattern.
	 **/
	public Pattern getCurrentPattern()
	{
		String pattern = findString;
		int flags = Pattern.DOTALL;
		if (state != SearchType.Regex)
		{
			pattern = escapePattern(pattern);
			
			//make "*" .* and "?" .
			if (state == SearchType.Wildcards)
			{
				pattern = pattern.replaceAll("\\\\\\*", ".*");
				pattern = pattern.replaceAll("\\\\\\?", ".");
			}
		}
		if (!caseSensitive)
			flags |= Pattern.CASE_INSENSITIVE;
		if (wholeWord)
			pattern = "\\b" + pattern + "\\b";
		return Pattern.compile(pattern, flags);
	}
	
	/**
	 *	Takes a search string that isn't supposed to be used as a regular
	 *	expression and makes it regular-expression-able by escaping characters
	 *	that would otherwise have an unwanted meaning in a regular expression
	 *	(or worse, make the regular expression not compile, in which case we'd
	 *	be hosed).
	 *	
	 *	Characters that are currently escaped by this method are \, [, ], ^, $,
	 *	&, |, (, ), ., *, +, ?, { and }.
	 **/
	public String escapePattern(String pattern)
	{
		String newpattern = "";
		//'quote' the pattern
		for (int i=0; i<pattern.length(); i++)
		{
			if ("\\[]^$&|().*+?{}".indexOf(pattern.charAt(i)) >= 0)
				newpattern += '\\';
			newpattern += pattern.charAt(i);
		}
		return newpattern;
	}
	
	/**
	 *	Returns true if the find/replace dialog has been created and is showing.
	 **/
	public boolean dialogShowing()
	{
		return dialog != null && dialog.isVisible();
	}
	
	/**
	 *	Returns the Find/Replace dialog.
	 *	
	 *	If the dialog hasn't been created yet, creates the dialog.
	 **/
	public JDialog getDialog()
	{
		if (dialog == null)
		{
			dialog = (JDialog)UIHandler.loadMenu(MenuID.FindReplaceDialog, disp);
		}
		return dialog;
	}
}
