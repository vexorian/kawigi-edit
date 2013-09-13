package kawigi.widget;
import javax.swing.*;
import java.awt.*;

/**
 *	Represents an icon with a "color chip" which represents the current selected
 *	color.
 *	
 *	This is similar to color swatches used in some non-Java applications where
 *	the icon has a small rectangle indicating the current color on top of the
 *	regular icon.
 **/
public class ChipIcon implements Icon
{
	/**
	 *	The icon that this icon draws its chip on top of.
	 **/
	private Icon parentIcon;
	/**
	 *	The KawigiEdit color picker that this icon is in front of.
	 **/
	private ColorSwatchDropdown colorPicker;
	
	/**
	 *	Constructs a new ChipIcon which draws a color chip on top of the given
	 *	Icon in the color selected by the given ColorSwatchDropdown.
	 **/
	public ChipIcon(Icon icon, ColorSwatchDropdown colorPicker)
	{
		parentIcon = icon;
		this.colorPicker = colorPicker;
	}
	
	/**
	 *	Returns the height of this icon.
	 **/
	public int getIconHeight()
	{
		return parentIcon.getIconHeight();
	}
	
	/**
	 *	Returns the width of this icon.
	 **/
	public int getIconWidth()
	{
		return parentIcon.getIconWidth();
	}
	
	/**
	 *	Draws this icon at the given location.
	 **/
	public void paintIcon(Component c, Graphics g, int x, int y)
	{
		parentIcon.paintIcon(c, g, x, y);
		g.setColor(colorPicker.getColor());
		g.fillRect(getIconWidth()/8, getIconHeight()*3/4, getIconWidth()*3/4, getIconHeight()/8);
	}
}
