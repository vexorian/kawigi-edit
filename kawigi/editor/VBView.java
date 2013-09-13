package kawigi.editor;
import javax.swing.text.*;
import java.awt.*;
import java.util.*;

/**
 *	Implementation of a Visual Basic implementation of a View that hilights
 *	syntax attributes.
 **/
public class VBView extends GenericView
{
	/**
	 *	A map from token to its color.
	 **/
	protected static Hashtable<String,Color> colorHash;

	static
	{
		initColors();
	}

	/**
	 *	Initializes the keywords/tokens and their colors.
	 *
	 *	What this basically does is calls GenericView.getColors and
	 *	GenericView.readKeywords.  It is called when the class is loaded and
	 *	any time when the color settings might have changed.
	 **/
	public static void initColors()
	{
		colorHash = new Hashtable<String,Color>();
		getColors();
		readKeywords("vb", false, colorHash);
	}

	/**
	 *	Creates an instance of VBView and passes along the Element.
	 **/
	public VBView(Element e)
	{
		super(e);
	}

	/**
	 *	Overridden from PlainView - this method gets called to render every
	 *	element of unselected text.
	 **/
	protected int drawUnselectedText(Graphics g, int x, int y, int p0, int p1) throws BadLocationException
	{
		String s = getDocument().getText(p0, p1 - p0);
		String before = getDocument().getText(0, p0);
		boolean inComment = false;
		for (int i=0; i<before.length(); i++)
		{
			if (inComment && before.charAt(i) == '/' && i > 0 && before.charAt(i-1) == '*')
				inComment = false;
			else if (!inComment && before.charAt(i) == '*' && i > 0 && before.charAt(i-1) == '/')
				inComment = true;
			else if (!inComment && before.charAt(i) == '\"')
				for (i = i+1; i<before.length() && before.charAt(i) != '\"' && before.charAt(i) != '\r' && before.charAt(i) != '\n'; i++)
					;
			else if (!inComment && before.charAt(i) == '\'')
				for (i = i+1; i<before.length() && before.charAt(i) != '\'' && before.charAt(i) != '\r' && before.charAt(i) != '\n'; i++)
					;
			else if (!inComment && before.charAt(i) == '/' && i > 0 && before.charAt(i-1) == '/')
				for (i = i+1; i<before.length() && before.charAt(i) != '\r' && before.charAt(i) != '\n'; i++)
					;
		}
		return drawTabbedText(s, x, y, g, p0, false);
	}

	/**
	 *	Overridden from PlainView - this method gets called to render every
	 *	element of selected text, so that I could change how it's displayed if I
	 *	want.
	 **/
	protected int drawSelectedText(Graphics g, int x, int y, int p0, int p1) throws BadLocationException
	{
		String s = getDocument().getText(p0, p1 - p0);
		getDocument().getText(0, p0);
		return drawTabbedText(s, x, y, g, p0, true);
	}

	/**
	 *	Renders the text segment onto the given Grapnics context.
	 *
	 *	Originally, this looked a lot like drawTabbedText from the
	 *	javax.swing.text.Utilities class with a little bit of tokenizing, but
	 *	now it's pretty much modified to my own style.  Unfortunately, at some
	 *	point in time, my indentation got all weird (probably as a result of me
	 *	using tabs and the original implementation mixing spaces and tabs), so
	 *	there may be some weird-looking parts left in the code.
	 *
	 *	This is the magic of my syntax hilighting for VB.
	 **/
	protected int drawTabbedText(String s, int x, int y, Graphics g, int startOffset, boolean selected)
	{
		if (selected)
			g.setColor(((JTextComponent)getContainer()).getSelectedTextColor());
		FontMetrics metrics = g.getFontMetrics();
		int flushIndex = 0;
		for (int i = 0; i < s.length(); i++)
		{
			//parse a string:
			if (s.charAt(i) == '\"')
			{
	    		if (flushIndex < i)
	    		{
					if (!selected)
			    		g.setColor(getColor(s.substring(flushIndex, i)));
		    		g.drawString(s.substring(flushIndex, i), x, y);
		    		x += metrics.stringWidth(s.substring(flushIndex, i));
		   	 		flushIndex = i;
		    	}
				i++;
				for (; i<s.length() && s.charAt(i) != '\"' && s.charAt(i) != '\n' && s.charAt(i) != '\r'; i++)
				{
					if (s.charAt(i) == '\t')
					{
						if (i > flushIndex)
						{
							if (!selected)
		    						g.setColor(stringColor);
							g.drawString(s.substring(flushIndex, i), x, y);
		    				x += metrics.stringWidth(s.substring(flushIndex, i));
						}
						flushIndex = i + 1;
						x = (int)nextTabStop((float)x, startOffset+i);
					}
					//There's apparently only one escape sequence in VB.  Weird, huh?
					if (s.charAt(i) == '\"' && i+1 < s.length() && s.charAt(i) == '\"')
						i++;
				}
				if (!selected)
			    	g.setColor(stringColor);
				g.drawString(s.substring(flushIndex, i), x, y);
		    	x += metrics.stringWidth(s.substring(flushIndex, i));
				if (i < s.length() && s.charAt(i) == '\"')
				{
					g.drawString(s.substring(i, i+1), x, y);
			    	x += metrics.charWidth(s.charAt(i));
			    }
	    		flushIndex = i+1;
			}
			else if (s.charAt(i) == '\'')	//parsing VB comments.
			{
	    		if (flushIndex < i)
	    		{
					if (!selected)
						g.setColor(getColor(s.substring(flushIndex, i)));
		   	 		g.drawString(s.substring(flushIndex, i), x, y);
		    		x += metrics.stringWidth(s.substring(flushIndex, i));
					flushIndex = i;
				}
				i++;
				for (; i<s.length() && s.charAt(i) != '\n' && s.charAt(i) != '\r'; i++)
				{
					if (s.charAt(i) == '\t')
					{
						if (i > flushIndex)
						{
							if (!selected)
		    					g.setColor(commentColor);
							g.drawString(s.substring(flushIndex, i), x, y);
		    				x += metrics.stringWidth(s.substring(flushIndex, i));
						}
						flushIndex = i + 1;
						x = (int)nextTabStop((float)x, startOffset+i);
					}
				}
				if (!selected)
					g.setColor(commentColor);
				//some people may let this bug them, but I like having a tribute
				//to myself in my plugin, so those people can hack it out or put
				//up with it.
				if (s.substring(flushIndex, i).endsWith("[KawigiEdit]"))
			   	{
			   		if (!s.substring(flushIndex, i).equals("[KawigiEdit]"))
			   		{
			   			g.drawString(s.substring(flushIndex, s.indexOf("[KawigiEdit]", flushIndex)), x, y);
			   			x += metrics.stringWidth(s.substring(flushIndex, s.indexOf("[KawigiEdit]", flushIndex)));
			   		}
			   		Font oldFont = g.getFont();
			   		g.setFont(oldFont.deriveFont(Font.BOLD));
			    	if (!selected)
				   		g.setColor(getContainer().getForeground());
			    	g.drawString("[KawigiEdit]", x, y);
			    	if (!selected)
				   		g.setColor(Color.red);
		    		g.drawString("Edit", x+g.getFontMetrics().stringWidth("[Kawigi"), y);
		    		x += g.getFontMetrics().stringWidth("[KawigiEdit]");
			   		g.setFont(oldFont);
			   	}
			   	else
			   	{
					g.drawString(s.substring(flushIndex, i), x, y);
		    		x += metrics.stringWidth(s.substring(flushIndex, i));
		    	}
				if (i < s.length() && s.charAt(i) == '\'')
				{
					g.drawString(s.substring(i, i+1), x, y);
			   		x += metrics.charWidth(s.charAt(i));
			   	}
		    	flushIndex = i+1;
			}
			//Tabs are basically non-displayable characters, so I need to flush and
			//move where I'm drawing.
			else if (s.charAt(i) == '\t')
			{
				if (i > flushIndex)
				{
					if (!selected)
						g.setColor(getColor(s.substring(flushIndex, i)));
					g.drawString(s.substring(flushIndex, i), x, y);
		    		x += metrics.stringWidth(s.substring(flushIndex, i));
				}
				flushIndex = i + 1;
				x = (int) nextTabStop((float) x, startOffset+i);
			}
			//end of line...
			else if ((s.charAt(i) == '\n') || (s.charAt(i) == '\r'))
			{
				if (i > flushIndex)
				{
					if (!selected)
			    		g.setColor(getColor(s.substring(flushIndex, i)));
					g.drawString(s.substring(flushIndex, i), x, y);
		    		x += metrics.stringWidth(s.substring(flushIndex, i));
				}
				flushIndex = i + 1;
	    	}
	    	//parsing compiler directives:
	    	else if (s.charAt(i) == '#')
	    	{
	    		if (flushIndex < i)
	    		{
					if (!selected)
			    		g.setColor(getColor(s.substring(flushIndex, i)));
		    		g.drawString(s.substring(flushIndex, i), x, y);
		    		x += metrics.stringWidth(s.substring(flushIndex, i));
		   	 		flushIndex = i;
		    	}
				i++;
				for (; i<s.length() && s.charAt(i) != '\n' && s.charAt(i) != '\r' && (s.charAt(i) != '/' || i+1 >= s.length() || (s.charAt(i+1) != '/' && s.charAt(i+1) != '*')); i++)
				{
					if (s.charAt(i) == '\t')
					{
						if (i > flushIndex)
						{
							if (!selected)
		    					g.setColor(directiveColor);
							g.drawString(s.substring(flushIndex, i), x, y);
		    				x += metrics.stringWidth(s.substring(flushIndex, i));
						}
						flushIndex = i + 1;
						x = (int)nextTabStop((float)x, startOffset+i);
					}
				}
				i--;
				if (i >= flushIndex)
				{
					if (!selected)
				    	g.setColor(getColor(s.substring(flushIndex, i+1).split("[ \\t\\n\\r\\f]")[0]));
					g.drawString(s.substring(flushIndex, i+1), x, y);
		    		x += metrics.stringWidth(s.substring(flushIndex, i+1));
				}
				flushIndex = i+1;
	    	}
			//KawigiEdit control stuff:
			else if (i < s.length()-4 && s.charAt(i) == '<' && s.charAt(i+1) == '%' && s.charAt(i+2) == ':' && s.indexOf('%', i+2) >= 0 && s.indexOf('%', i+2) < s.length()-1 && s.charAt(s.indexOf('%', i+2)+1) == '>')
			{
				if (flushIndex < i)
				{
					if (!selected)
						g.setColor(getColor(s.substring(flushIndex, i)));
					g.drawString(s.substring(flushIndex, i), x, y);
					x += metrics.stringWidth(s.substring(flushIndex, i));
				}
				flushIndex = i;
				i = s.indexOf('%', i+2)+1;
				g.setColor(tagColor);
				g.drawString(s.substring(flushIndex, i+1), x, y);
				x += metrics.stringWidth(s.substring(flushIndex, i+1));
				flushIndex = i+1;
			}
	    	//all this stuff is part of a normal "token"
			else if (Character.isLetterOrDigit(s.charAt(i)) || s.charAt(i) == '_')
			{
			}
		    else	//otherwise flush!
		    {
		    	//if I have stuff in the buffer, display it first...
		    	if (flushIndex < i)
		    	{
					if (!selected)
			    		g.setColor(getColor(s.substring(flushIndex, i)));
		    		g.drawString(s.substring(flushIndex, i), x, y);
		    		x += metrics.stringWidth(s.substring(flushIndex, i));
		    	}
				if (!selected)
		    		g.setColor(getColor(s.substring(i, i+1)));
		    	//then do the current character!
				g.drawString(s.substring(i, i+1), x, y);
				flushIndex = i+1;
				x += metrics.charWidth(s.charAt(i));
			}
		}
		//flush if I haven't yet.
		if (flushIndex < s.length())
		{
			if (!selected)
				g.setColor(getColor(s.substring(flushIndex, s.length())));
			g.drawString(s.substring(flushIndex, s.length()), x, y);
		    x += metrics.stringWidth(s.substring(flushIndex, s.length()));
		}
		return x;
	}

	/**
	 *	Returns the color to use on the given token.
	 *
	 *	This will return the default foreground color, if no special color is
	 *	assigned to that token.
	 **/
	protected Color getColor(String word)
	{
		if (colorHash.containsKey(word.toLowerCase()))
			return (Color)colorHash.get(word.toLowerCase());
		else
			return getContainer().getForeground();
	}

	/**
	 *	Returns an <code>ArrayList</code> of <code>Interval</code> objects
	 *	representing code block intervals in the document.
	 *
	 *	This version is specifically for finding blocks in the Visual Basic
	 *	language.
	 **/
	public ArrayList<Interval> getIntervals()
	{
		ArrayList<Interval> ret = new ArrayList<Interval>();
		try
		{
			parseIndex = 0;
			lineIndex = 1;
			findIntervals(ret, getDocument().getText(0, getDocument().getLength()).toLowerCase(), new String[]{"class", "end class"}, true);
			parseIndex = 0;
			lineIndex = 1;
			findIntervals(ret, getDocument().getText(0, getDocument().getLength()).toLowerCase(), new String[]{"module", "end module"}, true);
			parseIndex = 0;
			lineIndex = 1;
			findIntervals(ret, getDocument().getText(0, getDocument().getLength()).toLowerCase(), new String[]{"sub", "end sub"}, true);
			parseIndex = 0;
			lineIndex = 1;
			findIntervals(ret, getDocument().getText(0, getDocument().getLength()).toLowerCase(), new String[]{"for", "next"}, true);
			parseIndex = 0;
			lineIndex = 1;
			findIntervals(ret, getDocument().getText(0, getDocument().getLength()).toLowerCase(), new String[]{"if", "end if"}, true);
			parseIndex = 0;
			lineIndex = 1;
			findIntervals(ret, getDocument().getText(0, getDocument().getLength()).toLowerCase(), new String[]{"with", "end with"}, true);
			parseIndex = 0;
			lineIndex = 1;
			findIntervals(ret, getDocument().getText(0, getDocument().getLength()).toLowerCase(), new String[]{"structure", "end structure"}, true);
			parseIndex = 0;
			lineIndex = 1;
			findIntervals(ret, getDocument().getText(0, getDocument().getLength()).toLowerCase(), new String[]{"function", "end function"}, true);
			parseIndex = 0;
			lineIndex = 1;
			findIntervals(ret, getDocument().getText(0, getDocument().getLength()).toLowerCase(), new String[]{"synclock", "end synclock"}, true);
			parseIndex = 0;
			lineIndex = 1;
			findIntervals(ret, getDocument().getText(0, getDocument().getLength()).toLowerCase(), new String[]{"try", "end try"}, true);
			parseIndex = 0;
			lineIndex = 1;
			findIntervals(ret, getDocument().getText(0, getDocument().getLength()).toLowerCase(), new String[]{"get", "end get"}, true);
			parseIndex = 0;
			lineIndex = 1;
			findIntervals(ret, getDocument().getText(0, getDocument().getLength()).toLowerCase(), new String[]{"set", "end set"}, true);
			parseIndex = 0;
			lineIndex = 1;
			findIntervals(ret, getDocument().getText(0, getDocument().getLength()).toLowerCase(), new String[]{"property", "end property"}, true);
			parseIndex = 0;
			lineIndex = 1;
			findIntervals(ret, getDocument().getText(0, getDocument().getLength()).toLowerCase(), new String[]{"select", "end select"}, true);
			parseIndex = 0;
			lineIndex = 1;
			findIntervals(ret, getDocument().getText(0, getDocument().getLength()).toLowerCase(), new String[]{"do", "loop"}, true);
			parseIndex = 0;
			lineIndex = 1;
			findIntervals(ret, getDocument().getText(0, getDocument().getLength()).toLowerCase(), new String[]{"while", "end while"}, true);
			parseIndex = 0;
			lineIndex = 1;
			findIntervals(ret, getDocument().getText(0, getDocument().getLength()).toLowerCase(), new String[]{"(", ")"}, false);
			parseIndex = 0;
			lineIndex = 1;
			findIntervals(ret, getDocument().getText(0, getDocument().getLength()).toLowerCase(), new String[]{"#region", "#end region"}, true);
			parseIndex = 0;
			lineIndex = 1;
			findIntervals(ret, getDocument().getText(0, getDocument().getLength()).toLowerCase(), new String[]{"#externalsource", "#end externalsource"}, true);
			parseIndex = 0;
			lineIndex = 1;
			findIntervals(ret, getDocument().getText(0, getDocument().getLength()).toLowerCase(), new String[]{"#if", "#end if"}, true);
		}
		catch (BadLocationException ex)
		{
		}
		return ret;
	}

	/**
	 *	Helper function to help with <code>getIntervals</code>.
	 *
	 *	Puts the intervals into the list in an in-order traversal of blocks
	 *	between startEnd[0] and startEnd[1].  This is modified from the
	 *	C-syntax-based one in GenericView to work better with VB's string and
	 *	comment syntax.
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
					parseIndex++;
			}
			else if (text.charAt(parseIndex) == '\'')
			{
				while (parseIndex < text.length() && text.charAt(parseIndex) != '\n' && text.charAt(parseIndex) != '\r')
					parseIndex++;
				parseIndex--;
			}
			parseIndex++;
		}
	}

	/**
	 *	Searches for a String identifier for a block in the text after the block
	 *	start token.
	 *
	 *	Overridden from GenericView for VB.  This language is like one big
	 *	special case, relative to the other three allowed by TopCoder.
	 **/
	protected Interval findName(String text, int index, int startline, int endline, int endindex, String[] startEnd, boolean block)
	{
		int startindex = index;
		if (index == 0 &! text.startsWith(startEnd[0]))
			return null;
		String ret = "";
		index = text.indexOf(startEnd[0], index) + startEnd[0].length();
		int i;
		for (i=Math.min(index, text.length()-1); i < endindex; i++)
		{
			if (ret.length() > 0 && text.charAt(i) == '\n')
			{
				break;
			}
			if (!Character.isWhitespace(text.charAt(i)) || ret.length() > 0)
			{
				ret += text.charAt(i);
			}
		}
		return new Interval(startline, endline, startindex, endindex+startEnd[1].length(), ret.trim(), startEnd[0], startEnd[1], block);
	}
}
