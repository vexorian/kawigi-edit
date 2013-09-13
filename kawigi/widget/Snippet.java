package kawigi.widget;
import kawigi.cmd.*;
import java.io.*;
import java.awt.event.*;
import javax.swing.*;
/**
 *	This represents a Snippet menu item.
 *	
 *	Some of this came from kawigi.snippet.Snippet in KawigiEdit 1, but the
 *	actual implementation has been greatly simplified.
 *	
 *	The important stuff that this class does is:
 *	<ul>
 *		<li>It contains the code to save the snippet to disk.</li>
 *		<li>It stores the name of the snippet and the code inserted by the
 *		snippet.</li>
 *		<li>It listens for clicks on itself.  When a user clicks on a snippet,
 *		the code for the snippet is saved to the SnippetContext and
 *		actInsertCode is executed from the Dispatcher.</li>
 *	</ul>
 **/
@SuppressWarnings("serial")
public class Snippet extends JMenuItem implements ActionListener
{
	/**
	 *	The code that should be inserted when this snippet is used.
	 **/
	private String code;
	/**
	 *	The name of this snippet.
	 **/
	private String name;
	
	/**
	 *	Default constructor for a snippet.
	 *	
	 *	This is needed to load snippets using the UIHandler.
	 **/
	public Snippet()
	{
		this("","");
	}
	
	/**
	 *	Constructs a new snippet with the given code and name.
	 **/
	public Snippet(String code, String name)
	{
		super(name);
		this.code = code;
		this.name = name;
		addActionListener(this);
	}
	
	/**
	 *	Returns the name of this snippet.
	 **/
	public String getName()
	{
		return name;
	}
	
	/**
	 *	Returns the code this snippet is associated with.
	 **/
	public String getCode()
	{
		return code;
	}
	
	/**
	 *	Sets the name (and label) of this snippet.
	 **/
	public void setName(String name)
	{
		this.name = name;
		setText(name);
	}
	
	/**
	 *	Sets the code associated with this snippet.
	 **/
	public void changeCode(String code)
	{
		this.code = code;
	}
	
	/**
	 *	Returns the code associated with this snippet.
	 **/
	public String toString()
	{
		return code;
	}
	
	/**
	 *	Notifies us that the snippet is being used.
	 **/
	public void actionPerformed(ActionEvent e)
	{
		SnippetContext.setInsertCode(code);
		Dispatcher.getGlobalDispatcher().runCommand(ActID.actInsertSnippet);
	}
	
	/**
	 *	Writes the .ui XML for this snippet to <code>out</code>.
	 **/
	public void write(PrintWriter out, String indent) throws IOException
	{
		out.print(indent + "<Snippet Name=\"" + escape(name) + "\">");
		out.print(escape(code));
		out.print("</Snippet>");
	}
	
	/**
	 *	Utility function (also used by Category) which takes a String and
	 *	converts any special XML characters to XML escape sequences.
	 **/
	public static String escape(String code)
	{
		String out = "";
		for (int i=0; i<code.length(); i++)
		{
			switch (code.charAt(i))
			{
				default:
					out += code.charAt(i);
					break;
				case '&':
					out += "&amp;";
					break;
				case '\"':
					out += "&quot;";
					break;
				case '<':
					out += "&lt;";
					break;
				case '>':
					out += "&gt;";
					break;
			}
		}
		return out;
	}
}
