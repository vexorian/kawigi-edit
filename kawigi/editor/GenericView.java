package kawigi.editor;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;
import javax.swing.event.DocumentEvent;
import javax.swing.text.*;

import kawigi.properties.PrefFactory;
import kawigi.properties.PrefProxy;

/**
 *	Superclass of all language-specific views in KawigiEdit.
 *
 *	This class handles the custom tab stop, pair matching, has some shared
 *	methods for syntax highlighting, and paints the KawigiEdit watermark in the
 *	background of the editor.
 *
 *	As many people as have complained about the watermark and asked for a way
 *	to remove it, I was surprised when I changed it to blue and people said they
 *	don't like it as much as the old one that used "TopCoder" colors.
 **/
public class GenericView extends PlainView
{
	/**
	 *	The current tab width set.
	 **/
	private static int tabstop;
	/**
	 *	Keys for the pair-matching highlighter.
	 **/
	protected static Object highlightKey1, highlightKey2;
	/**
	 *	Indexes for the pair-matching highlights.
	 **/
	private int hindex1, hindex2;
	/**
	 *	Pair-matching color.
	 **/
	private static Color matchParensColor;
	/**
	 *	true if pair-matching is turned on.
	 **/
	private static boolean matchParens;
	/**
	 *	Colors for various token types.
	 **/
	protected static Color keywordColor, typeColor, operatorColor, classColor, stringColor, commentColor, directiveColor, tagColor;

	static
	{
		resetTabStop();
	}

	/**
	 *	initializes all the syntax colors.
	 **/
	public static void getColors()
	{
		PrefProxy prefs = PrefFactory.getPrefs();
		keywordColor = prefs.getColor("kawigi.editor.KeywordColor", new Color(191, 191, 0));
		typeColor = prefs.getColor("kawigi.editor.TypeColor", new Color(127, 127, 255));
		operatorColor = prefs.getColor("kawigi.editor.OperatorColor", new Color(191, 63, 63));
		classColor = prefs.getColor("kawigi.editor.ClassColor", new Color(191, 63, 191));
		stringColor = prefs.getColor("kawigi.editor.StringColor", new Color(255, 0, 0));
		commentColor = prefs.getColor("kawigi.editor.CommentColor", new Color(127, 255, 127));
		directiveColor = prefs.getColor("kawigi.editor.DirectiveColor", new Color(255, 127, 127));
		tagColor = prefs.getColor("kawigi.editor.TemplateTagColor", new Color(64, 192, 255));
	}

	/**
	 *	Reads a keywords file and initializes the map to appropriately color
	 *	those tokens.
	 **/
	public static void readKeywords(String name, boolean caseSensitive, Map<String,Color> colorHash)
	{
		try
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(GenericView.class.getResource("/rc/" + name + ".words").openStream()));
			String line;
			// Ignore anything before "KEYWORDS"
			while ((line = in.readLine()) != null && !line.equals("KEYWORDS"))
				;
			// Everything is keywords until we run into "TYPES"
			while ((line = in.readLine()) != null && !line.equals("TYPES"))
				colorHash.put(caseSensitive ? line : line.toLowerCase(), keywordColor);
			// Everything is types until we run into "OPERATORS"
			while ((line = in.readLine()) != null && !line.equals("OPERATORS"))
				colorHash.put(caseSensitive ? line : line.toLowerCase(), typeColor);
			// Everything is operators until we run into "DIRECTIVES"
			while ((line = in.readLine()) != null && !line.equals("DIRECTIVES"))
				colorHash.put(caseSensitive ? line : line.toLowerCase(), operatorColor);
			// Everything is directives until we run into "CLASSES"
			while ((line = in.readLine()) != null && !line.equals("CLASSES"))
				colorHash.put(caseSensitive ? line : line.toLowerCase(), directiveColor);
			// The rest of the file is classes
			while ((line = in.readLine()) != null)
				colorHash.put(caseSensitive ? line : line.toLowerCase(), classColor);
			in.close();
		}
		catch (IOException ex)
		{
		}
	}

	/**
	 *	Checks the property kawigi.editor.tabstop and sets the tabstop used in
	 *	GenericView and derivatives to its value.
	 *
	 *	It sets the property and the tabstop to 4 if the property isn't set.
	 **/
	public static void resetTabStop()
	{
		PrefProxy prefs = PrefFactory.getPrefs();
		tabstop = prefs.getInt("kawigi.editor.tabstop", 4);
		matchParens = prefs.getBoolean("kawigi.editor.matchparens", true);
		matchParensColor = prefs.getColor("kawigi.editor.matchparenscolor", new Color(64, 64, 128));
	}

	/**
	 *	Just forwards that Element on down.
	 **/
	public GenericView(Element e)
	{
		super(e);
	}

	/**
	 *	A little hack to put a "TopCoder-ish" logo and a copycat logo for
	 *	KawigiEdit in the background of the text pane.
	 *
	 *	I know, it just makes me seem like I need attention or something, but I
	 *	tried to at least make it look nice and non-intrusive.
	 **/
	public void paint(Graphics g, Shape a)
	{
		Rectangle bounds = a.getBounds();
		Color c1 = getContainer().getForeground();
		Color c2 = getContainer().getBackground();
		int gray1 = (c1.getRed() + c1.getGreen() + c1.getBlue())/3;
		int gray2 = (c2.getRed() + c2.getGreen() + c2.getBlue())/3;
		g.setColor(new Color(0, 0, (gray2+gray2+gray1)/3));
		g.setFont(new Font("Monospaced", Font.BOLD, 36));
		g.drawString("[  ]", (int)bounds.getWidth()/2+(int)bounds.getX()-g.getFontMetrics().stringWidth("[KE]")/2, g.getFontMetrics().getHeight());
		g.setColor(new Color(0, 0, (gray2+gray2+gray2+gray1)/4));
		g.drawString(" KE ", (int)bounds.getWidth()/2+(int)bounds.getX()-g.getFontMetrics().stringWidth("[KE]")/2, g.getFontMetrics().getHeight());
		JTextComponent host = (JTextComponent)getContainer();
		g.setFont(host.getFont());

		if (matchParens)
		{
			ArrayList<Interval> intervals = getIntervals();
			Interval use = null;
			int caret = host.getCaretPosition();
			for (int i=0; i<intervals.size(); i++)
			{
				Interval in = intervals.get(i);
				if (caret >= in.getStartIndex() && caret <= in.getEndIndex() && (use == null || in.getEndIndex() - in.getStartIndex() < use.getEndIndex() - use.getStartIndex()))
					use = in;
			}
			try
			{
				if (use != null && (caret == use.getStartIndex() || caret == use.getEndIndex() || caret == use.getStartIndex()+1 || caret == use.getEndIndex()-1))
				{
					if (highlightKey1 == null)
					{
						highlightKey1 = ((JTextComponent)getContainer()).getHighlighter().addHighlight(use.getStartIndex(), use.getStartIndex()+use.getStartToken().length(), new DefaultHighlighter.DefaultHighlightPainter(matchParensColor));
						highlightKey2 = ((JTextComponent)getContainer()).getHighlighter().addHighlight(use.getEndIndex()-use.getEndToken().length(), use.getEndIndex(), new DefaultHighlighter.DefaultHighlightPainter(matchParensColor));
					}
					else if (hindex1 != use.getStartIndex() || hindex2 != use.getEndIndex())
					{
						hindex1 = use.getStartIndex();
						hindex2 = use.getEndIndex();
						host.getHighlighter().changeHighlight(highlightKey1, hindex1, hindex1+use.getStartToken().length());
						host.getHighlighter().changeHighlight(highlightKey2, hindex2-use.getEndToken().length(), hindex2);
					}
				}
				else if (highlightKey1 != null && hindex1 != 0 || hindex2 != 0)
				{
					hindex1 = 0;
					hindex2 = 0;
					host.getHighlighter().changeHighlight(highlightKey1, 0, 0);
					host.getHighlighter().changeHighlight(highlightKey2, 0, 0);
				}
			}
			catch (BadLocationException ex)
			{
			}
		}
		super.paint(g, a);
	}

	/**
	 *	Programmers don't like 8-space tabstops.
	 *
	 *	At least I don't, so I set the default to 4.  At Ryan's request, this is
	 *	now configurable.
	 **/
	protected int getTabSize()
	{
		return tabstop;
    }

	/**
	 *	Returns an <code>ArrayList</code> of Interval objects representing code
	 *	block intervals in the document.
	 *
	 *	This default implementation matches curly braces, square brackets and
	 *	parentheses.
	 **/
	public ArrayList<Interval> getIntervals()
	{
		ArrayList<Interval> ret = new ArrayList<Interval>();
		try
		{
			parseIndex = 0;
			lineIndex = 1;
			findIntervals(ret, getDocument().getText(0, getDocument().getLength()), new String[]{"{", "}"}, true);
			parseIndex = 0;
			lineIndex = 1;
			findIntervals(ret, getDocument().getText(0, getDocument().getLength()), new String[]{"(", ")"}, false);
			parseIndex = 0;
			lineIndex = 1;
			findIntervals(ret, getDocument().getText(0, getDocument().getLength()), new String[]{"[", "]"}, false);
		}
		catch (BadLocationException ex)
		{
		}
		return ret;
	}

	/**
	 *	Set parseIndex to 0 and lineIndex to 1 before calling findIntervals.
	 **/
	protected int parseIndex, lineIndex;

	/**
	 *	Helper function to help with <code>getIntervals</code>.
	 *
	 *	Puts the intervals into the list in an in-order traversal of curly
	 *	braces.
	 **/
	protected void findIntervals(ArrayList<Interval> list, String text, String[] startEnd, boolean block)
	{
		int startline = lineIndex;
		int startIndex = parseIndex;
		if (parseIndex == 0 && text.length() > 0 && text.charAt(0) == '\n')
			lineIndex ++;
		parseIndex++;
		while (parseIndex < text.length())
		{
			if (text.charAt(parseIndex) == '\n')
				lineIndex ++;
			else if (text.substring(parseIndex).startsWith(startEnd[0]))
			{
				findIntervals(list, text, startEnd, block);
			}
			else if (text.substring(parseIndex).startsWith(startEnd[1]))
			{
				Interval in = findName(text, startIndex, startline, lineIndex, parseIndex, startEnd, block);
				if (in != null)
					list.add(in);
				return;
			}
			else if (text.charAt(parseIndex) == '\"')
			{
				parseIndex++;
				while (parseIndex < text.length() && text.charAt(parseIndex) != '\"' && text.charAt(parseIndex) != '\n' && text.charAt(parseIndex) != '\r')
				{
					if (text.charAt(parseIndex) == '\\')
						parseIndex++;
					parseIndex++;
				}
			}
			else if (text.charAt(parseIndex) == '\'')
			{
				parseIndex++;
				while (parseIndex < text.length() && text.charAt(parseIndex) != '\'' && text.charAt(parseIndex) != '\n' && text.charAt(parseIndex) != '\r')
				{
					if (text.charAt(parseIndex) == '\\')
						parseIndex++;
					parseIndex++;
				}
			}
			else if (text.charAt(parseIndex) == '/' && parseIndex+1 < text.length())
			{
				if (text.charAt(parseIndex+1) == '/')
				{
					while (parseIndex < text.length() && text.charAt(parseIndex) != '\n' && text.charAt(parseIndex) != '\r')
						parseIndex++;
					parseIndex--;
				}
				else if (text.charAt(parseIndex+1) == '*')
				{
					parseIndex+=2;
					//check for a new line but not the end of the comment on
					// this character:
					if (text.charAt(parseIndex) == '\n')
						lineIndex++;
					parseIndex++;
					while (parseIndex < text.length() && !(text.charAt(parseIndex) == '/' && text.charAt(parseIndex-1) == '*'))
					{
						if (text.charAt(parseIndex) == '\n')
							lineIndex++;
						parseIndex++;
					}
					parseIndex--;
				}
			}
			parseIndex++;
		}
	}

	/**
	 *	Searches for a String identifier for a block in the text before the
	 *	block.
	 *
	 *	May or may not be particularly useful in many situations.  This is
	 *	called by findIntervals.
	 **/
	protected Interval findName(String text, int index, int startline, int endline, int endindex, String[] startEnd, boolean block)
	{
		if (index == 0 &! text.startsWith(startEnd[0]))
			return null;
		String ret = "";
		index --;
		int i;
		for (i=Math.min(index, text.length()-1); i>= 0; i--)
		{
			if (ret.length() > 0 && text.charAt(i) == '\n')
			{
				break;
			}
			else if (text.charAt(i) == '\n')
				startline --;
			if (!Character.isWhitespace(text.charAt(i)) || ret.length() > 0)
			{
				ret = text.charAt(i) + ret;
			}
		}
		return new Interval(startline, endline, index+1, endindex+startEnd[1].length(), ret.trim(), startEnd[0], startEnd[1], block);
	}

	/**
	 *	NOTHING BUT A HACK (and a weird, dirty one at that).
	 *
	 *	This is a "fix" to the problem where after undoing exceptions occurred
	 *	causing the cursor to not update its position after edits (which leads
	 *	to a really strange editing experience after that point, as you might
	 *	imagine).  In the case that it was reaching when this bug occurred, It
	 *	seems I can force the parent of this class to do something of a double
	 *	take by spoofing its parent element and calling an update method.  Any
	 *	cleaner hack seemed impossible due to the use of private methods and
	 *	even package-level access classes.
	 *
	 *	This appears to be a blatant bug in Swing.  I think a real fix to this
	 *	would be in AbstractDocument$BranchElement.getEndOffset(), but the
	 *	necessary check here might not really solve the problem, just get it out
	 *	of my way in this case.  In this odd case, nchildren is zero, and
	 *	getEndOffset() tries to access the element at index -1.  Reproducing the
	 *	bug is about as simple as creating a JTextPane that uses a PlainView to
	 *	render, install a pretty standard UndoManager on it, write some
	 *	arbitrary text into it, and then select and paste in some other big text
	 *	or something, then undo (with whatever mechanism you have for that, for
	 *	me it's a keystroke).
	 **/
	protected void updateDamage(DocumentEvent changes, Shape a, ViewFactory f)
	{
		//most of this first code is from the beginning of the method it overrides.
		/*Component host = getContainer();
		updateMetrics();
		Element elem = getElement();
		DocumentEvent.ElementChange ec = changes.getChange(elem);
		Element[] added = (ec != null) ? ec.getChildrenAdded() : null;
		Element[] removed = (ec != null) ? ec.getChildrenRemoved() : null;
		if (((added != null) && (added.length > 0)) ||
		((removed != null) && (removed.length > 0))) {
			//this part is my silly hack.
			View root = getParent();
			setParent(new FakeRootView(added != null && added.length > 0 ? added[0] : removed[0]));
			updateMetrics();
			setParent(root);
			updateMetrics();
			//end of silly hack.
			preferenceChanged(null, true, true);
			host.repaint();
		}*/
		super.updateDamage(changes, a, f);
	}

    /**
     *	Part 2 of my super-dirty hack.
     *
     *	This is one of the most retarded classes I've ever written.
     **/
    /*class FakeRootView extends PlainView
    {
		public FakeRootView(Element e)
		{
			super(e);
		}

    	public Container getContainer()
    	{
    		return new CodePane();
    	}
    }*/
}
