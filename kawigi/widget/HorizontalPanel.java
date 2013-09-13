package kawigi.widget;
import javax.swing.*;

/**
 *	A JPanel with a horizontal BoxLayout set.
 *	
 *	This makes it easier to lay out the UI in XML.
 **/
@SuppressWarnings("serial")
public class HorizontalPanel extends JPanel
{
	/**
	 *	Constructs a new HorizontalPanel.
	 **/
	public HorizontalPanel()
	{
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	}
	
	/**
	 *	Sets a new titled border around this panel with the given title.
	 **/
	public void setBorderTitle(String title)
	{
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), title));
	}
}
