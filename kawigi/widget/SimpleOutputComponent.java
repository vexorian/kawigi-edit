package kawigi.widget;
import kawigi.properties.*;
import kawigi.util.*;
import java.awt.*;
import javax.swing.*;

/**
 *	The default display component for compile and program output in processes
 *	started by KawigiEdit.
 **/
@SuppressWarnings("serial")
public class SimpleOutputComponent extends JPanel implements ConsoleDisplay
{
	/**
	 *	The text component used to display the output.
	 **/
	private JTextPane output;
	
	/**
	 *	Constructs a new SimpleOutputComponent.
	 **/
	public SimpleOutputComponent()
	{
		super(new GridLayout(1, 1));
		output = new JTextPane();
		add(new JScrollPane(output));
		updatePrefs();
	}
	
	/**
	 *	Updates colors and fonts from the preferences.
	 **/
	public void updatePrefs()
	{
		PrefProxy prefs = PrefFactory.getPrefs();
		output.setBackground(prefs.getColor("kawigi.testing.background", Color.white));
		output.setForeground(prefs.getColor("kawigi.testing.foreground", Color.black));
		output.setFont(prefs.getFont("kawigi.testing.font", new Font("Monospaced", 0, 12)));
	}
	
	/**
	 *	Appends <code>s</code> to the end of the output display.
	 **/
	public void print(String s)
	{
		output.setCaretPosition(output.getDocument().getLength());
		output.replaceSelection(s);
	}
	
	/**
	 *	Appends <code>s</code> and a new line to the end of the output display.
	 **/
	public void println(String s)
	{
		print(s + "\n");
	}
	
	/**
	 *	Clears the display.
	 **/
	public void clear()
	{
		output.setText("");
	}
}
