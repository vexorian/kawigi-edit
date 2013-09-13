package kawigi.widget;
import javax.swing.*;
import java.awt.*;
import java.io.*;

/**
 *	Adapted from KawigiEdit 1's kawigi.snippet.Category.
 *	
 *	This is a control that subclasses JMenu and has some additional features
 *	for categories.  Honestly, some of this functionality could be moved to
 *	somewhere else and we could get rid of this class altogether, but it's
 *	already written, so why bother?
 **/
@SuppressWarnings("serial")
public class Category extends JMenu implements MenuElement
{
	/**
	 *	Name of this category or subcategory.
	 **/
	private String name;
	
	/**
	 *	Creates a Category with the given name.  If the name is "", then this is
	 *	the root category (and the label is set to be "Snippets").
	 **/
	public Category(String name)
	{
		super(name.length() == 0 ? "Snippets" : name);
		this.name = name;
	}
	
	/**
	 *	Default constructor for a Category.  This is required to read the
	 *	Category from disk or to just create a root category.
	 **/
	public Category()
	{
		this("");
	}
	
	/**
	 *	Adds a Snippet to this category with the given subcategory.
	 *	
	 *	If <code>cat</code> is empty, the snippet is added to this category.
	 *	If <code>cat</code> is non-empty, it will find the first subcategory
	 *	in cat and create it or find it and add this snippet to that and the
	 *	remaining subcategories recursively.  Categories and subcategories in
	 *	<code>cat</code> are delimited by "/", regardless of the operating
	 *	system (since it has nothing to do with the file system).
	 **/
	public void add(Snippet s, String cat)
	{
		while (cat.endsWith("/"))
			cat = cat.substring(0, cat.length()-1);
		while (cat.startsWith("/"))
			cat = cat.substring(1);
		if (cat.length() == 0)
			add(s);
		else
		{
			Component[] menuitems = getMenuComponents();
			if (cat.indexOf('/') >= 0)
			{
				String nextCat = cat.substring(0, cat.indexOf('/'));
				String rest = cat.substring(cat.indexOf('/')+1);
				for (int i=0; i<menuitems.length; i++)
					if (menuitems[i] instanceof Category && ((Category)menuitems[i]).getName().equals(nextCat))
					{
						((Category)menuitems[i]).add(s, rest);
						return;
					}
				Category newcat = new Category(nextCat);
				add(newcat);
				newcat.add(s, rest);
			}
			else
			{
				for (int i=0; i<menuitems.length; i++)
					if (menuitems[i] instanceof Category && ((Category)menuitems[i]).getName().equals(cat))
					{
						((Category)menuitems[i]).add(s);
						return;
					}
				Category newcat = new Category(cat);
				add(newcat);
				newcat.add(s);
			}
		}
	}
	
	/**
	 *	Returns the name of this category or subcategory.
	 **/
	public String getName()
	{
		return name;
	}
	
	/**
	 *	Sets the name (and text) of this category.
	 **/
	public void setName(String name)
	{
		this.name = name;
		setText(name);
	}
	
	/**
	 *	Returns a string representation of this category for debugging.
	 **/
	public String toString()
	{
		return name + "(" + getItemCount() + ")";
	}
	
	/**
	 *	Writes this category and its children as .ui XML to the given
	 *	PrintWriter.
	 **/
	public void write(PrintWriter out, String indent) throws IOException
	{
		if (name.length() == 0)
			out.println(indent + "<Category>");
		else
			out.println(indent + "<Category Name=\"" + Snippet.escape(name) + "\">");
		Component[] menuitems = getMenuComponents();
		for (int i=0; i<menuitems.length; i++)
		{
			if (menuitems[i] instanceof Category)
			{
				((Category)menuitems[i]).write(out, indent + "\t");
			}
			else if (menuitems[i] instanceof Snippet)
			{
				((Snippet)menuitems[i]).write(out, indent + "\t");
			}
		}
		out.println(indent + "</Category>");
	}
}
