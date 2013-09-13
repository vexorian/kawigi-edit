package kawigi.widget;

import kawigi.cmd.*;
import javax.swing.*;
import java.awt.event.*;

/**
 *	A simple panel that has fields to enter a font face and size.
 **/
@SuppressWarnings("serial")
public class FilePanel extends JPanel implements ActionListener, FocusListener
{
	/**
	 *	Text field that the user can enter the file path into.
	 **/
	private ActionTextField fileField;
	/**
	 *	Button that brings up the file dialog.
	 **/
	private JButton browseButton;

	/**
	 *	Constructs a new FontPanel linked to the given Action.
	 **/
	public FilePanel(Action a)
	{
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		fileField = new ActionTextField(a);
		fileField.setColumns(20);
		fileField.addFocusListener(this);
		browseButton = new JButton("Browse");
		browseButton.addActionListener(this);
		browseButton.addFocusListener(this);
		add(fileField);
		add(browseButton);
	}

	/**
	 *	Notifies us that the "Browse" button was pushed.
	 **/
	public void actionPerformed(ActionEvent e)
	{
		JFileChooser fileChooser = Dispatcher.getFileChooser();
		int oldmode = fileChooser.getFileSelectionMode();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
			fileField.setText(fileChooser.getSelectedFile().getPath());
		fileChooser.setFileSelectionMode(oldmode);
	}

	public void focusGained(FocusEvent e)
	{
		FocusListener[] arr = getFocusListeners();
		for (FocusListener lstn : arr)
			lstn.focusGained(e);
	}

	public void focusLost(FocusEvent e)
	{
		FocusListener[] arr = getFocusListeners();
		for (FocusListener lstn : arr)
			lstn.focusLost(e);
	}
}
