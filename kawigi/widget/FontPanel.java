package kawigi.widget;
import kawigi.cmd.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;

/**
 *	A simple panel that has fields to enter a font face and size.
 **/
@SuppressWarnings("serial")
public class FontPanel extends JPanel implements ItemListener, ChangeListener, PropertyChangeListener
{
	/**
	 *	Combo box that the font face is selected from.
	 **/
	private JComboBox facesBox;
	/**
	 *	Spinner that the font size is selected from.
	 **/
	private JSpinner sizeSpinner;
	/**
	 *	Action that this FontPanel is bound to.
	 **/
	protected Action a;
	
	/**
	 *	Constructs a new FontPanel linked to a.
	 **/
	public FontPanel(Action a)
	{
		this.a = a;
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		JPanel stuffPanel = new JPanel(new GridLayout(2, 1));
		JPanel labelPanel = new JPanel(new GridLayout(2, 1));
		facesBox = new JComboBox();
		facesBox.setRenderer(new FontCellRenderer());
		Font f = (Font)a.getValue(DefaultAction.FONT);
		if (f == null)
			f = new Font("Monospaced", Font.PLAIN, 12);
		Font[] systemFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
		for (int i=0; i<systemFonts.length; i++)
		{
			facesBox.addItem(systemFonts[i].deriveFont(12.0f));
			if (systemFonts[i].getName().equalsIgnoreCase(f.getName()) || systemFonts[i].getName().equalsIgnoreCase(f.getName() + ".plain"))
				facesBox.setSelectedIndex(i);
		}
		sizeSpinner = new JSpinner(new SpinnerNumberModel(f.getSize(), 7, 72, 1));
		JLabel label1 = new JLabel("Font Face:");
		labelPanel.add(label1);
		stuffPanel.add(facesBox);
		JLabel label2 = new JLabel("Size:");
		labelPanel.add(label2);
		stuffPanel.add(sizeSpinner);
		add(labelPanel);
		add(stuffPanel);
		a.addPropertyChangeListener(this);
		facesBox.addItemListener(this);
		sizeSpinner.addChangeListener(this);
	}
	
	/**
	 *	Notifies us that selection has changed on the font combo box.
	 **/
	public void itemStateChanged(ItemEvent e)
	{
		if (e.getStateChange() == ItemEvent.SELECTED)
		{
			changeEvent();
		}
	}
	
	/**
	 *	Notifies us that the value has changed on the Font size spinner.
	 **/
	public void stateChanged(ChangeEvent e)
	{
		changeEvent();
	}
	
	/**
	 *	Process any property changes.
	 **/
	public void changeEvent()
	{
		a.putValue(DefaultAction.FONT, getSelectedFont());
	}
	
	/**
	 *	Processes property changes from the action.
	 **/
	public void propertyChange(PropertyChangeEvent e)
	{
		// Text fields should be able to have their text centralized here.
		if (e.getPropertyName().equals(DefaultAction.FONT))
		{
			if (!e.getNewValue().equals(getSelectedFont()))
			{
				Font newfont = (Font)e.getNewValue();
				for (int i=0; i<facesBox.getItemCount(); i++)
				{
					String name = ((Font)facesBox.getItemAt(i)).getName();
					if (name.equalsIgnoreCase(newfont.getName()) || name.equalsIgnoreCase(newfont.getName() + ".plain"))
						facesBox.setSelectedIndex(i);
				}
				sizeSpinner.setValue(newfont.getSize());
			}
		}
	}
	
	/**
	 *	Returns the currently selected font, with plain decoration (not italic 
	 *	or bold)
	 **/
	public Font getSelectedFont()
	{
		return ((Font)facesBox.getSelectedItem()).deriveFont((float)Integer.parseInt(sizeSpinner.getValue().toString()));
	}
}
