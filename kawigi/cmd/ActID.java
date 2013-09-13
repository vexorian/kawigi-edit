package kawigi.cmd;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import javax.swing.*;

/**
 *	ActID enum - The master list of KawigiEdit commands.
 *
 *	My idea is to use these to identify Action objects semi-uniquely (non-global
 *	commands may not quite be unique) in such a way that they will behave
 *	consistently on different UI or using different keystrokes, or even using
 *	automation or macros/scripts of some kind (just call a bunch of ActIDs on
 *	the global dispatcher on open, for instance, or create a list of automated
 *	tasks behind some kind of macro-driven button).  We'll see how much of my
 *	vision actually turns into anything practical.  The perfect editor plugin is
 *	useful and useable when left in the jar, with no modifications or extra
 *	work, and also flexible, powerful and maleable to the power user, without
 *	being too hard to port over old changes (I'm making an exception when it
 *	comes to porting old changes from KawigiEdit 1.* to KawigiEdit 2.0, since
 *	they really aren't the same program internally, and this enum is a big
 *	reason why).
 *
 *	If you want to add your own commands to KawigiEdit, this is the preferred
 *	place to start - add a command entry here, create your own Action class
 *	(which inherits from kawigi.cmd.DefaultAction) to control what it does.
 *	Then add it to one of the .ui files (or make your own and make sure it shows
 *	up somewhere), or if it's an editor command with just a keystroke, add it
 *	to the KawigiEditKeyMap (and make sure it's set to not be global).
 **/
public enum ActID
{
	/*

	KawigiEdit's full recognized command list (is it long enough?)
	The values are:
	-	Accelerator
	-	Mnemonic (the underlined letter you'd use to get to it on a menu or on
		a button with Alt down)
	-	Text
	-	Tooltip
	-	Icon file
	-	Action class (should have a constructor that takes an ActID as a
		parameter or has a default constructor.
	-	true for global commands, false for context-based commands (commands
		that act on any open editor window are context-based, for instance -
		that way things like undo and find can work in the main editor as well
		as the template editor.  If a command should always operate on the same
		view (like Save, Load, or Generate Code), they should be global.
	*/

	//Basic Editor Commands:
	actCut(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK), new Integer(KeyEvent.VK_T), "Cut", "Cut Selection", "Cut?.gif", EditorAction.class, false),
	actCopy(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK), new Integer(KeyEvent.VK_C), "Copy", "Copy Selection", "Copy?.gif", EditorAction.class, false),
	actPaste(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK), new Integer(KeyEvent.VK_P), "Paste", "Paste Image", "Paste?.gif", EditorAction.class, false),
	actDelete(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), new Integer(KeyEvent.VK_D), "Delete", "Delete Selection", "Delete?.gif", EditorAction.class, false),
	actSelectAll(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK), new Integer(KeyEvent.VK_A), "Select All", "Select Entire Image", null, EditorAction.class, false),
	// Makes it so that there is no text selected in an editor
	actCancelSelection(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), new Integer(KeyEvent.VK_L), "Cancel Selection", "Cancel Current Selection", null, EditorAction.class, false),
	actUndo(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK), new Integer(KeyEvent.VK_U), "Undo", "Undo Last Action", "Undo?.gif", EditorAction.class, false),
	actRedo(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK), new Integer(KeyEvent.VK_R), "Redo", "Redo Last Undone Action", "Redo?.gif", EditorAction.class, false),
	// Allows you to scroll without moving your cursor.
	actScrollUp(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_MASK), new Integer(KeyEvent.VK_U), "Scroll Up", "Scroll Up Without Moving the Cursor", null, EditorAction.class, false),
	actScrollDown(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_MASK), new Integer(KeyEvent.VK_D), "Scroll Down", "Scroll Down Without Moving the Cursor", null, EditorAction.class, false),
	// same basic idea as ctrl+right, delete and ctrl+left, delete (although I
	// don't guarantee that the semantics are the same).
	actDeleteNextWord(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.CTRL_MASK), new Integer(KeyEvent.VK_N), "Delete Next Word", "Delete Next Word", null, EditorAction.class, false),
	actDeletePreviousWord(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, InputEvent.CTRL_MASK), new Integer(KeyEvent.VK_P), "Delete Previous Word", "Delete Previous Word", null, EditorAction.class, false),
	// These are the commands that allow you to indent and "unindent" selected
	// blocks of code.
	actIndent(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), new Integer(KeyEvent.VK_I), "Indent", "Indent Block", null, EditorAction.class, false),
	actOutdent(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_MASK), new Integer(KeyEvent.VK_O), "Outdent", "Outdent Block", null, EditorAction.class, false),
	// This inserts a new line and enough spaces/tabs to make it even with the
	// previous line.  This could someday be changed to do something more like
	// "smart indent", but the problem is that would also require making normal
	// typing events to undo the "smart indenting" in some cases.
	actNewLine(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), null, "New Line", "Insert a New Line and Auto-indent.", null, EditorAction.class, false),
	// Find/Replace Commands:
	// Launches Find dialog or changes a replace dialog to a find dialog.
	actFindDlg(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK), new Integer(KeyEvent.VK_F), "Find...", "Find...", "Find?.gif", FindReplaceAction.class, false),
	// Launches Replace dialog or changes a find dialog to a replace dialog.
	actReplaceDlg(KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_MASK), new Integer(KeyEvent.VK_R), "Replace...", "Replace...", null, FindReplaceAction.class, false),
	// The find action.
	actFindNext(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), new Integer(KeyEvent.VK_F), "Find Next", "Find Next", null, FindReplaceAction.class, false),
	// Replaces the current selection if it matches what is being searched for
	// and then does FindNext
	actReplace(null, new Integer(KeyEvent.VK_R), "Replace", "Replace", null, FindReplaceAction.class, false),
	// Replaces all matches to the search string with the replacement string
	actReplaceAll(null, new Integer(KeyEvent.VK_A), "Replace All", "Replace All", null, FindReplaceAction.class, false),
	// Closes find/replace dialog
	actCloseFindReplaceDlg(null, new Integer(KeyEvent.VK_C), "Close", "Close Dialog", null, FindReplaceAction.class, false),
	// Text field(/Label) to enter the search string
	actFindField(null, new Integer(KeyEvent.VK_N), "Find:", "Type search string here", null, FindReplaceAction.class, false),
	// Text field(/Label) to enter the replacement string
	actReplaceField(null, new Integer(KeyEvent.VK_P), "Replace:", "Type replacement string here", null, FindReplaceAction.class, false),
	// Checkbox to toggle case-sensitivity.
	actCaseSensitive(null, new Integer(KeyEvent.VK_C), "Case Sensitive", "Case Sensitive", null, FindReplaceAction.class, false),
	// Checkbox to toggle the "whole word" option - if this option is on, the
	// search string is surrounded with "word boundary" markers, as defined in
	// Java's regex spec.  This includes start/end of input, whitespace, or any
	// other "non-word" characters, which is useful if you need to use search/
	// replace to change a variable's name (for instance, searching for 'i' in
	// a way that doesn't find 'if'.
	actWholeWord(null, new Integer(KeyEvent.VK_W), "Whole Word", "Whole Word or Identifier", null, FindReplaceAction.class, false),
	// Option to search for only the literal search string
	actLiteral(null, new Integer(KeyEvent.VK_L), "Literal", "Literal String", null, FindReplaceAction.class, false),
	// Allows you to search using * to mean one or more characters and ? to mean
	// any one character.
	actWildCards(null, new Integer(KeyEvent.VK_I), "Wild Cards", "Use Wild Cards", null, FindReplaceAction.class, false),
	// Full regular expression search, according to the Java regex spec.
	actRegex(null, new Integer(KeyEvent.VK_X), "Regular Expressions", "Use Regular Expressions", null, FindReplaceAction.class, false),
	// Local Testing commands:
	// This will generate the code skeleton for you to type in.  If you open a
	// problem and you don't already have anything in there from TopCoder, this
	// will automatically be run.  If you have code already and you use this
	// command, it WILL blow away your code, and that's the intended behavior.
	actGenerateCode(null, null, "Generate Code", "Generate Testing and Skeleton code", null, LocalTestAction.class, true),
	// Saves your code to the testing code directory (without compiling it)
	actSaveLocal(null, new Integer(KeyEvent.VK_A), "Save", "Save Code Locally", "Save?.gif", LocalTestAction.class, true),
	// Looks for a file with the appropriate name for the current problem and
	// language and if it exists, replaces the current source code with the
	// contents of the file.
	actLoad(null, new Integer(KeyEvent.VK_L), "Load", "Load Code from Disk", "Import?.gif", LocalTestAction.class, true),
	// Saves the code locally (inserting the testing code and all that jazz),
	// compiles it, and if the compile succeeds, runs it.
	actRunTests(null, new Integer(KeyEvent.VK_R), "Run Tests", "Compile and Run All Test Cases Locally", "Play?.gif", LocalTestAction.class, true),
	// Kills a test or compile process.
	actKillProcess(null, new Integer(KeyEvent.VK_K), "Kill", "Kill Currently Running Process", "Stop?.gif", LocalTestAction.class, true),
	// Opens some local file into the "Local Code" tab.
	actOpenLocal(null, new Integer(KeyEvent.VK_O), "Open", "Open a local source file for reference", "Open?.gif", LocalTestAction.class, true),
	// General config commands:
	// Starts the config dialog.
	actLaunchConfig(null, new Integer(KeyEvent.VK_D), "Config", "Launch Configuration Dialog", "Preferences?.gif", SettingAction.class, true),
	// Closes and commits the config dialog.
	actCommitConfig(null, new Integer(KeyEvent.VK_O), "OK", "Commit config changes", null, SettingAction.class, true),
	// Closes and doesn't commit settings on the config dialog.
	actCancelConfig(null, new Integer(KeyEvent.VK_C), "Cancel", "Cancel config changes", null, SettingAction.class, true),
	// File selector for the directory things are saved in.
	actLocalDirField(null, new Integer(KeyEvent.VK_D), "Local Directory:", "Directory on your hard drive where programs and settings are saved", null, TextSettingAction.class, true, "kawigi.localpath", "."),
	// Turn on automatic synchronization of source code with external file
	actAutoFileSync(null, new Integer(KeyEvent.VK_A), "Synchronization with external file", "Make source code load and save automatically from and to an external file.", null, BooleanSettingAction.class, true, "kawigi.file.sync", false),
	// Prefer external file sources over sources given from TopCoder server
	actPreferFileOpen(null, new Integer(KeyEvent.VK_P), "Always prefer external file to TC source", "When opening the problem always load source from file if it exists.", null, BooleanSettingAction.class, true, "kawigi.file.prefer", false),
	// Prefer external file sources over sources given from TopCoder server
	actSaveStatement(null, null, "Save problem statement to external file", "When saving source code with testing code to file save text of problem statement too", null, BooleanSettingAction.class, true, "kawigi.file.statement", false),
	// Make the log tab the default. Useful if you use an external editor.
	actLogByDefault(null, new Integer(KeyEvent.VK_L), "Select log tab at initialization", "Makes the log tab replace the editor tab as the default. Useful if you intend to use an external file editor exclusively.", null, BooleanSettingAction.class, true, "kawigi.logbydefault", false),
	// Always ignore code saved in TopCoder. Useful if you have a habit to change languages.
	actIgnoreTopCoderCode(null, new Integer(KeyEvent.VK_I), "Always ignore code saved in TopCoder", "Ignore code saved in topcoder, use in conjunction to file synchronization when you have a habit of using multiple languages so that the correct language code is always loaded.", null, BooleanSettingAction.class, true, "kawigi.ignoretopcodercode", false),
	// Timeout before test processes are automatically killed.
	actTimeout(null, new Integer(KeyEvent.VK_T), "Process Timeout:", "How long to wait before killing local processes (seconds)", null, NumberSettingAction.class, true, "kawigi.timeout", new int[]{10, 1, 100, 1}),
	// Settings on the compile and test output text boxes.
	actTestFont(null, null, "Test Font:", "Test Font", null, FontSettingAction.class, true, "kawigi.testing.font", new Font("Monospaced", 0, 12)),
	actTestBackground(null, new Integer(KeyEvent.VK_B), "Background", "Testing pane background", null, ColorSettingAction.class, true, "kawigi.testing.background", Color.white),
	actTestForeground(null, new Integer(KeyEvent.VK_F), "Foreground", "Testing pane foreground", null, ColorSettingAction.class, true, "kawigi.testing.foreground", Color.black),
	// Settings on Problem timer
	actTimerDelay(null, new Integer(KeyEvent.VK_E), "Problem Timer Delay", "Problem Timer Delay (milliseconds)", null, NumberSettingAction.class, true, "kawigi.timer.delay", new int[]{1000, 50, 60000, 10}),
	actTimerLEDColor(null, new Integer(KeyEvent.VK_L), "Timer LED Color", "Color of filled LED graphics for problem timer", null, ColorSettingAction.class, true, "kawigi.timer.foreground", Color.green),
	actTimerBGColor(null, new Integer(KeyEvent.VK_G), "Timer Background Color", "Background color for problem timer", null, ColorSettingAction.class, true, "kawigi.timer.background", Color.black),
	actTimerUnlitColor(null, new Integer(KeyEvent.VK_U), "Unlit LED Color", "Inactive LED Color for problem timer", null, ColorSettingAction.class, true, "kawigi.timer.unlit", new Color(0, 63, 0)),
	// Turns off the problem timer.
	actShowTimer(null, new Integer(KeyEvent.VK_S), "Show Problem Timer", "Show Problem Timer - Note: if you toggle this while KawigiEdit is running, you'll have to resize the window to see the effect.", null, BooleanSettingAction.class, true, "kawigi.timer.show", true),
	// Editor settings:
	actForegroundColor(null, new Integer(KeyEvent.VK_F), "Default Color", "Default font color for code", null, ColorSettingAction.class, true, "kawigi.editor.foreground", Color.white),
	actBackgroundColor(null, new Integer(KeyEvent.VK_B), "Background Color", "Default background color for code", null, ColorSettingAction.class, true, "kawigi.editor.background", Color.black),
	// Color of the highlight on selected text.
	actSelectionColor(null, new Integer(KeyEvent.VK_S), "Selection Color", "Highlight Color of selected text in code windows", null, ColorSettingAction.class, true, "kawigi.editor.SelectionColor", new Color(204, 204, 255)),
	// Color of letters selected (which aren't highlighted for syntax).
	actSelectedTextColor(null, new Integer(KeyEvent.VK_T), "Selected Text", "Color of selected text in code windows", null, ColorSettingAction.class, true, "kawigi.editor.SelectedTextColor", Color.black),
	// Syntax colors:
	actSyntaxKeywordColor(null, new Integer(KeyEvent.VK_K), "Keyword Color", "Color of keywords in code", null, ColorSettingAction.class, true, "kawigi.editor.KeywordColor", new Color(191, 191, 0)),
	// primitive types and constant values (like true, false and null).
	actSyntaxTypeColor(null, new Integer(KeyEvent.VK_Y), "Type Color", "Color of type names in code", null, ColorSettingAction.class, true, "kawigi.editor.TypeColor", new Color(127, 127, 255)),
	// operators, arithmetic or things like instanceof in Java.
	actSyntaxOperatorColor(null, new Integer(KeyEvent.VK_O), "Operator Color", "Color of operators in code", null, ColorSettingAction.class, true, "kawigi.editor.OperatorColor", new Color(191, 63, 63)),
	// String and Character literal colors
	actSyntaxStringColor(null, new Integer(KeyEvent.VK_R), "String Color", "Color of strings in code", null, ColorSettingAction.class, true, "kawigi.editor.StringColor", new Color(255, 0, 0)),
	// Color for single- and multi-line comments
	actSyntaxCommentColor(null, new Integer(KeyEvent.VK_C), "Comment Color", "Color of comments in code", null, ColorSettingAction.class, true, "kawigi.editor.CommentColor", new Color(127, 255, 127)),
	// Any line that starts with # in any language that's not Java :-)
	actSyntaxDirectiveColor(null, new Integer(KeyEvent.VK_D), "Directive Color", "Color of compiler directives in code", null, ColorSettingAction.class, true, "kawigi.editor.DirectiveColor", new Color(255, 127, 127)),
	// Color for words in a subset of standard libraries in each language.
	actSyntaxClassColor(null, new Integer(KeyEvent.VK_L), "Class Color", "Color of class names in code", null, ColorSettingAction.class, true, "kawigi.editor.ClassColor", new Color(191, 63, 191)),
	// Color for KawigiEdit template tags.
	actSyntaxTagColor(null, new Integer(KeyEvent.VK_G), "Tag Color", "Color of KawigiEdit template tags in code", null, ColorSettingAction.class, true, "kawigi.editor.TemplateTagColor", new Color(64, 192, 255)),
	// Color for highlighting pair matches (i.e. if your cursor is next to a
	// '(', the '(' and the corresponding ')' are highlighted with this color).
	actMatchingColor(null, new Integer(KeyEvent.VK_M), "Matching Color", "Color of highlights for pair-matching", null, ColorSettingAction.class, true, "kawigi.editor.matchparenscolor", new Color(64, 64, 128)),
	// Setting to turn on and off pair-match highlighting.
	actDoMatching(null, new Integer(KeyEvent.VK_P), "Match Parentheses", "Match Parentheses and other \"matching pairs\" as your cursor is near them", null, BooleanSettingAction.class, true, "kawigi.editor.matchparens", true),
	// the number of spaces between each tab stop.
	actTabWidth(null, new Integer(KeyEvent.VK_W), "Tab Width:", "Tab Width", null, NumberSettingAction.class, true, "kawigi.editor.tabstop", new int[]{4, 1, 16, 1}),
	// Font for the code editor.  I'm not sure how well the syntax highlighting
	// will render if it's not a fixed-width font.
	actCodeFont(null, null, "Font:", "Code Font", null, FontSettingAction.class, true, "kawigi.editor.font", new Font("Monospaced", Font.PLAIN, 12)),
	// Local Testing language settings:
	actJavaFileName(null, null, "File Name:", "File name for Java files - use $PROBLEM$ to substitute for the problem name", null, TextSettingAction.class, true, "kawigi.language.java.filename", "$PROBLEM$.java"),
	actJavaCompileCommand(null, null, "Compile Command:", "Compile command for compiling Java files - use $PROBLEM$ to substitute for the problem name", null, TextSettingAction.class, true, "kawigi.language.java.compiler", "javac $PROBLEM$.java"),
	actJavaRunCommand(null, null, "Run Command:", "Run command for Java programs - use $PROBLEM$ for the problem name and $CWD$ for the current directory", null, TextSettingAction.class, true, "kawigi.language.java.run", "java $PROBLEM$"),
	actCPPFileName(null, null, "File Name:", "File name for C++ files - use $PROBLEM$ to substitute for the problem name", null, TextSettingAction.class, true, "kawigi.language.cpp.filename", "$PROBLEM$.cpp"),
	actCPPCompileCommand(null, null, "Compile Command:", "Compile command for compiling C++ files - use $PROBLEM$ to substitute for the problem name", null, TextSettingAction.class, true, "kawigi.language.cpp.compiler", "g++ -std=c++0x -Wno-sign-compare $PROBLEM$.cpp"),
	actCPPRunCommand(null, null, "Run Command:", "Run command for C++ programs - use $PROBLEM$ for the problem name and $CWD$ for the current directory", null, TextSettingAction.class, true, "kawigi.language.cpp.run", File.separatorChar == '/' ? "./a.out" : "$CWD$\\a.exe"),
	actCSharpFileName(null, null, "File Name:", "File name for C# files - use $PROBLEM$ to substitute for the problem name", null, TextSettingAction.class, true, "kawigi.language.csharp.filename", "$PROBLEM$.cs"),
	actCSharpCompileCommand(null, null, "Compile Command:", "Compile command for compiling C# files - use $PROBLEM$ to substitute for the problem name", null, TextSettingAction.class, true, "kawigi.language.csharp.compiler", File.separatorChar == '/' ? "mcs $PROBLEM$.cs" : "csc $PROBLEM$.cs"),
	actCSharpRunCommand(null, null, "Run Command:", "Run command for C# programs - use $PROBLEM$ for the problem name and $CWD$ for the current directory", null, TextSettingAction.class, true, "kawigi.language.csharp.run", File.separatorChar == '/' ? "mono $PROBLEM$.exe" : "$CWD$\\$PROBLEM$.exe"),
	actVBFileName(null, null, "File Name:", "File name for VB files - use $PROBLEM$ to substitute for the problem name", null, TextSettingAction.class, true, "kawigi.language.vb.filename", "$PROBLEM$.vb"),
	actVBCompileCommand(null, null, "Compile Command:", "Compile command for compiling VB files - use $PROBLEM$ to substitute for the problem name", null, TextSettingAction.class, true, "kawigi.language.vb.compiler", "vbc $PROBLEM$.vb"),
	actVBRunCommand(null, null, "Run Command:", "Run command for VB programs - use $PROBLEM$ for the problem name and $CWD$ for the current directory", null, TextSettingAction.class, true, "kawigi.language.vb.run", File.separatorChar == '/' ? "mono $PROBLEM$.exe" : "$CWD$\\$PROBLEM$.exe"),
    actPyFileName(null, null, "File Name:", "File name for Python files - use $PROBLEM$ to substitute for the problem name", null, TextSettingAction.class, true, "kawigi.language.py.filename", "$PROBLEM$.py"),
    actPyCompileCommand(null, null, "Compile Command:", "Compile command for compiling Python files - use $PROBLEM$ to substitute for the problem name. You can leave it empty.", null, TextSettingAction.class, true, "kawigi.language.py.compiler", ""),
    actPyRunCommand(null, null, "Run Command:", "Run command for Python programs - use $PROBLEM$ for the problem name and $CWD$ for the current directory", null, TextSettingAction.class, true, "kawigi.language.py.run", "python $PROBLEM$.py"),

    // Special C++ long long substitution parameters
	actCPPLLType(null, null, "'long long' type:", "Name of type that have to be substituted instead of 'long long'.", null, TextSettingAction.class, true, "kawigi.language.cpp.lltype", "long long"),
	actCPPLLConst(null, null, "'long long' constant:", "The way in which 'long long' constants will be surrounded.", null, TextSettingAction.class, true),
	actCPPLLPrefix(null, null, "        prefix:", "Prefix that should be put before 'long long' constants.", null, TextSettingAction.class, true, "kawigi.language.cpp.llprefix", ""),
	actCPPLLPostfix(null, null, "        postfix:", "Postfix that should be put after 'long long' constants.", null, TextSettingAction.class, true, "kawigi.language.cpp.llpostfix", "ll"),
	
	// Special C++ c++11 support in generated code:
	actCPPCPP11(null, null, "Use c++11 features in tester code.", "Enables c++11 features in generated tester code. Nicer vector<> parameter code. Uncheck if you do not have c++11 (c++0x) support locally.", null, BooleanSettingAction.class, true, "kawigi.language.cpp.cpp11", true),

	// Use ahmed_aly 's code cleaner in c++ submissions:
	actAhmedAlyCleaner(null, null, "ahmed_aly's code cleaner (beta)", "Calls ahmed_aly's unused code cleaner before sending c++ code to TopCoder. Removes unused defines, typedefs, comments and empty lines.", null, BooleanSettingAction.class, true, "kawigi.language.cpp.ahmedaly", false),

	
	// Template Editor commands:
	actOpenTemplate(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK), new Integer(KeyEvent.VK_O), "Open", "Open Template", "Open?.gif", TemplateAction.class, true),
	actSaveTemplate(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK), new Integer(KeyEvent.VK_S), "Save", "Save Template", "Save?.gif", TemplateAction.class, true),
	actSaveTemplateAs(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK), new Integer(KeyEvent.VK_A), "Save As", "Save Template as a different template", "SaveAs?.gif", TemplateAction.class, true),
	actOpenDefaultTemplate(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK), new Integer(KeyEvent.VK_D), "Open Default", "Open Default Template", null, TemplateAction.class, true),
	// Template Setting commands (file panels act basically like text fields to their Action classes):
	actJavaOverride(null, null, "Template Override:", "Template to use for Java solutions.", null, TextSettingAction.class, true, "kawigi.language.java.override", ""),
	actCPPOverride(null, null, "Template Override:", "Template to use for C++ solutions.", null, TextSettingAction.class, true, "kawigi.language.cpp.override", ""),
	actCSharpOverride(null, null, "Template Override:", "Template to use for C# solutions.", null, TextSettingAction.class, true, "kawigi.language.csharp.override", ""),
	actVBOverride(null, null, "Template Override:", "Template to use for Visual Basic solutions.", null, TextSettingAction.class, true, "kawigi.language.vb.override", ""),
    actPyOverride(null, null, "Template Override:", "Template to use for Python solutions.", null, TextSettingAction.class, true, "kawigi.language.py.override", ""),
	// Snippet UI commands
	// Launches "Add Snippet" dialog.
	actAddSnippetDlg(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK), new Integer(KeyEvent.VK_T), "Add Snippet", "Add the currently selected text as a \"Snippet\" to your precoded library.", null, SnippetAction.class, false),
	// Button on the dialog to actually add the snippet.
	actAddSnippet(null, new Integer(KeyEvent.VK_T), "Add Snippet", "Add the Snippet to the tree.", null, SnippetAction.class, false),
	// Closes the "Add Snippet" dialog without saving the snippet.
	actCancelSnippet(null, new Integer(KeyEvent.VK_L), "Cancel", "Cancel adding this Snippet.", null, SnippetAction.class, false),
	// Text field for the name of the category of the snippet.
	actSnippetCategory(null, new Integer(KeyEvent.VK_C), "Category:", "Category for the Snippet to be added to (use '/' to make subcategories)", null, SnippetAction.class, false),
	// Text field for the name of a snippet.
	actSnippetName(null, new Integer(KeyEvent.VK_N), "Name:", "Name for the Snippet to be added", null, SnippetAction.class, false),
	// Note: This action isn't *really* meant to be on a menu or anything.  The
	// Snippet menu items set a global value for the current snippet code, and
	// then invoke this action through the dispatcher.  If it was put on a menu,
	// it would just insert the code that the variable was set to, probably the
	// code from the last snippet you inserted.
	actInsertSnippet(null, new Integer(KeyEvent.VK_I), "Insert Snippet", "Repeats inserting the most recently inserted snippet.", null, SnippetAction.class, false),
	// Brings up the context menu when you hit ctrl+I
	actCtxMenu(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK), new Integer(KeyEvent.VK_I), "Context Menu", "Brings up the context menu", null, EditorAction.class, false),
	// Inserts the <%:testing-code%> tag.
	actInsertTestCode(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK), new Integer(KeyEvent.VK_E), "Test Code Tag", "Inserts the KawigiEdit tag for testing code", null, EditorAction.class, false),
	// Brings up a dialog for editing test cases.
	actTestCases(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK), new Integer(KeyEvent.VK_C), "Test Cases", "Calling the test cases editor for 'Run Tests'", null, TestCasesAction.class, true),
	// Forces Test Cases dialog to update.
	actUpdateTestCases(null, null, "Update test cases dialog", "Closes the window of test cases editor, next time it is open it will parse test cases again.", null, TestCasesAction.class, true),
	// Closes the dialog for editing test cases.
	actCloseTestCases(null, null, "Close", "Closes the window of test cases editor", null, TestCasesAction.class, true),
	// Edit the single test case.
	actEditTestCase(null, null, "Edit", "Change this test case", null, TestCasesAction.class, true),
	// Delete some test case.
	actDeleteTestCase(null, null, "Delete", "Delete this test case", null, TestCasesAction.class, true),
	// Add new test case.
	actAddTestCase(null, null, "Add", "Add new test case", null, TestCasesAction.class, true),
	// Add all test cases from examples.
	actAddExTestCases(null, null, "Add from examples", "Add all test cases from examples", null, TestCasesAction.class, true),
	// Enable all test cases
	actEnableAllTestCases(null, null, "Enable all", "Enable all test cases", null, TestCasesAction.class, true),
	// Disable all test cases
	actDisableAllTestCases(null, null, "Disable all", "Disable all test cases", null, TestCasesAction.class, true),
	// Calling a dialog for editing array params of the test case.
	actEditArrayParam(null, null, "modify", "Change this array parameter", null, TestCasesAction.class, false),
	// Some action that realy is not an action. It needed for showing text in textboxes and saving changes in it
	actTestCaseParamsTexts(null, null, null, null, null, TestCasesAction.class, false),
	// Saving changed test case parameters.
	actSaveCaseParams(null, null, "OK", null, null, TestCasesAction.class, true),
	// Cancel editing test case parameters.
	actCancelCaseParams(null, null, "Cancel", null, null, TestCasesAction.class, true),
	// Saving array filling of test case parameter.
	actSaveArrayParam(null, null, "OK", null, null, TestCasesAction.class, true),
	// Cancel editing of array parameter.
	actCancelArrayParam(null, null, "Cancel", null, null, TestCasesAction.class, true),
	// Put plugin into separate window and back into Arena window.
	actOutsideMode(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK), new Integer(KeyEvent.VK_I), "Out/In", "Pop-out editor in different window or pop it back into Arena", null, PluginAction.class, true),
	// Adding this to the end so it's easier to cut and paste actions and modify
	// them at the end.  Call me lazy.
	actEnd(null, null, null, null, null, null, true);

	public KeyStroke accelerator;
	public Integer mnemonic;
	public String label;
	public String tooltip;
	public String iconFile;
	public String preference;
	public Object defaultValue;
	public Class<? extends DefaultAction> actionClass;
	private boolean global;

	/**
	 *	Creates the ActID instances.
	 **/
	ActID(KeyStroke accelerator, Integer mnemonic, String label, String tooltip, String iconFile, Class<? extends DefaultAction> actionClass, boolean global)
	{
		this.accelerator = accelerator;
		this.mnemonic = mnemonic;
		this.label = label;
		this.tooltip = tooltip;
		if (iconFile != null)
			this.iconFile = "rc/" + iconFile;
		this.actionClass = actionClass;
		this.global = global;
	}

	/**
	 *	Creates the ActID instances for settings.
	 **/
	ActID(KeyStroke accelerator, Integer mnemonic, String label, String tooltip, String iconFile, Class<? extends DefaultAction> actionClass, boolean global, String pref, Object defaultValue)
	{
		this(accelerator, mnemonic, label, tooltip, iconFile, actionClass, global);
		this.preference = pref;
		this.defaultValue = defaultValue;
	}

	/**
	 *	Use [actid].isGlobal() to figure out if it is owned by the global
	 *	dispatcher or if it could be associated with (multiple?) sub-dispatchers.
	 **/
	public boolean isGlobal()
	{
		return global;
	}
}
