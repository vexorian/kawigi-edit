package kawigi.widget;
import kawigi.cmd.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;

/**
 *	Implementation of a button that drops a menu that has a color swatch on it.
 *	
 *	[rant length="short"]
 *	There should be a much simpler way to do this.  Unfortunately, JMenus can't
 *	be added to anything but JMenuBars and expect to actually work.  Therefore,
 *	I have to position my own Dropdown from a JToggleButton, which won't work
 *	as consistently as a regular Java-implemented menu would (because the menu
 *	might go off the edge of the screen).
 *	[/rant]
 **/
@SuppressWarnings("serial")
public class ColorSwatchDropdown extends JToggleButton implements ActionListener, PropertyChangeListener, PopupMenuListener
{
	/**
	 *	Currently selected color on this dropdown.
	 *	
	 *	Should always be kept in sync with the selected color on the swatch.
	 **/
	protected Color currentColor;
	/**
	 *	Menu item to launch the "More Colors" dialog.
	 **/
	protected JMenuItem moreColorsItem;
	/**
	 *	ColorSwatch grid to go on the menu.
	 **/
	protected ColorSwatch swatch;
	/**
	 *	Action that this dropdown is bound to.
	 **/
	protected Action a;
	/**
	 *	Menu that pops out when the user uses this dropdown.
	 **/
	protected JPopupMenu popupMenu;
	
	/**
	 *	Creates and populates the popup menu.
	 **/
	protected void addComponents()
	{
		swatch = new ColorSwatch();
		swatch.addActionListener(this);
		moreColorsItem = new JMenuItem("More Colors...");
		moreColorsItem.addActionListener(this);
		moreColorsItem.setMnemonic(KeyEvent.VK_M);
		popupMenu = new JPopupMenu();
		popupMenu.add(swatch);
		popupMenu.add(moreColorsItem);
		popupMenu.addPopupMenuListener(this);
		addActionListener(this);
	}
	
	/**
	 *	Constructs a new ColorSwatchDropdown bound to the given action.
	 **/
	public ColorSwatchDropdown(Action a)
	{
		super(a);
		this.a = a;
		addComponents();
		currentColor = (Color)a.getValue(DefaultAction.COLOR);
		if (currentColor == null)
			currentColor = Color.black;
		Icon icon = (Icon)a.getValue(Action.SMALL_ICON);
		if (icon != null)
			a.putValue(Action.SMALL_ICON, new ChipIcon(icon, this));
		a.addPropertyChangeListener(this);
	}
	
	/**
	 *	Sets the color of this dropdown and its swatch.
	 **/
	public void setColor(Color c)
	{
		currentColor = c;
		swatch.setSelected(c);
		repaint();
		a.putValue(DefaultAction.COLOR, c);
		Dispatcher.getGlobalDispatcher().UIRefresh();
	}
	
	/**
	 *	Gets the color selected by this dropdown.
	 **/
	public Color getColor()
	{
		return currentColor;
	}
	
	/**
	 *	Executes actions on the menu anchor button or on the popup menu.
	 **/
	public void actionPerformed(ActionEvent e)
	{
		if (e == null)
			return;
		if (e.getSource() == swatch)
		{
			popupMenu.setVisible(false);
			setColor(swatch.getSelected());
		}
		else if (e.getSource() == moreColorsItem)
		{
			popupMenu.setVisible(false);
			Color c = JColorChooser.showDialog(Dispatcher.getWindow(), getText(), currentColor);
			if (c != null)
			{
				setColor(c);
			}
		}
		// If the anchor button was pushed, we need to show the menu (or hide it
		// if it was already showing.  This is pretty normal behavior except in
		// applications where the UI was written from scratch by people who
		// didn't know this.
		else if (e.getSource() == this)
		{
			if (!popupMenu.isVisible())
				popupMenu.show(this, 0, getHeight());
			else
				popupMenu.setVisible(false);
		}
	}
	
	/**
	 *	Returns the background color of this button.
	 *	
	 *	This will normally be the selected color.
	 **/
	public Color getBackground()
	{
		if (getColor() == null)
			return super.getBackground();
		return getColor();
	}
	
	/**
	 *	Returns the foreground color.
	 *	
	 *	If the current background color is dark, the forground color will be
	 *	white, otherwise it will be black.
	 **/
	public Color getForeground()
	{
		Color c = getColor();
		if (c == null)
			return super.getForeground();
		if (c.getRed() + c.getGreen() + c.getBlue() - (255*3/2) >= 0)
			return Color.black;
		else
			return Color.white;
	}
	
	/**
	 *	Processes property changes from the action.
	 **/
	public void propertyChange(PropertyChangeEvent e)
	{
		if (e.getPropertyName().equals(DefaultAction.COLOR))
		{
			if (!e.getNewValue().equals(getColor()))
				setColor((Color)e.getNewValue());
		}
	}
	
	/**
	 *	Called when the popup menu is cancelled.
	 **/
	public void popupMenuCanceled(PopupMenuEvent e)
	{
		setSelected(false);
	}
	
	/**
	 *	Called when the popup menu is about to disappear.
	 **/
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
	{
		setSelected(false);
	}
	
	/**
	 *	Called when the popup menu is about to appear.
	 **/
	public void popupMenuWillBecomeVisible(PopupMenuEvent e)
	{
		setSelected(true);
	}
}
