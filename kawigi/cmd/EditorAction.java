package kawigi.cmd;
import kawigi.editor.*;
import kawigi.language.EditorLanguage;
import kawigi.properties.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

/**
 *	This is the big thing that controls common editor commands that act on a
 *	specific CodePane.
 *	
 *	Since they act on a code pane, they are local actions, and the context is
 *	the CodePane, and they should belong to a non-global dispatcher.
 *	
 *	Examples of actions implemented here include copy, paste, undo, and indent.
 **/
@SuppressWarnings("serial")
public class EditorAction extends DefaultAction
{
	/**
	 *	The CodePane this action should act on.
	 **/
	protected CodePane context;

	/**
	 *	Constructs a new EditorAction that executes the ActID on the CodePane.
	 **/
	public EditorAction(ActID cmdid, CodePane context)
	{
		super(cmdid);
		this.context = context;
	}
	
	/**
	 *	Returns true if a button for this action should appear enabled.
	 **/
	public boolean isEnabled()
	{
		switch (cmdid)
		{
			case actCut:
			case actCopy:
				return context.getSelectedText() != null;
			case actUndo:
				return context.canUndo();
			case actRedo:
				return context.canRedo();
			default:
				return true;
		}
	}
	
	/**
	 *	Runs the action of the command.
	 **/
	public void actionPerformed(ActionEvent e)
	{
		switch (cmdid)
		{
			case actCut:
				context.cut();
				break;
			case actCopy:
				context.copy();
				break;
			case actPaste:
				context.paste();
				break;
			case actSelectAll:
				context.selectAll();
				break;
			case actUndo:
				context.undo();
				break;
			case actRedo:
				context.redo();
				break;
			case actScrollUp:
			{
				Point loc = ((JViewport)context.getParent()).getViewPosition();
				loc.y = Math.max(0, loc.y - context.getScrollableUnitIncrement(context.getParent().getBounds(), SwingConstants.VERTICAL, -1));
				((JViewport)context.getParent()).setViewPosition(loc);
				break;
			}
			case actScrollDown:
			{
				Point loc = ((JViewport)context.getParent()).getViewPosition();
				loc.y = Math.min(context.getSize().height-context.getParent().getSize().height, loc.y + context.getScrollableUnitIncrement(context.getParent().getBounds(), SwingConstants.VERTICAL, -1));
				((JViewport)context.getParent()).setViewPosition(loc);
				break;
			}
			case actIndent:
			{
				if (context.getSelectionStart() == context.getSelectionEnd())
				{
					context.replaceSelection("\t");
				}
				else
				{
					StyledDocument doc = context.getStyledDocument();
					Element root = doc.getRootElements()[0];
					int startLine = root.getElementIndex(context.getSelectionStart());
					int endLine = root.getElementIndex(context.getSelectionEnd()-1);
					String text = context.getText().replaceAll("\\r", "");
					for (int i=endLine; i>= startLine; i--)
					{
						Element line = root.getElement(i);
						text = text.substring(0, line.getStartOffset()) + "\t" + text.substring(line.getStartOffset());
					}
					context.setText(text);
					context.setCaretPosition(root.getElement(endLine).getEndOffset()-1);
					context.setSelectionStart(root.getElement(startLine).getStartOffset());
				}
				break;
			}
			case actOutdent:
			{
				if (context.getSelectionStart() == context.getSelectionEnd())
				{
					context.replaceSelection("\t");
				}
				else
				{
					StyledDocument doc = context.getStyledDocument();
					Element root = doc.getRootElements()[0];
					int startLine = root.getElementIndex(context.getSelectionStart());
					int endLine = root.getElementIndex(context.getSelectionEnd()-1);
					String text = context.getText().replaceAll("\\r", "");
					int tabwidth = PrefFactory.getPrefs().getInt("kawigi.editor.tabstop");
					for (int i=endLine; i>= startLine; i--)
					{
						Element line = root.getElement(i);
						if (text.charAt(line.getStartOffset()) == '\t')
							text = text.substring(0, line.getStartOffset()) + text.substring(line.getStartOffset()+1);
						else if (text.charAt(line.getStartOffset()) == ' ')
						{
							int start = line.getStartOffset();
							int end;
							for (end = start; end < start + tabwidth && text.charAt(end) == ' '; end++)
								;
							if (end < start + tabwidth && text.charAt(end) == '\t')
								end ++;
							text = text.substring(0, start) + text.substring(end);
						}
					}
					context.setText(text);
					context.setCaretPosition(root.getElement(endLine).getEndOffset()-1);
					context.setSelectionStart(root.getElement(startLine).getStartOffset());
				}
				break;
			}
			case actDeletePreviousWord:
			{
				int currentIndex = context.getCaretPosition();
				String text = context.getText().replaceAll("\\r", "");
				int start = currentIndex-1;
				if (start >= 0 && text.charAt(start) != '\n')
				{
					while (start > 0 && Character.isWhitespace(text.charAt(start)) && text.charAt(start) != '\n')
						start --;
					if (start > 0 && (Character.isLetterOrDigit(text.charAt(start)) || text.charAt(start) == '_'))
					{
						do
						{
							start--;
						}
						while (start >= 0 && (Character.isLetterOrDigit(text.charAt(start)) || text.charAt(start) == '_'));
					}
					else if (start >= 0 && text.charAt(start) != '\n')
					{
						do
						{
							start--;
						}
						while (start >= 0 && !(Character.isLetterOrDigit(text.charAt(start)) || text.charAt(start) == '_') && !Character.isWhitespace(text.charAt(start)));
					}
				}
				else if (start >= 0)
					start--;
				
				context.setText(text.substring(0, start+1) + text.substring(currentIndex));
				context.setCaretPosition(start+1);
				break;
			}
			case actDeleteNextWord:
			{
				int currentIndex = context.getCaretPosition();
				String text = context.getText().replaceAll("\\r", "");
				int start = currentIndex;
				while (start < text.length() && Character.isWhitespace(text.charAt(start)) && text.charAt(start) != '\n')
					start ++;
				if (start < text.length() && (Character.isLetterOrDigit(text.charAt(start)) || text.charAt(start) == '_'))
				{
					do
					{
						start++;
					}
					while (start < text.length() && (Character.isLetterOrDigit(text.charAt(start)) || text.charAt(start) == '_'));
				}
				else if (start < text.length() && text.charAt(start) != '\n')
				{
					do
					{
						start++;
					}
					while (start < text.length() && !(Character.isLetterOrDigit(text.charAt(start)) || text.charAt(start) == '_') && !Character.isWhitespace(text.charAt(start)));
				}
				else if (start < text.length())
					start++;
				
				context.setText(text.substring(0, currentIndex) + text.substring(start));
				context.setCaretPosition(currentIndex);
				break;
			}
			case actNewLine:
			{
				String text = context.getText().replaceAll("\\r", "");
				String line = "";
				String indentation = "";
				int lines = 0;
				for (int ind = -1; ind < context.getCaretPosition(); ind = text.indexOf('\n', ind+1))
				{
					if (ind < 0 && lines != 0)
						break;
					lines++;
					line = (text.indexOf('\n', ind+1) < 0) ? text.substring(ind+1) : text.substring(ind+1, text.indexOf('\n', ind+1));
					indentation = (line.trim().length() == 0) ? line : line.substring(0, Math.min(line.indexOf(line.trim()), context.getCaretPosition()-1-ind));
				}
				indentation = "\n" + indentation;
				context.replaceSelection(indentation);
				break;
			}
			case actCtxMenu:
			{
				Point pt = context.getCaret().getMagicCaretPosition();
				if (pt == null)
					pt = new Point();
				JPopupMenu popup = (JPopupMenu)UIHandler.loadMenu(MenuID.EditorContextMenu, context.getDispatcher());
				popup.show(context, pt.x, pt.y);
				break;
			}
			case actInsertTestCode:
			{
				context.replaceSelection(EditorLanguage.sTestingCodeTag);
				break;
			}
		}
	}
}
