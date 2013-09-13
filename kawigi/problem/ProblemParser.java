package kawigi.problem;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.*;

import kawigi.cmd.ProblemContext;
import kawigi.language.*;
// Class works hard with strings. So code looks much better if this StringsUtil name
// doesn't appear everywhere
import static kawigi.util.StringsUtil.*;

/**
 *	This is used to create the ClassDecl so that we can generate code for
 *	problems in standalone.
 *
 *	The parsing process is far from perfect here, and I obviously just haven't
 *	put enough time into writing it and testing it.  If someone wants to put
 *	some effort into it and make it work in more (or all) cases, be my guest,
 *	and let me know if you want your contributions to go to the final product.
 **/
@SuppressWarnings("serial")
public final class ProblemParser extends JDialog implements ActionListener, ClassDeclGenerator
{
	/**
	 * Text box that the problem statement is pasted into.
	 **/
	private JTextPane textArea;
	/**
	 * Button you push to start the parsing.
	 **/
	private JButton parseButton;
	/**
	 * Combo box to select the language you want the code generated in.
	 **/
	private JComboBox languageBox;
	/**
	 * Resulting ClassDecl.
	 **/
	private static ClassDecl retval;

	/*
	  ============================================================================
		Several constants for problem statement parsing
	 */
	/**
	 * Prefix at the line in problem statement where class name is written.
	 */
	private static final String sClassPrefix = "Class:";
	/**
	 * Prefix at the line in problem statement where method name is written.
	 */
	private static final String sMethodPrefix = "Method:";
	/**
	 * Prefix at the line in problem statement where parameters' types are written.
	 */
	private static final String sParamsPrefix = "Parameters:";
	/**
	 * Prefix at the line in problem statement where return type is written.
	 */
	private static final String sRetPrefix = "Returns:";
	/**
	 * Prefix at the line in problem statement where full method signature is written.
	 */
	private static final String sSignPrefix = "Method signature:";
	/**
	 * Prefix at the line in problem statement where return value of test case is written.
	 */
	private static final String sTestRetPrefix = "Returns: ";
	//============================================================================

	/*
	  ============================================================================
		Variables used wide across different functions involved in
		problem statement parsing
	 */
	/**
	 * String buffer used in searching EditorDataType by type name
	 */
	private static final StringBuilder typeSearch = new StringBuilder(20);
	/**
	 * Full text of problem given to editor
	 */
	private static final StringBuilder problemText = new StringBuilder(2000);
	/**
	 * Current position in problem text where parser is now
	 */
	private static int curParsePos;
	/**
	 * Matcher for finding line endings in problem statement
	 */
	private static Matcher matEndLine;
	/**
	 * Additional variable for flagging if last search of line ending was successful
	 */
	private static boolean endLineFound;
	//============================================================================


	/**
	 * Constructs a problem parser ready to prompt the user for a problem
	 * statement.
	 **/
	public ProblemParser()
	{
		super((JFrame)null, "Paste the Problem Statement", true);
		JPanel panel = new JPanel(new BorderLayout());
		textArea = new JTextPane();
		panel.add(new JScrollPane(textArea));
		parseButton = new JButton("Parse!");
		parseButton.addActionListener(this);
		languageBox = new JComboBox(new Object[]{"C++", "Java", "Python", "C#", "VB" });
		JPanel bottomPanel = new JPanel();
		bottomPanel.add(new JLabel("Language"));
		bottomPanel.add(languageBox);
		bottomPanel.add(parseButton);
		panel.add(bottomPanel, BorderLayout.SOUTH);
		setContentPane(panel);
		setSize(500, 300);
	}

	/**
	 * Process events from the Problem Parser dialog.
	 **/
	public void actionPerformed(ActionEvent e)
	{
		EditorLanguage lang = LanguageFactory.getLanguage(languageBox.getSelectedItem().toString());
		String text = textArea.getText();
		problemText.setLength(0);
		// Add new line to avoid some possibly wrong results in parsing
		problemText.append(text).append('\n');
		ProblemContext.setLanguage(lang);
		parseClassDecl();
		setVisible(false);
	}

	/**
	 *	Shows the dialog, and when the dialog is closed, returns the ClassDecl
	 *	that resulted from it.
	 **/
	public ClassDecl getClassDecl(Object ... parameters)
	{
		setVisible(true);
		return retval;
	}

	/**
	 *	Parsing text entered in the dialog earlier once more.
	 **/
	public ClassDecl reparseClassDecl()
	{
		parseClassDecl();
		return retval;
	}

	/*
	  ============================================================================
		Functions involved in problem parsing process. All functions made static
		because they must not depend on concrete instance.
	 */

	/**
	 * Checks if parser can proceed its work and moves pointer to line ending
	 * if it's necessary.
	 *
	 * @return      Flag if parser can proceed working
	 */
	private static boolean canProceed()
	{
		if (endLineFound && matEndLine.start() < curParsePos)
			endLineFound = matEndLine.find(curParsePos);
		return endLineFound;
	}

	/**
	 * Shifts current parsing position to next non-white-space character
	 * and checks if parser can work further (if we are not at the end of the text.
	 *
	 * @return      Flag if parser can proceed working
	 */
	private static boolean canShiftParsePos()
	{
		boolean res = false;
		if (canProceed())
		{
			curParsePos = getFirstNonSpaceInd(problemText, matEndLine.end());
			res = canProceed();
		}
		return res;
	}

	/**
	 * Checks if some prefix string appears at the current parsing position.
	 * If so then moves current parsing position at the first non-space character
	 * after prefix. Pointer to line ending is adjusted too if necessary.
	 *
	 * @param prefix        Prefix to look for in problem statement
	 * @return              Flag if prefix was met in problem text
	 */
	private static boolean isPrefix(String prefix)
	{
		boolean res = false;
		if (isStringAt(problemText, prefix, curParsePos)) {
			curParsePos = getFirstNonSpaceInd(problemText, curParsePos + prefix.length());
			res = canProceed();
		}
		return res;
	}

	/**
	 * Checks if some prefix is at the current position. And if so extracts all
	 * after prefix till the end of line to the given StringBuilder.
	 *
	 * @param prefix        Prefix to look for in the problem statement
	 * @param extractRes    Buffer to put extracted result to
	 * @return              Flag if all operations successful
	 */
	private static boolean canExtractAfterPrefix(String prefix, StringBuilder extractRes)
	{
		boolean res = false;
		if (isPrefix(prefix)) {
			extractRes.setLength(0);
			appendTrimmed(extractRes, problemText, curParsePos, matEndLine.start());
			res = true;
		}
		return res;
	}

	/**
	 * Extracts types of method parameters from the problem text.
	 *
	 * @return      Array of parameters' types
	 */
	private static EditorDataType[] extractParamTypes()
	{
		// First we will collect all in the list because we don't know
		// how many of them are there. We only know that they must all fit
		// till the end of line
		ArrayList<EditorDataType> typesList = new ArrayList<EditorDataType>(5);
		while (matEndLine.start() > curParsePos)
		{
			int nextPos = indexOf(problemText, ',', curParsePos);
			if (-1 == nextPos || matEndLine.start() < nextPos)
				nextPos = getLastNonSpaceInd(problemText, matEndLine.start()) + 1;
			typeSearch.setLength(0);
			typeSearch.append(problemText, curParsePos, nextPos);
			EditorDataType type = EditorLanguage.getType(typeSearch);
			// some deffence from invalid problem statement
			if (null != type)
				typesList.add(type);
			curParsePos = getFirstNonSpaceInd(problemText, nextPos + 1);
		}
		// Adjusting current position to not skip next line in the main parsing function
		curParsePos = matEndLine.start();

		// To avoid unnecessary object creation we create array of the final length
		EditorDataType[] res = new EditorDataType[typesList.size()];
		return typesList.toArray(res);
	}

	/**
	 * Extracts names of method parameters written in method signature.
	 * At the entrance current position must point to first character after
	 * parenthesis in method signature.
	 *
	 * @param lastPos       Position in problem text where method signature ends
	 *                      (where right parenthesis is placed)
	 * @param paramsCount   Number of parameters in method
	 * @return              Array with parameters names
	 */
	private static StringBuilder[] extractParamNames(int lastPos, int paramsCount)
	{
		StringBuilder[] paramNames = new StringBuilder[paramsCount];
		for (int i = 0; paramsCount > i; ++i)
		{
			int nextPos = indexOf(problemText, ',', curParsePos);
			if (-1 == nextPos || matEndLine.start() < nextPos)
				nextPos = lastPos;
			// At least empty strings we must create for all parameters names
			paramNames[i] = new StringBuilder(10);

			// First we try to take C-like parameter name written after type name
			// It will work for most of the languages (C++, C#, Java)
			int midPos = lastIndexOf(problemText, ' ', nextPos);
			if (curParsePos < midPos)
			{
				paramNames[i].append(problemText, midPos + 1, nextPos);
				// But if this right word is valid type name then we work with
				// problem statement written in VB. In this case first word will be
				// parameter name
				if (null != EditorLanguage.getType(paramNames[i]))
				{
					midPos = indexOf(problemText, ' ', curParsePos);
					paramNames[i].setLength(0);
					paramNames[i].append(problemText, curParsePos, midPos);
				}
			}
			curParsePos = getFirstNonSpaceInd(problemText, nextPos + 1);
		}
		return paramNames;
	}

	/**
	 * Extracts information about test case (values of the parameters and valid return)
	 * from problem statement.
	 *
	 * @param lang          Language that will add information about test case to
	 *                      class declaration
	 * @param paramsCount   Number of parameters in method
	 */
	private static void extractTestcase(EditorLanguage lang, int paramsCount)
	{
		StringBuilder[] testParams = new StringBuilder[paramsCount];
		for (int i = 0; paramsCount > i; ++i)
		{
			// At least empty string we need to create for each parameter
			StringBuilder sb = new StringBuilder(20);
			testParams[i] = sb;
			if (canShiftParsePos())
			{
				appendTrimmed(sb, problemText, curParsePos, matEndLine.start());
				// Treat special case when array is split to different lines
				while ('{' == sb.charAt(0) && '}' != sb.charAt(sb.length() - 1))
				{
					if (!canShiftParsePos())
						break;
					appendTrimmed(sb, problemText, curParsePos, matEndLine.start());
				}
			}
		}
		// At the end we parse return value and add test case to class declaration
		if (canShiftParsePos())
		{
			StringBuilder result = new StringBuilder(20);
			if (canExtractAfterPrefix(sTestRetPrefix, result))
				lang.addTestCase(retval, testParams, result);
		}
	}

	/**
	 * This is actually where the parsing of problem statement happens.
	 **/
	private static void parseClassDecl()
	{
		retval = null;
		EditorLanguage lang = ProblemContext.getLanguage();

		// Initialization of parse engine
		matEndLine = Pattern.compile(sCRLFregex).matcher(problemText);
		endLineFound = matEndLine.find(0);
		Matcher matStartTest = Pattern.compile("[0-9]+\\)").matcher(problemText);
		boolean hasStartTest = matStartTest.find(0);

		// Variables for parse results
		StringBuilder className = new StringBuilder(20);
		StringBuilder methodName = new StringBuilder(20);
		StringBuilder[] paramNames = null;
		EditorDataType returnType = null;
		EditorDataType[] paramTypes = null;

		// Let's start our work
		curParsePos = getFirstNonSpaceInd(problemText);
		while (canProceed())
		{
			// For each line we'll check if it's start of some block,
			// information from which we need.
			if (canExtractAfterPrefix(sClassPrefix, className)
					|| canExtractAfterPrefix(sMethodPrefix, methodName))
			{
				// for class and method names we do nothing
				// all done in canExtractAfterPrefix
			}
			else if (canExtractAfterPrefix(sRetPrefix, typeSearch))
			{
				returnType = EditorLanguage.getType(typeSearch);
			}
			else if (isPrefix(sParamsPrefix))
			{
				paramTypes = extractParamTypes();
			}
			else if (isPrefix(sSignPrefix))
			{
				curParsePos = indexOf(problemText, '(', curParsePos);
				// some deffence from false positive or from invalid problem statement
				if (-1 < curParsePos)
				{
					++curParsePos;
					int lastPos = lastIndexOf(problemText, ')', matEndLine.start());
					if (lastPos > curParsePos)
						paramNames = extractParamNames(lastPos, paramTypes.length);
				}
			}
			else if (hasStartTest && matStartTest.start() == curParsePos)
			{
				if (null == retval)
				{
					// Let's set up what we have:
					if (null != returnType && null != paramTypes && null != paramNames)
					{
						retval = new ClassDecl(className,
						              new MethodDecl(methodName, returnType,
						                            paramTypes, paramNames));
					}
				}
				// if somethin went wrong and we couldn't parse valid class declaration
				// then we will not do anything
				if (null != retval)
					extractTestcase(lang, paramTypes.length);
			}

			// Finally shift to the next non-empty line and if necessary shift
			// pointer to next test case
			if (canShiftParsePos() && hasStartTest && matStartTest.start() < curParsePos)
				hasStartTest = matStartTest.find(curParsePos);
		}  // while (canProceed())
	}
}
