package kawigi.util;
import javax.swing.filechooser.*;
import java.io.File;
import java.util.*;

/**
 *	Good All-purpose configurable file filter.
 **/
public class GenericFileFilter extends FileFilter
{
	/**
	 *	Valid file extensions for this file filter.
	 **/
	private ArrayList<String> extensions;
	
	/**
	 *	Description of the type of files this filter accepts.
	 **/
	private String description;
	
	/**
	 *	Creates a new file filter with the given description.
	 *	
	 *	File extensions should be added subsequently using addExtention.
	 **/
	public GenericFileFilter(String name)
	{
		description = name;
		extensions = new ArrayList<String>();
	}
	
	/**
	 *	Creates a new file filter with the given description and single file extension.
	 *	
	 *	Subsequent file extensions can be added using addExtension.
	 **/
	public GenericFileFilter(String name, String extension)
	{
		this(name);
		extensions.add(extension);
	}
	
	/**
	 *	Adds a file extension accepted by this filter.
	 **/
	public void addExtension(String ext)
	{
		extensions.add(ext);
	}
	
	/**
	 *	Returns true if the file should be shown with this filter.
	 *	
	 *	It will be accepted if it is either a folder or a file with one of the extensions specified for
	 *	this file filter.  The extensions are not case sensitive.
	 **/
	public boolean accept(File f)
	{
		if (f.isDirectory())
			return true;
		for (int i=0; i<extensions.size(); i++)
			if (f.getName().toLowerCase().endsWith("." + extensions.get(i)))
				return true;
		return false;
	}
	
	/**
	 *	Returns the description of this filter.
	 **/
	public String getDescription()
	{
		return description;
	}
}
