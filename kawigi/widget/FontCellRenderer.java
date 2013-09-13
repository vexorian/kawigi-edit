package kawigi.widget;
import javax.swing.*;
import java.awt.*;

/**
 *	Allows a JComboBox of Fonts to show the fonts in the described font.
 *	
 *	The fonts should be in a reasonable size.
 **/
@SuppressWarnings("serial")
public class FontCellRenderer extends JLabel implements ListCellRenderer
{
	/**
	 *	Returns the component to render the cell <code>value</code> in <code>list</code>
	 **/
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
	{
		if (isSelected)
		{
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		}
		else
		{
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}		
		setFont((Font)value);
		setText(((Font)value).getName());
		return this;
	}
}