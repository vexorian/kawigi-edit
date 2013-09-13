package kawigi.widget;
import javax.swing.*;

/**
 *	A JPanel with a vertical BoxLayout applied.
 **/
@SuppressWarnings("serial")
public class VerticalPanel extends JPanel
{
	/**
	 *	Constructs a new VerticalPanel and sets its layout.
	 **/
	public VerticalPanel()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	}
	
	/**
	 *	Sets a titled border around this panel with the given title.
	 **/
	public void setBorderTitle(String title)
	{
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), title));
	}
}
