package kawigi.cmd;
import kawigi.util.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;

/**
 *	Action implementation for Template editor actions.
 **/
@SuppressWarnings("serial")
public class TemplateAction extends DefaultAction
{
	/**
	 *	File to save current code to.
	 **/
	private static File location;
	
	/**
	 *	Language for which the current template is intended.
	 **/
	private static String language;
	
	/**
	 *	Constructs a new TemplateAction for the given ActID.
	 **/
	public TemplateAction(ActID cmdid)
	{
		super(cmdid);
	}
	
	/**
	 *	Returns true if these actions are available.
	 **/
	public boolean isEnabled()
	{
		if (Dispatcher.getTemplateEditor() == null)
			return false;
		return true;
	}
	
	/**
	 *	Runs the command.
	 **/
	public void actionPerformed(ActionEvent e)
	{
		switch (cmdid)
		{
			case actOpenTemplate:
				JFileChooser chooser = Dispatcher.getFileChooser();
				chooser.setFileFilter(new GenericFileFilter("KawigiEdit Templates (*.ket)", "ket"));
				if (chooser.showOpenDialog(Dispatcher.getTemplateEditor()) == JFileChooser.APPROVE_OPTION)
				{
					location = chooser.getSelectedFile();
					if (location.exists())
					{
						BufferedReader inFile = null;
						try
						{
							inFile = new BufferedReader(new FileReader(location));
							String stuff = "";
							String line;
							while ((line = inFile.readLine()) != null)
								stuff += line + "\n";
							Dispatcher.getTemplateCodePane().setText(stuff);
							inFile.close();
						}
						catch (IOException ex)
						{
							if (inFile != null)
								try
								{
									inFile.close();
								}
								catch (IOException ex2)
								{
								}
						}
					}
				}
				chooser.resetChoosableFileFilters();
				break;
			case actSaveTemplateAs:
				save(true);
				break;
			case actSaveTemplate:
				save(false);
				break;
			case actOpenDefaultTemplate:
				language = (String)JOptionPane.showInputDialog(Dispatcher.getTemplateEditor(), "Choose Default Template to Open", "Choose Template Language", JOptionPane.QUESTION_MESSAGE, null, new Object[]{"java.ket", "cpp.ket", "py.ket", "csharp.ket", "vb.ket" }, "java.ket");
				if (language != null)
				{
					InputStream stream = getClass().getClassLoader().getResourceAsStream("rc/templates/" + language);
					if (stream != null)
					{
						BufferedReader in = null;
						try
						{
							in = new BufferedReader(new InputStreamReader(stream));
							String stuff = "";
							String line;
							while ((line = in.readLine()) != null)
								stuff += line + "\n";
							Dispatcher.getTemplateCodePane().setText(stuff);
							in.close();
						}
						catch (IOException ex)
						{
							System.err.println("IOException");
							if (in != null)
								try
								{
									in.close();
								}
								catch (IOException ex2)
								{
								}
						}
					}
					else
						System.err.println("Null stream - resource not found");
				}
				break;
		}
	}
	
	/**
	 *	Saves the current file.
	 *	
	 *	Prompts the user for the file to save to if necessary, or if prompt is true.
	 **/
	public static void save(boolean prompt)
	{
		if (prompt || location == null)
		{
			JFileChooser chooser = Dispatcher.getFileChooser();
			chooser.setFileFilter(new GenericFileFilter("KawigiEdit Templates (*.ket)", "ket"));
			if (chooser.showSaveDialog(Dispatcher.getTemplateEditor()) == JFileChooser.APPROVE_OPTION)
			{
				location = chooser.getSelectedFile();
				chooser.resetChoosableFileFilters();
			}
			else
			{
				chooser.resetChoosableFileFilters();
				return;
			}
		}
		PrintWriter outFile = null;
		try
		{
			outFile = new PrintWriter(new BufferedWriter(new FileWriter(location)));
			outFile.println(Dispatcher.getTemplateCodePane().getText());
			outFile.close();
		}
		catch (IOException ex)
		{
			if (outFile != null)
				outFile.close();
		}
		if (JOptionPane.showConfirmDialog(Dispatcher.getTemplateEditor(), "Would you like this template to be used?", "Template Saved", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
		{
			language = (String)JOptionPane.showInputDialog(Dispatcher.getTemplateEditor(), "Choose Template to Override", "Choose Template Language", JOptionPane.QUESTION_MESSAGE, null, new Object[]{"java.ket", "cpp.ket", "py.ket", "csharp.ket", "vb.ket"}, language == null ? "java.ket" : language);
			if (language.equals("java.ket"))
			{
				DefaultAction a = Dispatcher.getGlobalDispatcher().getAction(ActID.actJavaOverride);
				a.putValue(DefaultAction.TEXT, location.getPath());
			}
			else if (language.equals("cpp.ket"))
			{
				DefaultAction a = Dispatcher.getGlobalDispatcher().getAction(ActID.actCPPOverride);
				a.putValue(DefaultAction.TEXT, location.getPath());
			}
			else if (language.equals("csharp.ket"))
			{
				DefaultAction a = Dispatcher.getGlobalDispatcher().getAction(ActID.actCSharpOverride);
				a.putValue(DefaultAction.TEXT, location.getPath());
			}
			else if (language.equals("vb.ket"))
			{
				DefaultAction a = Dispatcher.getGlobalDispatcher().getAction(ActID.actVBOverride);
				a.putValue(DefaultAction.TEXT, location.getPath());
			}
			else if (language.equals("py.ket"))
			{
				DefaultAction a = Dispatcher.getGlobalDispatcher().getAction(ActID.actPyOverride);
				a.putValue(DefaultAction.TEXT, location.getPath());
			}
		}
	}
}
