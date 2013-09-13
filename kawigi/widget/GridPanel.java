package kawigi.widget;
import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
public class GridPanel extends JPanel
{
	public GridPanel()
	{
	}
	
	public void setGridDimensions(String s)
	{
		String[] parts = s.split(",");
		setLayout(new GridLayout(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim())));
	}
	
	public void setBorderTitle(String title)
	{
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), title));
	}
}
