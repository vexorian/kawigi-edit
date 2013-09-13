package kawigi.widget;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 *	This is a component that renders a grid of colors for the user to choose
 *	from.
 **/
@SuppressWarnings("serial")
public class ColorSwatch extends JMenuItem implements MouseListener
{
	/**
	 *	The default colors available in the swatch.
	 **/
	private static final Color[][] defaultColors = new Color[][]{
		//	Gray Column					Green Column				Purple Column				Blue Column					Yellow Column				Red Column					Cyan Column
		{new Color(0, 0, 0),		new Color(0, 63, 0),		new Color(85, 0, 85),		new Color(0, 0, 85),		new Color(51, 51, 0),		new Color(85, 0, 0),		new Color(0, 85, 85)},
		{new Color(51, 51, 51),		new Color(0, 127, 0),		new Color(170, 0, 170),		new Color(64, 64, 128),		new Color(102, 102, 0),		new Color(170, 0, 0), 		new Color(0, 170, 170)},
		{new Color(102, 102, 102),	new Color(0, 191, 0),		new Color(191, 63, 191),	new Color(0, 0, 170),		new Color(191, 191, 0),		new Color(191, 63, 63),		new Color(85, 170, 170)},
		{new Color(153, 153, 153),	new Color(0, 255, 0),		new Color(255, 0, 255),		new Color(0, 0, 255),		new Color(255, 255, 0),		new Color(255, 0, 0),		new Color(0, 255, 255)},
		{new Color(204, 204, 204),	new Color(63, 255, 63), 	new Color(255, 85, 255),	new Color(127, 127, 255),	new Color(255, 255, 85),	new Color(255, 63, 63),		new Color(85, 255, 255)},
		{new Color(255, 255, 255),	new Color(127, 255, 127),	new Color(255, 170, 255),	new Color(64, 192, 255),	new Color(255, 255, 170),	new Color(255, 127, 127),	new Color(170, 255, 255)}
	};
	
	/**
	 *	Colors available in this swatch grid.
	 **/
	private Color[][] swatchItems;
	/**
	 *	"Most Recently Used" color items.
	 **/
	private static Color[] mruItems;
	/**
	 *	Currently selected color.
	 **/
	private Color selected;
	
	/**
	 *	Constructs a new ColorSwatch with the default colors.
	 **/
	public ColorSwatch()
	{
		swatchItems = new Color[defaultColors.length][defaultColors[0].length];
		for (int i=0; i<swatchItems.length; i++)
			for (int j=0; j<swatchItems[i].length; j++)
				swatchItems[i][j] = defaultColors[i][j];
		if (mruItems == null)
			mruItems = new Color[swatchItems[0].length];
		if (mruItems.length < swatchItems[0].length)
		{
			Color[] newMruItems = new Color[swatchItems[0].length];
			for (int i=0; i<mruItems.length; i++)
				newMruItems[i] = mruItems[i];
			mruItems = newMruItems;
		}
		setPreferredSize(new Dimension(15*swatchItems[0].length+5, 15 /* Main Swatch label */ + 15*swatchItems.length + 5 /* main swatch */+ 15 /* MRU label */ + 20 /* MRU swatches and end gap */));
		addMouseListener(this);
	}
	
	/**
	 *	Sets the selected color value on this swatch.
	 **/
	public void setSelected(Color c)
	{
		selected = c;
		addToMru(selected);
		repaint();
	}
	
	/**
	 *	Gets the selected color value from this swatch.
	 **/
	public Color getSelected()
	{
		return selected;
	}
	
	/**
	 *	Calls paintComponent.
	 **/
	public void paint(Graphics g)
	{
		paintComponent(g);
	}
	
	/**
	 *	Renders the color swatch grid.
	 **/
	public void paintComponent(Graphics g)
	{
		g.clearRect(0, 0, getWidth(), getHeight());
		g.setColor(Color.black);
		g.setFont(g.getFont().deriveFont(Font.BOLD));
		g.drawString("Colors", 5, 15);
		g.drawString("Recently Used", 5, 30 + 15 * swatchItems.length);
		boolean foundSelected = false;
		for (int y=0; y<swatchItems.length; y++)
			for (int x=0; x<swatchItems[y].length; x++)
			{
				if (selected != null && selected.equals(swatchItems[y][x]))
				{
					g.setColor(new Color(192, 128, 255));
					g.fillRect(x*15+3, y*15+18, 15, 15);
					foundSelected = true;
				}
				g.setColor(swatchItems[y][x]);
				g.fillRect(x*15+5, y*15+20, 10, 10);
			}
		for (int x=0; x<mruItems.length && mruItems[x] != null; x++)
		{
			if (!foundSelected && selected != null && selected.equals(mruItems[x]))
			{
				g.setColor(new Color(192, 128, 255));
				g.fillRect(x*15+3, 33 + swatchItems.length * 15, 15, 15);
			}
			g.setColor(mruItems[x]);
			g.fillRect(x*15+5, 35 + swatchItems.length * 15, 10, 10);
		}
		g.setColor(Color.darkGray);
		for (int y=0; y<swatchItems.length; y++)
			for (int x=0; x<swatchItems[y].length; x++)
				g.drawRect(x*15+5, y*15+20, 10, 10);
		for (int x=0; x<mruItems.length && mruItems[x] != null; x++)
			g.drawRect(x*15+5, 35 + swatchItems.length * 15, 10, 10);
	}
	
	/**
	 *	Action when the user clicks on the swatch grid.
	 **/
	public void mousePressed(MouseEvent e)
	{
		int x = e.getX();
		int y = e.getY();
		if (y < 15+15*swatchItems.length && y > 15)
		{
			y-=15;
			if (y%15 > 5 && x%15 > 5)
			{
				y /= 15;
				x /= 15;
				selected = swatchItems[y][x];
				addToMru(selected);
				fireActionPerformed(new ActionEvent(this, 0, getActionCommand()));
			}
		}
		else if (y > swatchItems.length * 15 + 35 && y < swatchItems.length * 15 + 45)
		{
			if (x%15 > 5)
			{
				x /= 15;
				if (mruItems[x] != null)
				{
					selected = mruItems[x];
					addToMru(selected);
					fireActionPerformed(new ActionEvent(this, 0, getActionCommand()));
				}
			}
		}
	}
	
	/**
	 *	Adds a given color to the front of the MRU color list.
	 *	
	 *	This list is shared between all color swatches.
	 **/
	public static void addToMru(Color c)
	{
		int index = -1;
		for (int i=0; i<mruItems.length; i++)
			if (mruItems[i] != null && mruItems[i].equals(c))
				index = i;
		if (index == -1)
		{
			for (int mru = mruItems.length-1; mru > 0; mru--)
				mruItems[mru] = mruItems[mru-1];
			mruItems[0] = c;
		}
		else
		{
			for (; index > 0; index--)
				mruItems[index] = mruItems[index-1];
			mruItems[0] = c;
		}
	}
	
	/**
	 *	Empty.
	 *	
	 *	Required by the MouseListener interface.
	 **/
	public void mouseReleased(MouseEvent e) {}
	
	/**
	 *	Empty.
	 *	
	 *	Required by the MouseListener interface.
	 **/
	public void mouseClicked(MouseEvent e) {}
	
	/**
	 *	Empty.
	 *	
	 *	Required by the MouseListener interface.
	 **/
	public void mouseEntered(MouseEvent e) {}
	
	/**
	 *	Empty.
	 *	
	 *	Required by the MouseListener interface.
	 **/
	public void mouseExited(MouseEvent e) {}
}
