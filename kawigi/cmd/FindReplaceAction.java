package kawigi.cmd;
import kawigi.editor.*;
import java.awt.event.*;
import java.util.regex.*;
import javax.swing.*;

/**
 *	Action implementation for Find/Replace related actions.
 *	
 *	Actions here are <i>not</i> global, because they act on a specific CodePane.
 **/
@SuppressWarnings("serial")
public class FindReplaceAction extends DefaultAction
{
	/**
	 *	The CodePane that this action should act on.
	 **/
	protected CodePane codepane;
	
	/**
	 *	Constructs a new FindReplaceAction to execute the given ActID on the
	 *	given CodePane.
	 **/
	public FindReplaceAction(ActID cmdid, CodePane codepane)
	{
		super(cmdid);
		this.codepane = codepane;
	}
	
	/**
	 *	Returns true if this Action should show up as enabled.
	 **/
	public boolean isEnabled()
	{
		FindReplaceContext context = codepane.getFindReplaceContext();
		switch (cmdid)
		{
			case actFindDlg:
			case actReplaceDlg:
				if (context.dialogShowing())
					return (context.isShowingReplace()) == (cmdid == ActID.actFindDlg);
				else
					return true;
			case actFindNext:
				return context.getSearchString().length() > 0;
			case actReplaceAll:
				return context.isShowingReplace() && context.getSearchString().length() > 0;
			case actReplace:
				return context.isShowingReplace() && (codepane.getSelectedText() != null) && context.getCurrentPattern().matcher(codepane.getSelectedText()).matches();
			case actReplaceField:
				return context.isShowingReplace();
			case actCloseFindReplaceDlg:
				return context.dialogShowing();
			case actLiteral:
			case actWildCards:
			case actRegex:
				// Take this opportunity to validate selection:
				putValue(SELECTED, getValue(SELECTED));
				// fallthrough:
			default:
				return true;
		}
	}
	
	/**
	 *	Returns true if this action should be showing at all.
	 **/
	public boolean isVisible()
	{
		FindReplaceContext context = codepane.getFindReplaceContext();
		if (!context.isShowingReplace())
		{
			if (cmdid == ActID.actReplaceField || cmdid == ActID.actReplaceAll || cmdid == ActID.actReplace)
				return false;
		}
		else
			return cmdid != ActID.actFindDlg;
		return true;
	}
	
	/**
	 *	Runs this action.
	 **/
	public void actionPerformed(ActionEvent e)
	{
		FindReplaceContext context = codepane.getFindReplaceContext();
		switch (cmdid)
		{
			case actFindDlg:
			case actReplaceDlg:
				context.showing(cmdid == ActID.actReplaceDlg);
				break;
			case actReplace:
			case actReplaceField:
				doReplacement();
				// fallthrough:
			case actFindNext:
			case actFindField:
				findNext();
				break;
			case actReplaceAll:
				doReplaceAll();
				break;
			case actCloseFindReplaceDlg:
				JDialog dialog = context.getDialog();
				if (dialog != null)
					dialog.setVisible(false);
				break;
			// These 5 could have been done by the modified controls, but it
			// made sense to me to just tell them what their state is rather
			// than have them tell me.
			case actCaseSensitive:
				context.toggleCaseSensitive();
				putValue(SELECTED, Boolean.valueOf(context.getCaseSensitive()));
				break;
			case actWholeWord:
				context.toggleWholeWord();
				putValue(SELECTED, Boolean.valueOf(context.getWholeWord()));
				break;
			case actLiteral:
				context.setUseLiteral();
				putValue(SELECTED, Boolean.TRUE);
				break;
			case actWildCards:
				context.setUseWildcards();
				putValue(SELECTED, Boolean.TRUE);
				break;
			case actRegex:
				context.setUseRegex();
				putValue(SELECTED, Boolean.TRUE);
				break;
		}
		Dispatcher.getGlobalDispatcher().UIRefresh();
	}
	
	/**
	 *	The simpler side of the magic that finds and highlights the next match.
	 **/
	private void findNext()
	{
		Pattern p = codepane.getFindReplaceContext().getCurrentPattern();
		// for some reason, getText() trims off \r but the indexes in
		// the editor pane don't.
		String text = codepane.getText().replaceAll("\\r", "");
		Matcher m = p.matcher(text);
		int index = codepane.getSelectionEnd();
		if (!m.find(index))
			if (!m.find())
			{
				JOptionPane.showMessageDialog(codepane, "No matches found!", "No Matches!", JOptionPane.PLAIN_MESSAGE);
				return;
			}
		codepane.setSelectionStart(m.start());
		codepane.setSelectionEnd(m.end());
	}
	
	/**
	 *	Does the replacement action with the replacement pattern if the current
	 *	selection matches the search string.
	 **/
	public void doReplacement()
	{
		String text = codepane.getSelectedText();
		if (text == null)
		{
			//no selection
			return;
		}
		FindReplaceContext context = codepane.getFindReplaceContext();
		Pattern p = context.getCurrentPattern();
		Matcher m = p.matcher(text);
		if (m.matches())
		{
			if (context.useRegex())
				//allow capturing and cool stuff with regex replace.  Advanced feature :-)
				codepane.replaceSelection(m.replaceFirst(context.getReplacement()));
			else
				codepane.replaceSelection(context.getReplacement());
		}
	}
	
	/**
	 *	Does a global replacement with the current search and replacement
	 *	strings.
	 **/
	public void doReplaceAll()
	{
		FindReplaceContext context = codepane.getFindReplaceContext();
		Pattern p = context.getCurrentPattern();
		
		String text = codepane.getSelectedText() == null ? codepane.getText() : codepane.getSelectedText();
		Matcher m = p.matcher(text);
		String replacement = context.getReplacement();
		if (!context.useRegex())
		{
			String newreplacement = "";
			for (int i=0; i<replacement.length(); i++)
			{
				if (replacement.charAt(i) == '$' || replacement.charAt(i) == '\\')
					newreplacement += "\\" + replacement.charAt(i);
				newreplacement += replacement.charAt(i);
			}
			replacement = newreplacement;
		}
		if (codepane.getSelectedText() == null)
			codepane.setText(m.replaceAll(replacement));
		else
			codepane.replaceSelection(m.replaceAll(replacement));
	}
	
	/**
	 *	Overridden to override the TEXT property of text fields and SELECTED
	 *	property of the check boxes and radio buttons.
	 **/
	public Object getValue(String key)
	{
		FindReplaceContext context = codepane.getFindReplaceContext();
		if (key.equals(TEXT))
		{
			if (cmdid == ActID.actFindField)
				return context.getSearchString();
			else if (cmdid == ActID.actReplaceField)
				return context.getReplacement();
		}
		else if (key.equals(SELECTED))
		{
			switch (cmdid)
			{
				case actWholeWord:
					return context.getWholeWord();
				case actCaseSensitive:
					return context.getCaseSensitive();
				case actLiteral:
					return context.useLiteral();
				case actWildCards:
					return context.useWildcards();
				case actRegex:
					return context.useRegex();
			}
		}
		return super.getValue(key);
	}
	
	/**
	 *	Overridden to save the TEXT property from the text fields and the
	 *	SELECTED property from the checkboxes and radio buttons.
	 **/
	public void putValue(String key, Object value)
	{
		if (codepane != null)
		{
			FindReplaceContext context = codepane.getFindReplaceContext();
			if (key.equals(TEXT))
			{
				if (cmdid == ActID.actFindField)
					context.setSearchString((String)value);
				else if (cmdid == ActID.actReplaceField)
					context.setReplacement((String)value);
			}
			else if (key.equals(SELECTED))
			{
				boolean state = ((Boolean)value).booleanValue();
				switch (cmdid)
				{
					case actWholeWord:
						if (state != context.getWholeWord())
							context.toggleWholeWord();
						break;
					case actCaseSensitive:
						if (state != context.getCaseSensitive())
							context.toggleCaseSensitive();
						break;
					case actLiteral:
						if (state)
							context.setUseLiteral();
						break;
					case actWildCards:
						if (state)
							context.setUseWildcards();
						break;
					case actRegex:
						if (state)
							context.setUseRegex();
						break;
				}
			}
		}
		super.putValue(key, value);
	}
}
