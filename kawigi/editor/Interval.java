package kawigi.editor;

/**
 *	Represents a block of code that is logically delimited (like by parentheses, curly braces, or other
 *	matched constructs).
 **/
public class Interval
{
	/**
	 *	The index of the line (zero-based) that this interval starts on.
	 **/
	private int startline;
	/**
	 *	The index of the line (zero-based) that this interval ends on.
	 **/
	private int endline;
	/**
	 *	The string index of the start of this interval.
	 **/
	private int startindex;
	/**
	 *	The string index of the end of this interval.
	 **/
	private int endindex;
	/**
	 *	The "Name" of the block, if there is one found.
	 *	
	 *	Normally this comes from before the start of the interval.  If I were to
	 *	implement block collapsing (like in several common IDEs like Visual
	 *	Studio), this would be the text that would show when the block was
	 *	collapsed.
	 **/
	private String name;
	/**
	 *	The token that starts this interval.
	 **/
	private String startString;
	/**
	 *	The token that ends this interval.
	 **/
	private String endString;
	/**
	 *	True if this should be detected by the block matching by the line
	 *	numbers.
	 **/
	private boolean block;
	
	/**
	 *	Constructs a new Interval with the given fields.
	 **/
	public Interval(int start, int end, int startind, int endind, String name, String startString, String endString, boolean block)
	{
		startline = start;
		endline = end;
		startindex = startind;
		endindex = endind;
		this.name = name;
		this.startString = startString;
		this.endString = endString;
		this.block = block;
	}
	
	/**
	 *	Returns the "name" of this block.
	 *	
	 *	The name usually comes from somewhere right before the start of the
	 *	block.  It won't always have any meaning, but it will end in with the
	 *	start and end delimiters that define the interval.
	 **/
	public String getName()
	{
		return name;
	}
	
	/**
	 *	Returns the line number that the block starts on.
	 *
	 *  This includes the "title" of the block, if it's on a previous line.
	 **/
	public int getStartLine()
	{
		return startline;
	}
	
	/**
	 *	Returns the line number that the block ends on.
	 **/
	public int getEndLine()
	{
		return endline;
	}
	
	/**
	 *	Returns the literal index in the string that the block starts on.
	 *	
	 *	This doesn't include the title of the block.
	 **/
	public int getStartIndex()
	{
		return startindex;
	}
	
	/**
	 *	Returns the literal index in the string of code that the block ends on.
	 **/
	public int getEndIndex()
	{
		return endindex;
	}
	
	/**
	 *	Returns the token ending this match.
	 **/
	public String getEndToken()
	{
		return endString;
	}
	
	/**
	 *	Returns the token starting this match.
	 **/
	public String getStartToken()
	{
		return startString;
	}
	
	/**
	 *	Returns true if this interval is appropriate for block marking on the
	 *	left side of the code.
	 **/
	public boolean isBlock()
	{
		return block;
	}
}
