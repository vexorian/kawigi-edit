package kawigi.editor;
import java.awt.*;
import javax.swing.*;

/**
 *	This class represents a CodePane with line numbers and scroll bars.
 *	
 *	It helps me not worry about doing this for every CodePane, and it also makes
 *	it easier to specify in the .ui XML.
 **/
@SuppressWarnings("serial")
public class EditorPanel extends JPanel
{
	/**
	 *	Scroll pane that the editor goes on.
	 **/
	private JScrollPane scrollPane;
	/**
	 *	The actual editor.
	 **/
	private CodePane textPane;
	/**
	 *	The line numbers display that is set as the row header on the scroll
	 *	pane.
	 **/
	private LineNumbers lineNumbers;
	
	/**
	 *	Constructs a new EditorPanel.
	 **/
	public EditorPanel()
	{
		super(new BorderLayout());
		textPane = new CodePane();
		lineNumbers = new LineNumbers(textPane);
		scrollPane = new JScrollPane(textPane, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setRowHeaderView(lineNumbers);
		add(scrollPane);
	}
	
	/**
	 *	Returns the CodePane displayed on this EditorPanel.
	 **/
	public CodePane getCodePane()
	{
		return textPane;
	}
}
