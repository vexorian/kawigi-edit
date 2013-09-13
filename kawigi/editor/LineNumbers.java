package kawigi.editor;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.beans.*;

/**
 *	A custom widgets creating line numbers to the side of a JTextPane that also
 *	provides some interaction with the text pane.
 *	
 *	It will use the same font (but bolded) as the JText pane and the same colors.
 *	By clicking or clicking and dragging on numbers, you can select lines of text
 *	in the text pane.
 **/
@SuppressWarnings("serial")
public class LineNumbers extends JComponent implements DocumentListener, MouseListener, MouseMotionListener,
													   PropertyChangeListener, CaretListener, HierarchyListener
{
	/**
	 *	Text pane that we are numbering.
	 **/
	private JTextPane textarea;
	/**
	 *	Number of lines of text in the text pane on our last update.
	 **/
	private int currentLines;
	/**
	 *	Number of pixels between lines.
	 **/
	private int lineWidth;
	/**
	 *	Line number where the user started a click-and-drag operation.
	 **/
	private int anchor;
	/**
	 *	Line number where the user is dragging/dragged to when selecting lines.
	 **/
	private int lastIndex;
	/**
	 *	Offset of where line numbers should be drawn from the top of the window.
	 **/
	private int offset;
	/**
	 *	Maximum width of a line as of the last update.
	 **/
	private int textwidth;
	/**
	 *	View that renders text on the text pane.
	 **/
	private GenericView view;
	/**
	 * Graphics which will be used for setting correct size of the component  
	 */
	private Graphics graphics;
	
	/**
	 *	Creates an instance of <code>LineNumbers</code> to go next to
	 *	<code>textarea</code>.
	 *	
	 *	In general, <code>textarea</code> should be put on a JScrollPane and
	 *	this should be set as the row header view.
	 **/
	public LineNumbers(JTextPane textarea)
	{
		this.textarea = textarea;
		textarea.getDocument().addDocumentListener(this);
		setForeground(textarea.getForeground());
		setBackground(textarea.getBackground());
		currentLines = 1;
		lastIndex = -1;
		anchor = -1;
		addMouseListener(this);
		addMouseMotionListener(this);
		textarea.addMouseListener(this);
		textarea.addHierarchyListener(this);
		view = (GenericView)textarea.getEditorKit().getViewFactory().create(textarea.getDocument().getDefaultRootElement());
		setPreferredSize(new Dimension(24, 17));	//<---- this is the right starting size if they use the default font and size
		textarea.addPropertyChangeListener(this);
		textarea.addCaretListener(this);
		setFocusable(false);

		BufferedImage im = new BufferedImage(24, 17, BufferedImage.TYPE_INT_RGB);
		graphics = im.createGraphics();
	}
	
	/**
	 *	Listens for changes on the Document in its associated text pane.
	 *	
	 *	If the number of lines has changed, it updates its view.
	 **/
	public void changedUpdate(DocumentEvent e)
	{
		anchor = lastIndex = -1;
		try
		{
			checkLines(e.getDocument().getText(0, e.getDocument().getLength()));
		}
		catch (BadLocationException ex)
		{
		}
	}
	
	/**
	 *	Listens for changes on the Document in its associated text pane.
	 *	
	 *	If the number of lines has changed, it updates its view.
	 **/
	public void insertUpdate(DocumentEvent e)
	{
		anchor = lastIndex = -1;
		try
		{
			checkLines(e.getDocument().getText(0, e.getDocument().getLength()));
		}
		catch (BadLocationException ex)
		{
		}
	}
	
	/**
	 *	Listens for changes on the Document in its associated text pane.
	 *	
	 *	If the number of lines has changed, it updates its view.
	 **/
	public void removeUpdate(DocumentEvent e)
	{
		anchor = lastIndex = -1;
		try
		{
			checkLines(e.getDocument().getText(0, e.getDocument().getLength()));
		}
		catch (BadLocationException ex)
		{
		}
	}

	/**
	 * Fix lineWidth variable with info from FontMetrics if called for the first time
	 * 
	 * @param fm	metrics to get info from
	 */
	private void fixLineWidth(FontMetrics fm)
	{
		if (lineWidth == 0) {
			lineWidth = fm.getHeight();
			offset = -fm.getDescent()/2;
		}
	}

	/**
	 * Get size of space reserved for line numbers in component;
	 *  
	 * @param fm	font metrics to get info from
	 * @return		width of text to reserve
	 */
	private int getTextWidth(FontMetrics fm)
	{
		return Math.max(textwidth, fm.stringWidth("000"));
	}

	/**
	 * Check (and fix if needed) that minimum and preferred sizes are sufficient
	 * to accommodate all line numbers
	 *  
	 * @param fm	Font metrics used for getting information about font sizes
	 */
	private void fixPrefferedSize(FontMetrics fm)
	{
		int need_width = getTextWidth(fm) + 8;
		int need_height = lineWidth * currentLines + 8;
		Dimension dim = getPreferredSize();
		if (dim.width != need_width || dim.height != need_height) {
			dim = new Dimension(need_width, need_height);
			setPreferredSize(dim);
			setMinimumSize(dim);

			repaint();
			//getParent().invalidate();
			//paintImmediately(0, 0, dim.width, dim.height);
		}
	}
	
	/**
	 *	Called by the DocumentListener methods to check if the number of lines
	 *	has changed, and if it has, it updates the display.
	 **/
	protected void checkLines(String text)
	{
		int lines = 0;
		int index = -1;
		do
		{
			lines++;
			index = text.indexOf('\n', index+1);
		}
		while (index >= 0);
		if (lines != currentLines)
		{
			currentLines = lines;
			
			graphics.setFont(textarea.getFont().deriveFont(Font.BOLD));
			FontMetrics fm = graphics.getFontMetrics();
			fixLineWidth(fm);

			fixPrefferedSize(fm);
		}
	}
	
	/**
	 *	Draws the old line numbers.
	 **/
	public void paint(Graphics g)
	{
		g.setFont(textarea.getFont().deriveFont(Font.BOLD));
		FontMetrics fm = g.getFontMetrics();
		fixLineWidth(fm);
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(textarea.getSelectionColor());
		int start = Math.max(1, Math.min(anchor, lastIndex));
		int end = Math.min(currentLines, Math.max(anchor, lastIndex));
		g.fillRect(0, (start-1)*lineWidth-offset, textwidth, (end-start+1)*lineWidth);
		int maxwidth = getTextWidth(fm);
		for (int i=1; i<= currentLines; i++)
		{
			String str = Integer.toString(i);
			maxwidth = Math.max(maxwidth, fm.stringWidth(str));
			if (i >= start && i <= end)
				g.setColor(textarea.getSelectedTextColor());
			else
				g.setColor(getForeground());
			g.drawString(str, textwidth-fm.stringWidth(str), i*lineWidth+offset);
		}
		textwidth = maxwidth;
		g.setColor(getForeground());
		ArrayList<Interval> intervals = view.getIntervals();
		Interval use = null;
		int caret = textarea.getCaretPosition();
		for (int i=0; i<intervals.size(); i++)
		{
			Interval in = intervals.get(i);
			if (in.isBlock() && caret >= in.getStartIndex() && caret <= in.getEndIndex() && (use == null || in.getEndLine() - in.getStartLine() < use.getEndLine() - use.getStartLine()))
				use = in;
		}
		if (use != null)
		{
			int plusy = use.getStartLine()*lineWidth+offset-fm.getAscent()/2;
			int bottomy = use.getEndLine()*lineWidth+offset-fm.getAscent()/2;
			g.drawLine(maxwidth+4, plusy, maxwidth+4, bottomy);
			g.drawLine(maxwidth+5, plusy, maxwidth+5, bottomy);
			g.drawLine(maxwidth+4, bottomy, maxwidth+8, bottomy);
			g.drawLine(maxwidth+4, plusy, maxwidth+8, plusy);
		}
		g.drawLine(maxwidth+2, 0, maxwidth+2, getHeight());
		g.drawLine(maxwidth+1, 0, maxwidth+1, getHeight());
	}
	
	/**
	 *	Handles selection in the text pane due to gestures on the line numbers.
	 **/
	protected void doSelection()
	{
		int first = Math.min(anchor, lastIndex);
		int last = Math.max(anchor, lastIndex);
		try
		{
			String text = textarea.getDocument().getText(0, textarea.getDocument().getLength());
			int index = -1, lines = 1;
			while (lines < first)
			{
				index = text.indexOf('\n', index+1);
				lines++;
			}
			int firstindex = index+1;
			do
			{
				index = text.indexOf('\n', index+1);
				lines++;
			}
			while (lines <= last && index > 0);
			int lastindex;
			if (index < 0)
				lastindex = textarea.getDocument().getLength();
			else
				lastindex = index+1;
			textarea.setSelectionStart(firstindex);
			textarea.setSelectionEnd(lastindex);
		}
		catch (BadLocationException ex)
		{
		}
	}
	
	/**
	 *	Selects the number that was clicked on and sets it as an "anchor" for
	 *	dragging.
	 **/
	public void mousePressed(MouseEvent e)
	{
		if (e.getSource() == this)
		{
			if (e.getX() < textwidth)
			{
				anchor = e.getY()/lineWidth+1;
				lastIndex = anchor;
				doSelection();
				repaint();
			}
		}
		else
		{
			anchor = lastIndex = -1;
			repaint();
		}
	}
	
	/**
	 *	Sets the end anchor and updates the state of the widget to reflect that
	 *	the click-and-drag gesture has ended.
	 **/
	public void mouseReleased(MouseEvent e)
	{
		if (e.getSource() == this)
		{
			if (e.getX() < textwidth)
			{
				if (lastIndex != e.getY()/lineWidth+1)
				{
					lastIndex = e.getY()/lineWidth+1;
					doSelection();
					repaint();
				}
			}
		}
		else
		{
			anchor = lastIndex = -1;
			repaint();
		}
	}
	
	/**
	 *	Temporarily moves the end anchor of the current selection.
	 **/
	public void mouseDragged(MouseEvent e)
	{
		if (lastIndex != e.getY()/lineWidth+1)
		{
			if (e.getX() < textwidth)
			{
				lastIndex = e.getY()/lineWidth+1;
				doSelection();
				repaint();
			}
		}
	}
	
	/**
	 *	Empty - part of the MouseMotionListener interface.
	 **/
	public void mouseMoved(MouseEvent e){}
	
	/**
	 *	Empty - part of the MouseListener interface.
	 **/
	public void mouseEntered(MouseEvent e){}
	
	/**
	 *	Empty - part of the MouseListener interface.
	 **/
	public void mouseExited(MouseEvent e){}
	
	/**
	 *	Empty - part of the MouseListener interface.
	 **/
	public void mouseClicked(MouseEvent e){}
	
	/**
	 *	Listens for property changes on the text pane, specifically to its
	 *	document (so I can become a DocumentListener on the new Document).
	 **/
	public void propertyChange(PropertyChangeEvent e)
	{
		if (e.getOldValue() instanceof Document)
		{
			view = (GenericView)textarea.getEditorKit().getViewFactory().create(textarea.getDocument().getDefaultRootElement());
			((Document)e.getNewValue()).addDocumentListener(this);
		}
	}
	
	/**
	 *	Listens for changes in caret position on the text pane.
	 *	
	 *	Updates the code block display and stuff
	 **/
	public void caretUpdate(CaretEvent e)
	{
		repaint();
	}

	public void hierarchyChanged(HierarchyEvent e) {
		if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
			// Some bug in Java causes to not repaint this component after resizing
			// Symptoms: open problem (or generate code in standalone) and without doing
			// anything else change tab to "Test code" - line numbers will not be shown
			// until you click in editor or press any movement key while cursor is in editor
			Dimension dim = getPreferredSize();
			paintImmediately(0, 0, dim.width, dim.height);
		}
	}
}
