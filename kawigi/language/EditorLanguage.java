package kawigi.language;

import java.util.*;
import java.util.regex.*;
import java.io.*;

import kawigi.cmd.ActID;
import kawigi.cmd.ProblemContext;
import kawigi.properties.PrefFactory;
import kawigi.properties.PrefProxy;
import kawigi.problem.*;
import kawigi.util.StringsUtil;
import kawigi.KawigiEdit;


/**
 * Class introducing methods for working with language-specific features including
 * test code generation and parsing.
 *
 * All constants controlling test code generation are put at the beginning
 * of the class. First there are constants used as variable names in test code.
 * They are followed by constants containing language-specific structures and
 * operators.
 */
public abstract class EditorLanguage
{
	/*
	  ============================================================================
		Variables for test code generation that are independent of language
		and can be easyly changed to make code look like you like. :-)
	 */
	/**
	 * Comment that will be put at the beginning of the test code. This comment
	 * is used when loading code to distinguish problem solving code from test
	 * code.
	 *
	 * @see     #sTestRegionEnd
	 */
	private static final String sTestRegionStart = "BEGIN KAWIGIEDIT TESTING";
	/**
	 * Comment that will be put at the end of the test code. This comment is used
	 * when loading code to find where test code already finished.
	 *
	 * @see     #sTestRegionStart
	 */
	private static final String sTestRegionEnd = "END KAWIGIEDIT TESTING";
	/**
     * The prefix to variables that will appear in test code as test parameters
	 * and desired answer. This will be added by parameter number.
	 *
	 * @see     #sArrayParamVarPrefix
	 */
	protected static final String sTestParamVarPrefix = "p";
    /**
     * The name of the boolean variable that will disable the test case if true..
     */
    protected static final String sTestDisabled = "disabled";
	/**
	 * Name of function in test code that will run single test case and print
	 * to the output all information about it.
	 */
	private static final String sRunTestFuncName = "KawigiEdit_RunTest";
	/**
	 * Name of the parameter to test case executor that points to sequential number
	 * of test case.
	 */
	private static final String sTestNumVarName = "testNum";
	/**
	 * Name of the parameter to test case executor that gives information about
	 * whether this case has valid answer or not.
	 */
	private static final String sHasAnswerVarName = "hasAnswer";
	/**
	 * Name of the variable in test case executor that will hold problem solver
	 * object.
	 */
	protected static final String sSolverVarName = "obj";
	/**
	 * Name of the variable in test case executor that will hold answer to test case
	 * returned by problem solver object.
	 */
	protected static final String sAnswerVarName = "answer";
	/**
	 * Name of the variable in test case executor that will hold the time of
	 * calling method in problem solver object for purposes of checking execution
	 * duration.
	 *
	 * @see     #sEndTimeVarName
	 */
	protected static final String sStartTimeVarName = "startTime";
	/**
	 * Name of the variable in test case executor that will hold the time of
	 * returning from method in problem solver object for purposes of checking
	 * execution duration.
	 *
	 * @see     #sStartTimeVarName
	 */
	protected static final String sEndTimeVarName = "endTime";
	/**
	 * Name of the variable in test case executor that will hold the result
	 * of test execution - whether it is right or wrong.
	 */
	private static final String sCallerResVarName = "res";
	/**
	 * Name of the variable in main function that will hold the result of
	 * execution of all tests - whether they all right or some tests are wrong.
	 */
	private static final String sMainResVarName = "all_right";
	/**
     * Name of the variable in main function that determines if at least one 
     * test was disabled.
     */
    private static final String sMainOneDisabledName = "tests_disabled";
    /**
	 * Maximum length of one line in generated code. In fact it can slightly vary
	 * (for example it can be 1010 characters), but this is the number which
	 * generator respects when makes test case parameters constants.
	 */
	protected static final int nMaxCodeLineLength = 1000;
	//============================================================================

	/*
	  ============================================================================
		Variables for working with tags in code templates.
	 */
	/**
	 * Prefix of the tag that will be replaced by some sensible information.
	 *
	 * @see     #sTagPostfix
	 */
	private static final String sTagPrefix = "<%:";
	/**
	 * Postfix of the tag that will be replaced by some sensible information.
	 *
	 * @see     #sTagPrefix
	 */
	private static final String sTagPostfix = "%>";
	/**
	 * Name of the tag that will be replaced with name of the class in problem.
	 */
	private static final String sClassNameTagName = "class-name";
	/**
	 * Name of the tag that will be replaced with the name of the type returning from
	 * method in problem.
	 */
	private static final String sReturnTypeTagName = "return-type";
	/**
	 * Name of the tag that will be replaced with the name of method in problem.
	 */
	private static final String sMethodNameTagName = "method-name";
	/**
	 * Name of the tag that will be replaced with the C-style list of types of
	 * parameters to method in problem.
	 */
	private static final String sParamTypesTagName = "param-type-list";
	/**
	 * Name of the tag that will be replaced with the C-style list of
	 * parameters (with its types and names) to method in problem.
	 */
	private static final String sParamsTagName = "param-list";
	/**
	 * Name of the tag that will be replaced with the VB-style list of
	 * parameters (without pointing type of passing) to method in problem.
	 */
	private static final String sVBParamsTagName = "vb-param-list";
	/**
	 * Name of the tag that will be replaced with the VB-style list of
	 * parameters to method in problem passed by reference.
	 */
	private static final String sByRefParamsTagName = "byref-param-list";
	/**
	 * Name of the tag that will be replaced with the VB-style list of
	 * parameters to method in problem passed by value.
	 */
	private static final String sByValParamsTagName = "byval-param-list";
	/**
	 * Name of the tag that will be replaced with test code when problem solver
	 * will be saved locally.
	 * 
	 * @see		#sTestingCodeTag
	 */
	private static final String sTestingCodeTagName = "testing-code";
	/**
     * Name of the tag that will be replaced with KawigiEdit version string.
     */
    private static final String sKawigiEditVersionTagName = "kawigi-edit-version";
    /**
     * Name of the tag that will be replaced with KawigiEdit credits string.
     */
    private static final String sKawigiEditCreditsTagName = "kawigi-edit-credits";

    /**
	 * Full signature of tag for testing code.
	 * 
	 * @see     #sTestingCodeTagName
	 * @see		#sTagPrefix
	 * @see		#sTagPostfix
	 */
	public static final String sTestingCodeTag = sTagPrefix + sTestingCodeTagName + sTagPostfix;
	//============================================================================

	/*
	  ============================================================================
		Variables for test code generation that depend on current language.
		So they must be set only in particular language constructor and msut not
		be changed during normal work.
	 */
	/**
	 * String that will start line comment in current language.
	 */
	protected String sLineComment = "//";
	/**
	 * String that will start print operator (NOT putting line end after printing)
	 * in current language.
	 */
	protected String sPrintPrefix = "Console.Write(";
	/**
	 * String that will end print operator (NOT putting line end after printing)
	 * in current language.
	 */
	protected String sPrintPostfix = ")";
	/**
	 * String that will start println operator (PUTTING line end after printing)
	 * in current language.
	 */
	protected String sPrintlnPrefix = "Console.WriteLine(";
	/**
	 * String that will end println operator (PUTTING line end after printing)
	 * in current language.
	 */
	protected String sPrintlnPostfix = ")";
	/**
	 * String that will put code concatenating different strings into one string
	 * in print operator in current language. Concatenation operator must be with
	 * space separators for test code beauty.
	 */
	protected String sPrintStrAdd = " + ";
	/**
	 * String that must be put at the end of each code line in current language.
	 */
	protected String sLineEnd = ";";
	/**
	 * String starting <code>if</code> operator in current language.
	 */
	protected String sIfStat = "if (";
	/**
	 * String continuing <code>if</code> operator with <code>then</code> operator
	 * in current language.
	 */
	protected String sThenStat = ") {";
	/**
	 * String continuing <code>if</code> operator with <code>else</code> block
	 * in current language.
	 */
	protected String sElseStat = "} else {";
	/**
	 * String continuing <code>if</code> operator with one more <code>if</code> block
	 * in current language.
	 */
	protected String sElseIfStat = "} else if (";
	/**
	 * String ending <code>if</code> operator in current language.
	 */
	protected String sEndIfStat = "}";
	/**
	 * String ending <code>for</code> iterating in current language.
	 */
	protected String sForEndStat = "}";
	/**
	 * Character standing on the left of array indexing in current language.
	 */
	protected char sArrayIndLeft = '[';
	/**
	 * Character standing on the right of array indexing in current language.
	 */
	protected char sArrayIndRight = ']';
	/**
	 * String used to call method in object in current language.
	 */
	protected String sObjMethodCall = ".";
	/**
	 * String that will place in code string containing tab character
	 * in current language.
	 */
	protected String sTabString = "\"\\t\"";
	/**
	 * String that will place in code string containing quote character
	 * in current language.
	 */
	protected String sQuoteString = "\"\\\"\"";
	/**
	 * String that will place in code equality comparing sign in current language
	 * along with space separators for code beautifulness.
	 */
	protected String sEqualSign = " == ";
	/**
	 * String that will place in code inequality comparing sign in current language
	 * along with space separators for code beautifulness.
	 */
	protected String sUnequalSign = " != ";
	/**
	 * String that will place in code logical or sign in current language
	 * along with space separators for code beautifulness.
     * Needs to be a lazy OR, If a is true in (a OR b), only a is evaluated.
     * Reason why VB uses OrElse instead of Or.
	 */
	protected String sLogicalOr = " || ";
	/**
	 * String that will place in code logical and sign in current language
	 * along with space separators for code beautifulness.
	 */
	protected String sLogicalAnd = " && ";
	/**
	 * String that will place in code logical not sign in current language
	 * along with space separators for code beautifulness.
	 */
	protected String sLogicalNot = "!";
	/**
	 * String that will place in code the name of function returning absolute value
	 * of double parameter in current language.
	 */
	protected String sDoubleAbsFunc = "Math.Abs";
	/**
	 * String that will place in code the name of function returning maximum value
	 * of two double parameters in current language.
	 */
	protected String sDoubleMaxFunc = "Math.Max";
	/**
	 * String that will place in code retrieving of array length in current language.
	 */
	protected String sArrayLenFunc = ".Length";
	/**
	 * String that will place in code function ending in current language.
	 */
	protected String sFuncEnd = "}";
	/**
	 * String that will place in code main procedure ending in current language.
	 */
	protected String sMainSubEnd = "}";
	/**
	 * String that will place in code operator of deletion of problem solver object
	 * in current language. If this string is empty then deletion of object is not
	 * necessary. If it's not empty it is appended by name of the object variable
	 * to form deletion operator.
	 */
	protected String sDeleteObjOp = "";
	/**
	 * String that will put code concatenating different strings into one string
	 * in test case parameters at the end of the line.
	 * Concatenation operator must be with space separator for test code beauty.
	 */
	protected String sStringsAdd = " +";
	/**
	 * Code line continuation string. Necessary mostly for VB.
	 */
	protected String sLineContinued = "";
	/**
	 * The prfix to array variables that will appear in test code before array
	 * initializers. It is necessary mostly for C++ where vector initialization
	 * is made via additional variable.
	 *
	 * @see     #sTestParamVarPrefix
	 */
	protected String sArrayParamVarPrefix = sTestParamVarPrefix;
    /**
     * Name of the true boolean literal
     */
    protected String sTrue = "true";
    /**
     * Name of the false boolean literal
     */
    protected String sFalse = "false";

	//============================================================================

	/*
	  ============================================================================
		Variables for type to string and string to type conversion
	 */
	/**
	 * Map for converting name of a type to instance of
	 * <code>EditorDataType</code>. Variable made static to be more flexible
	 * in parsing problem statement - it's made independent of which language
	 * is currently selected.
	 */
	private static Map<CharSequence, EditorDataType> types
				= new TreeMap<CharSequence, EditorDataType>(StringsUtil.getComparator());
	/**
	 * Map for converting instance of <code>EditorDataType</code> into
	 * name of a type ready to put in the code written in current language.
	 */
    private Map<EditorDataType, String> typeNames = new EnumMap<EditorDataType, String>(EditorDataType.class);
	//============================================================================

	/*
	  ============================================================================
		Some constants used in several parts of code. They are much likely not
		to change ever but who knows.
	 */
	/**
	 * Pattern for extracting numbers from test parameters.
	 */
	private static final Pattern patNumberVal = Pattern.compile("[0-9.Ee-]+");
	//============================================================================

	/*
	  ============================================================================
		Variables for local saving and testing
	 */
	/**
	 * Default file name for local saving. Value depends on current language.
	 */
	protected String sDefaultFileName = "$PROBLEM$";
	/**
	 * Default command for local compilation. Value depends on current language.
	 */
	protected String sDefaultCompileCommand = "";
	/**
	 * Default command for local execution of compiled code.
	 * Value depends on current language.
	 */
	protected String sDefaultExecuteCommand = "";
	//============================================================================

	/*
	  ============================================================================
		Variables used widely in test code generation and parsing.
	 */
	/**
	 * Buffer that will accumulate all test code.
	 */
	private StringBuilder sb = new StringBuilder(1000);
	/**
	 * Buffer containing current line indentation in test code.
	 */
	protected StringBuilder sCurIndent = new StringBuilder(5);
	/**
	 * Pattern for finding line breaks in string constants that are put only
	 * for making line length shorter.
	 *
	 * @see     #unguardLongLines(StringBuilder)
	 */
	private Pattern patUnguardLongLines;
	/**
	 * Pattern for finding test case beginning in code. Was put here to avoid
	 * recreating it.
	 *
	 * @see     #getTestBeginPat()
	 */
	private Pattern patTestBegin;
    /**
     * Pattern for finding if test case is disabled. Was put here to avoid
     * recreating it.
     *
     * @see     #getDisabledTruePat
     */
    private Pattern patDisabledTrue;
	/**
	 * Class that this generator currently working on.
	 */
    protected ClassDecl cl;
	/**
     * Is the current test case disabled or not?
     **/
    private boolean testCaseDisabled;
    /**
	 * Types of parameters to problem solver method that are used in current
	 * test code generation.
	 */
	private EditorDataType[] paramTypes;
	/**
	 * Return type of problem solver method that is used in current
	 * test code generation.
	 */
    protected EditorDataType retType;
	/**
	 * Array of all test case parameters names. Collected here so that different
	 * methods can use it without recreation of these strings.
	 */
	protected String[] paramVarNames;
	/**
	 * Name of parameter containing desired return value of test case. Was put here
	 * so that different methods can use it without recreation of this string.
	 */
	private String retValVarName;
	/**
	 * Buffer containing whole text of now parsed test code.
	 */
	private StringBuilder parsingTests;
	/**
	 * Matcher for finding beginning of test case in parsed test code.
	 *
	 * @see     #parsingTests
	 * @see     #matTestEnd
	 */
	private Matcher matTestBegin;
	/**
	 * Matcher for finding ending of test case in parsed test code.
	 *
	 * @see     #parsingTests
	 * @see     #matTestBegin
	 */
	private Matcher matTestEnd;
	/**
	 * Matchers for finding parameters of test case in parsed test code.
	 *
	 * @see     #parsingTests
	 */
	private Matcher[] matParams;
    /**
     * Matcher to detect sTestDisabled = sTrue. (If the test case is disabled)
     **/
    private Matcher matDisabledTrue;
	private StringBuilder[] parsedParams;
	/**
	 * Index of current parsing character in parsed test code.
	 *
	 * @see     #parsingTests
	 */
	private int curParseInd;
	//============================================================================

	/*
	  ============================================================================
		OK. Here finally goes the code.
		First it's some common procedures.
	 */
	/**
	 * Default constructor. Can be called only from derived classes
	 * representing concrete languages.
	 */
    protected EditorLanguage()
    {
	    String[] propTypes;
	    // All we do is fill our static table of types used for parsing
	    // of problem statement. It's a kind of hack here 'cause with appearance
	    // of new language we must to add it here. I didn't find more beautiful
	    // solving of this task.
	    // This static table of type names I believe is used now only for parsing
	    // in standalone mode. In arena mode EditorDataType::getTypeByTopCoderID()
	    // should suffice.
	    propTypes = JavaLang.getAllTypeNames();
	    fillTypes(propTypes);
	    propTypes = CPPLang.getAllTypeNames();
	    fillTypes(propTypes);
	    propTypes = CSharpLang.getAllTypeNames();
	    fillTypes(propTypes);
	    propTypes = VBLang.getAllTypeNames();
	    fillTypes(propTypes);
        propTypes = PythonLang.getAllTypeNames();
        fillTypes(propTypes);
    }

	/**
	 * Method for filling EditorDataType to type name correspondence
	 *
	 * @param propTypes     Array of type names. It should have the same number of elements
	 *                      as number of elements in EditorDataType. Each element in array
	 *                      have to represent respective (by index) data type in EditorDataType
	 */
	protected final void fillTypeNames(String[] propTypes)
	{
	    int i = 0;
	    for (EditorDataType type : EditorDataType.values())
	        typeNames.put(type, propTypes[i++]);
	}

	/**
	 * Method for filling type name to EditorDataType correspondence
	 *
	 * @param propTypes     Array of type names. Restrictions are the same as in
	 *                      <code>fillTypeNames</code>
	 *
	 * @see                 #fillTypeNames(String[])
	 */
    private static void fillTypes(String[] propTypes)
    {
        int i = 0;
        for (EditorDataType type : EditorDataType.values())
            types.put(propTypes[i++], type);
    }

	/**
	 * Gets type name in this language given representation of needed type
	 * in EditorDataType-enumeration.
	 *
	 * @param type      Type to convert to name
	 * @return          Type name in this language
	 *
	 * @see             #getType(CharSequence)
	 */
    public String getTypeName(EditorDataType type)
    {
        return typeNames.get(type);
    }

	/**
	 * Gets type representation in EditorDataType-enumeration by given type
	 * name. Type name is recognized not only in this language but in any
	 * language supported by KawigiEdit.
	 *
	 * @param name      Type name to convert
	 * @return          EditorDataType enumeration member conforming to given type name
	 *
	 * @see             #getTypeName(EditorDataType)
	 */
    public static EditorDataType getType(CharSequence name)
    {
        return types.get(name);
    }

	/**
	 * Gets category in KawigiEdit properties for saving and loading of properties
	 * related to this specific language.
	 *
	 * @return  Category for this language in properties
	 */
	public final String getPropertyCategory()
	{
	    return "kawigi.language." + toString();
	}

	/**
	 * Gets some string from properties and replaces in this string problem name and
	 * current directory name.
	 *
	 * @param prefName      Name of the property to read. This name must have leading
	 *                      dot before name
	 * @param prefDefault   Default value of the read property
	 * @param problemName   Name of problem to substitute in string
	 * @param cwd           Current directory to substitute in string
	 * @return              Resultant string ready for use
	 */
	private String getPrefFileString(String prefName, String prefDefault,
	                                 CharSequence problemName, CharSequence cwd)
	{
		StringBuilder command = new StringBuilder(PrefFactory.getPrefs().getProperty(
				                            getPropertyCategory() + prefName, prefDefault));
		int ind = command.indexOf("$PROBLEM$");
		while (0 <= ind) {
			StringsUtil.replace(command, ind, ind + 9, problemName);
			ind = command.indexOf("$PROBLEM$", ind);
		}
		ind = command.indexOf("$CWD$");
		while (0 <= ind) {
			StringsUtil.replace(command, ind, ind + 5, cwd);
			ind = command.indexOf("$CWD$", ind);
		}
		return command.toString();
	}

	/**
	 * Gets name of the file to which we will save our code. Depends on properties set.
	 *
	 * @param className     Name of the class in problem
	 * @return              Name of the file to save code
	 */
	public final String getFileName(CharSequence className)
	{
		return getPrefFileString(".filename", sDefaultFileName, className, ".");
	}

	/**
	 * Gets command string for compiling our code locally. Depends on properties set.
	 *
	 * @param className     Name of the class in problem
	 * @param cwd           Current directory to insert in command instead of <code>$CWD$</code>
	 * @return              Complete compilation command
	 */
	public final String getCompileCommand(CharSequence className, CharSequence cwd)
	{
		return getPrefFileString(".compiler", sDefaultCompileCommand, className, cwd);
	}

	/**
	 * Gets command string for running our code compiled locally.
	 * Depends on properties set.
	 *
	 * @param className     Name of the class in problem
	 * @param cwd           Current directory to insert in command instead of <code>$CWD$</code>
	 * @return              Complete command to run testing
	 */
	public final String getRunCommand(CharSequence className, CharSequence cwd)
	{
		return getPrefFileString(".run", sDefaultExecuteCommand, className, cwd);
	}
	//============================================================================

	/*
	  ============================================================================
		Methods related to tag expansion in templates and templates themselves.
	 */
	/**
	 * Creates string made of parameter types and names in C-style.
	 * If <code>paramNames</code> is <code>null</code> then don't add names -
	 * only parameters. StringBuilder in result is created in method.
	 *
	 * @param res           Place for the final string
	 * @param paramTypes    Types of parameters
	 * @param paramNames    Names of parameters
	 */
	protected void makeCStyleParams(StringBuilder res, EditorDataType[] paramTypes,
	                              StringBuilder[] paramNames)
	{
		res.setLength(0);
		// All parameters are inserted separated by comma.
		res.append(getTypeName(paramTypes[0]));
		if (null != paramNames)
			res.append(' ').append(paramNames[0]);
		for (int i = 1; paramTypes.length > i; ++i) {
			res.append(", ").append(getTypeName(paramTypes[i]));
			if (null != paramNames)
				res.append(' ').append(paramNames[i]);
		}
	}

	/**
	 * Creates string made of parameter types and names in VB-style.
	 * Value of <code>byvalFlag</code> regulates if 'ByVal' or 'ByRef' inserted
	 * or doesn't inserted any. StringBuilder in result is created in method.
	 *
	 * @param res           Place for the final string
	 * @param paramTypes    Types of parameters
	 * @param paramNames    Names of parameters
	 * @param byvalFlag     Flag pointing if nothing inserted (0), 'ByVal' inserted
	 *                      (1) or 'ByRef' inserted (2)
	 */
	private void makeVBStyleParams(StringBuilder res, EditorDataType[] paramTypes,
	                               StringBuilder[] paramNames, int byvalFlag)
	{
		res.setLength(0);
		// All parameters are inserted separated by comma.
		for (int i = 0; paramTypes.length > i; ++i) {
			if (0 < i)
				res.append(", ");
			if (1 == byvalFlag)
				res.append("ByVal ");
			else if (2 == byvalFlag)
				res.append("ByRef ");
			res.append(paramNames[i]).append(" As ").append(getTypeName(paramTypes[i]));
		}
	}

	/**
	 * Evaluates a KawigiEdit tag from a template. Value of a tag is evaluated
	 * inplace.
	 * Certain tags aren't handled here because they shouldn't be expanded yet
	 * in the code edited by the user (for instance, the testing-code tag is
	 * left alone here, because it isn't evaluated until either TopCoder asks
	 * for your code or your code gets saved locally.
	 *
	 * @param tag       Tag name to be evaluated and the place for the answer to return
	 * @param cl        Class definition for the problem
	 **/
    private void evaluateTag(StringBuilder tag, ClassDecl cl)
    {
        // We know all tag names in lowercase
        StringsUtil.toLower(tag);
        // We will need this arrays later in many calls
        EditorDataType[] paramTypes = cl.getMethod().getParamTypes();
        StringBuilder[] paramNames = cl.getMethod().getParamNames();

        // Now let's consider all tag names that we know
        if (StringsUtil.isEqual(tag, sClassNameTagName)) {
            StringsUtil.reset(tag, cl.getName());
        } else if (StringsUtil.isEqual(tag, sReturnTypeTagName)) {
            StringsUtil.reset(tag, getTypeName(cl.getMethod().getReturnType()));
        } else if (StringsUtil.isEqual(tag, sMethodNameTagName)) {
            StringsUtil.reset(tag, cl.getMethod().getName());
        } else if (StringsUtil.isEqual(tag, sParamTypesTagName)) {
            makeCStyleParams(tag, paramTypes, null);
        } else if (StringsUtil.isEqual(tag, sParamsTagName)) {
            makeCStyleParams(tag, paramTypes, paramNames);
        } else if (StringsUtil.isEqual(tag, sByRefParamsTagName)) {
            makeVBStyleParams(tag, paramTypes, paramNames, 2);
        } else if (StringsUtil.isEqual(tag, sByValParamsTagName)) {
            makeVBStyleParams(tag, paramTypes, paramNames, 1);
        } else if (StringsUtil.isEqual(tag, sVBParamsTagName)) {
            makeVBStyleParams(tag, paramTypes, paramNames, 0);
        } else if (StringsUtil.isEqual(tag, sKawigiEditVersionTagName) ) {
           StringsUtil.reset(tag, KawigiEdit.versionString  );
        } else if (StringsUtil.isEqual(tag, sKawigiEditCreditsTagName) ) {
           StringsUtil.reset(tag, KawigiEdit.editorCreditsString  );
        } else if (StringsUtil.isEqual(tag, sTestingCodeTagName)) {
            // Special case: we leave tag in the code
            tag.insert(0, sTagPrefix).append(sTagPostfix);
        } else {
            // This case shouldn't happen but we must defend ourselves from wrong compilings
            tag.setLength(0);
        }
    }

	/**
	 * Returns the (default or customized) template for the current language.
	 *
	 * @return          Template string
	 **/
	private StringBuilder getTemplate()
	{
		StringBuilder res = new StringBuilder(200);
		BufferedReader in = null;
		try {
			// First let's try template that user set
			PrefProxy prefs = PrefFactory.getPrefs();
			String override = prefs.getProperty(getPropertyCategory() + ".override");
			if (null != override) {
				try {
					File f = new File(override);
					if (f.exists())
						in = new BufferedReader(new FileReader(f));
				}
				catch (FileNotFoundException ex)
				{
					//In this case, we're just handling the error by using the resource version.
				}
			}
			if (null == in) {
				// So here we are trying to open our default template from resources
				in = new BufferedReader(new InputStreamReader(
						this.getClass().getResource("/rc/templates/" + toString() + ".ket"
													).openStream()));
			}
			String line;
			// Let's read file line by line and append it to the output
			while (null != (line = in.readLine()))
				res.append(line).append('\n');
		}
		catch (IOException ex)
		{
			// Some io-exception - we can not do anything with it
			ex.printStackTrace();
		}
		finally
		{
			// In any case we need to close our reader if it was opened
			if (null != in)
				try {
					in.close();
				}
				catch (IOException ex)
				{}
		}
		return res;
	}

	/**
	 * Generates the output of the code template with the given ClassDecl in
	 * the current language.
	 *
	 * @param cl        Class definition for this problem
	 * @return          Skeleton for the problem solver class
	 **/
	public final Skeleton getSkeleton(ClassDecl cl)
	{
		// First get current template
		StringBuilder output = getTemplate();
		// Build the tag searcher over this template
		String tagRegex = sTagPrefix + "\\s*([a-zA-Z0-9_-]+)\\s*" + sTagPostfix;
		Matcher matcher = Pattern.compile(tagRegex).matcher(output);

		StringBuilder tag = new StringBuilder(50);
		int caretIndex = 0;
		int curInd = 0;
		// Search all tags
		while (matcher.find(curInd)) {
			int start = matcher.start(), end = matcher.end();
			tag.setLength(0);
			tag.append(output, matcher.start(1), matcher.end(1));
			//Ok, one special case here: setting current caret position
			if (StringsUtil.isEqual(tag, "set-caret")) {
				caretIndex = start;
				// Tag must be deleted
				output.delete(start, end);
				curInd = start;
			}
			else {
				// Evaluate what have to be instead of a tag and replace this value
				evaluateTag(tag, cl);
				StringsUtil.replace(output, start, end, tag);
				curInd = start + tag.length();
			}
		}
		// We are done
		return new Skeleton(output, caretIndex);
	}

	/**
	 * Add to source code taken from TopCoder server testing-code tag in place where
	 * it should be normally.
	 * 
	 * @param source	Source code given by TopCoder server
	 * @return			Source code that should be shown at the code pane
	 */
	public String addAutoTestTag(String source)
	{
		// This code is the same for C# and Java
		int line_ind = source.lastIndexOf('\n');
		if (line_ind != -1 && line_ind == source.length() - 1) {
			line_ind = source.lastIndexOf('\n', line_ind - 1);
		}
		if (line_ind != -1 && line_ind < source.length() - 2
			&& source.charAt(line_ind + 1) == '/' && source.charAt(line_ind + 2) == '/')
		{
			while (line_ind != -1 && source.charAt(line_ind + 1) != '}')
				line_ind = source.lastIndexOf('\n', line_ind - 1);
			if (line_ind != -1) {
				int prev_ind = line_ind;
				line_ind = source.lastIndexOf('\n', line_ind - 1);
				if (line_ind != -1 && source.substring(line_ind + 1, prev_ind).trim().length() == 0) {
					source = source.substring(0, line_ind + 1) + EditorLanguage.sTestingCodeTag
								+ source.substring(line_ind + 1);
				}
			}
		}
		
		return source;
	}
	//============================================================================

	/*
	  ============================================================================
		Now goes code related to test code generation.
	 */
	/**
	 * Clears all data stored in class for generating new pack of test code.
	 */
	protected void clear()
	{
		sb.setLength(0);
		sCurIndent.setLength(0);
	}

	/**
	 * Adds text from <code>CharSequence</code> to the code.
	 *
	 * @param txt   Text to be added
	 * @return      All methods of <code>EditorLanguage</code> related to test code generation
	 *              return <code>this</code> to make possible convinient call chains
	 */
	protected final EditorLanguage text(CharSequence txt)
	{
		sb.append(txt);
		return this;
	}

	/**
	 * Adds the character to the code.
	 *
	 * @param cVal  Character to be added
	 * @return      All methods of <code>EditorLanguage</code> related to test code generation
	 *              return <code>this</code> to make possible convinient call chains
	 */
	protected final EditorLanguage text(char cVal)
	{
		sb.append(cVal);
		return this;
	}

	/**
	 * Adds the <code>int</code> value to the code.
	 *
	 * @param iVal  Integer value to be added
	 * @return      All methods of <code>EditorLanguage</code> related to test code generation
	 *              return <code>this</code> to make possible convinient call chains
	 */
	protected final EditorLanguage text(int iVal)
	{
		sb.append(iVal);
		return this;
	}

	/**
	 * Adds the <code>boolean</code> value to the code.
	 *
	 * @param bVal  Boolean value to be added
	 * @return      All methods of <code>EditorLanguage</code> related to test code generation
	 *              return <code>this</code> to make possible convinient call chains
	 */
	protected final EditorLanguage text(boolean bVal)
	{
		if (bVal) sb.append(sTrue);
        else sb.append(sFalse);
		return this;
	}

	/**
	 * Used in plenty of places inserting comma and space after it.
	 *
	 * @return  All methods of <code>EditorLanguage</code> related to test code generation
	 *          return <code>this</code> to make possible convinient call chains
	 */
	protected final EditorLanguage comma()
	{
		return text(", ");
	}

	/**
	 * Finishes current line and starts next. Simply finishes line
	 * without regard to special code line endings.
	 *
	 * @see #endCodeLine()
	 */
	protected final void endLine()
	{
		//text(StringsUtil.CRLF).text(sCurIndent);
		text('\n').text(sCurIndent);
	}

	/**
	 * Finishes current code line and starts next. This method writes
	 * code line ending specific for current language.
	 *
	 * @see #endLine()
	 */
	protected final void endCodeLine()
	{
		text(sLineEnd).endLine();
	}

	/**
	 * Enlarges the indent of code by one Tab character. All consequent
	 * code lines will be more indented than current level of indentation.
	 *
	 * @return  All methods of <code>EditorLanguage</code> related to test code generation
	 *          return <code>this</code> to make possible convinient call chains
	 *
	 * @see     #indentLeft()
	 */
	protected final EditorLanguage indentRight()
	{
		sCurIndent.append('\t');
		return text('\t');
	}

	/**
	 * Decreases the indent of code by one Tab character. All consequent
	 * code lines will be less indented than current level of indentation.
	 *
	 * @return  All methods of <code>EditorLanguage</code> related to test code generation
	 *          return <code>this</code> to make possible convinient call chains
	 *
	 * @see     #indentRight()
	 */
	protected final EditorLanguage indentLeft()
	{
		sCurIndent.setLength(sCurIndent.length() - 1);
		sb.setLength(sb.length() - 1);
		return this;
	}

	/**
	 * Finalizes stored code and makes it ready for inserting into the file.
	 * Method must be called at the end of code generation.
	 */
	private void finalizeCode()
	{
		sb.setLength(sb.length() - sCurIndent.length() - 1);
	}

	/**
	 * Inserts comment to code. Automatically finishes current line.
	 *
	 * @param comm  Comment string to be inserted
	 */
	protected final void comment(String comm)
	{
		text(sLineComment).text(' ').text(comm).endLine();
	}

	/**
	 * Specific function for inserting some stuff before code generation
	 * starts. By default does nothing.
	 */
	protected void preamble()
	{}

	/**
	 * Specific function for inserting some stuff after code generation
	 * ended but is not finalized yet. Made specially for C# and VB.
	 * By default does nothing.
	 */
	protected void postamble()
	{}

	/**
	 * Abstract function for inserting prefix of function header -
	 * the part that is put before function name. This function must place
	 * space character at the end.
	 *
	 * @param funcRetType   The return type of function
	 * @return              All methods of <code>EditorLanguage</code> related to test code generation
	 *                      return <code>this</code> to make possible convinient call chains
	 *
	 * @see                 #funcDefPostfix(EditorDataType)
	 */
	protected abstract EditorLanguage funcDefPrefix(EditorDataType funcRetType);

	/**
	 * Function for inserting postfix of function header -
	 * the part that is put after closing parenthesis after parameters list.
	 * By default puts open curly bracket and finishes the code line
	 * 'cause this is all that needed in most of the languages.
	 * Method must put space character at the beginning if it's needed to separate
	 * function parameters' parenthesis and further text. Also finishing of
	 * code line and indenting further text is mandatory in this method.
	 *
	 * @param funcRetType   The return type of function
	 *
	 * @see                 #funcDefPrefix(EditorDataType)
	 */
	protected void funcDefPostfix(EditorDataType funcRetType)
	{
		text(" {").endLine();
		indentRight();
	}

	/**
	 * Puts parameter of the function to its definition (not call).
	 *
	 * @param paramName     Name of function parameter
	 * @param paramType     Type of function parameter
	 * @return              All methods of <code>EditorLanguage</code> related to test code generation
	 *                      return <code>this</code> to make possible convinient call chains
	 */
	protected EditorLanguage funcDefParam(String paramName, EditorDataType paramType)
	{
		return text(getTypeName(paramType)).text(' ').text(paramName);
	}

	/**
	 * Finilizes the function definition. Automatically goes to the next line.
	 */
	private void funcEnd()
	{
		indentLeft().text(sFuncEnd).endLine();
	}

	/**
	 * Adds print operator to the code. With parameter you can manage
	 * if it will be print with or without automatic going to next line.
	 *
	 * @param isEndLine     If println (<code>true</code>) or print
	 *                      (<code>false</code>) is needed
	 * @return              All methods of <code>EditorLanguage</code> related to test code generation
	 *                      return <code>this</code> to make possible convinient call chains
	 *
	 * @see                 #print()
	 * @see                 #println()
	 */
	private EditorLanguage print(boolean isEndLine)
	{
		return text(isEndLine? sPrintlnPrefix: sPrintPrefix);
	}

	/**
	 * Adds print operator (not println) to the code.
	 *
	 * @return  All methods of <code>EditorLanguage</code> related to test code generation
	 *          return <code>this</code> to make possible convinient call chains
	 *
	 * @see     #print(boolean)
	 * @see     #println()
	 */
	private EditorLanguage print()
	{
		return print(false);
	}

	/**
	 * Adds println operator (not print) to the code.
	 *
	 * @return  All methods of <code>EditorLanguage</code> related to test code generation
	 *          return <code>this</code> to make possible convinient call chains
	 *
	 * @see     #print(boolean)
	 * @see     #print()
	 */
	private EditorLanguage println()
	{
		return print(true);
	}

	/**
	 * Adds print concatenation operator. Concatenation have to be able
	 * to concatenate different types into string.
	 *
	 * @return  All methods of <code>EditorLanguage</code> related to test code generation
	 *          return <code>this</code> to make possible convinient call chains
	 */
	private EditorLanguage printAdd()
	{
		return text(sPrintStrAdd);
	}

	/**
	 * Ends the print operator. Parameter must be the same as in call
	 * to <code>print</code> or test code can differ by made action in different
	 * languages. Method automatically goes to the next code line.
	 *
	 * @param isEndLine     If println (<code>true</code>) or print
	 *                      (<code>false</code>) is needed
	 *
	 * @see                 #print(boolean)
	 * @see                 #endPrint()
	 * @see                 #endPrintln()
	 */
	private void endPrint(boolean isEndLine)
	{
		text(isEndLine? sPrintlnPostfix: sPrintPostfix).endCodeLine();
	}

	/**
	 * Ends the print (not println) operator.
	 * Method automatically goes to the next code line.
	 *
	 * @see #endPrint(boolean)
	 * @see #endPrintln()
	 */
	private void endPrint()
	{
		endPrint(false);
	}

	/**
	 * Ends the println (not print) operator.
	 * Method automatically goes to the next code line.
	 *
	 * @see #endPrint(boolean)
	 * @see #endPrint()
	 */
	private void endPrintln()
	{
		endPrint(true);
	}

	/**
	 * Starts iteration over some array variable.
	 *
	 * @param arrayName     Name of the variable to be iterated
	 *
	 * @see                 #iterLastLine()
	 */
	protected void iterFirstLine(String arrayName)
	{
		text("for (int i = 0; ").text(arrayName).text(sArrayLenFunc)
			.text(" > i; ++i) {").endLine();
		indentRight();
	}

	/**
	 * Ends iteration over some array variable.
	 *
	 * @see #iterFirstLine(String)
	 */
	private void iterLastLine()
	{
		indentLeft().text(sForEndStat).endLine();
	}

	/**
	 * Inserts <code>if</code> operator.
	 *
	 * @return  All methods of <code>EditorLanguage</code> related to test code generation
	 *          return <code>this</code> to make possible convinient call chains
	 *
	 * @see     #then()
	 * @see     #els()
	 * @see     #elseIf()
	 * @see     #endIf()
	 */
	protected final EditorLanguage iff()
	{
		return text(sIfStat);
	}

	/**
	 * Inserts <code>then</code> operator or what is needed instead of it
	 * in this particular language. Method automatically goes to the next code line.
	 *
	 * @see #iff()
	 * @see #els()
	 * @see #elseIf()
	 * @see #endIf()
	 */
	protected final void then()
	{
		text(sThenStat).endLine();
		indentRight();
	}

	/**
	 * Inserts <code>else</code> operator. Method automatically goes to the next code line.
	 *
	 * @see #iff()
	 * @see #then()
	 * @see #elseIf()
	 * @see #endIf()
	 */
	protected final void els()
	{
		indentLeft().text(sElseStat).endLine();
		indentRight();
	}

	/**
	 * Inserts <code>else</code> and <code>if</code> operators.
	 *
	 * @return  All methods of <code>EditorLanguage</code> related to test code generation
	 *          return <code>this</code> to make possible convinient call chains
	 *
	 * @see     #iff()
	 * @see     #then()
	 * @see     #els()
	 * @see     #endIf()
	 */
	protected final EditorLanguage elseIf()
	{
		return indentLeft().text(sElseIfStat);
	}

	/**
	 * Ends the sequence of if-else operators.
	 *
	 * @see #iff()
	 * @see #then()
	 * @see #els()
	 * @see #elseIf()
	 */
	protected final void endIf()
	{
		indentLeft().text(sEndIfStat).endLine();
	}

	/**
	 * Inserts logical or operator.
	 *
	 * @return  All methods of <code>EditorLanguage</code> related to test code generation
	 *          return <code>this</code> to make possible convinient call chains
	 */
	protected final EditorLanguage or()
	{
		return text(sLogicalOr);
	}

	/**
	 * Inserts logical and operator.
	 *
	 * @return  All methods of <code>EditorLanguage</code> related to test code generation
	 *          return <code>this</code> to make possible convinient call chains
	 */
	protected final EditorLanguage and()
	{
		return text(sLogicalAnd);
	}

	/**
	 * Inserts logical not operator.
	 *
	 * @return  All methods of <code>EditorLanguage</code> related to test code generation
	 *          return <code>this</code> to make possible convinient call chains
	 */
	protected final EditorLanguage not()
	{
		return text(sLogicalNot);
	}

	/**
	 * Inserts name of function returning double absolute value of the parameter.
	 *
	 * @return  All methods of <code>EditorLanguage</code> related to test code generation
	 *          return <code>this</code> to make possible convinient call chains
	 */
	private EditorLanguage abs()
	{
		return text(sDoubleAbsFunc);
	}

	/**
	 * Inserts name of function returning maximum of two double values.
	 *
	 * @return  All methods of <code>EditorLanguage</code> related to test code generation
	 *          return <code>this</code> to make possible convinient call chains
	 */
	private EditorLanguage max()
	{
		return text(sDoubleMaxFunc);
	}

	/**
	 * Adds array index <code>i</code> with language-specific brackets.
	 *
	 * @return  All methods of <code>EditorLanguage</code> related to test code generation
	 *          return <code>this</code> to make possible convinient call chains
	 */
	protected final EditorLanguage arrayIndex()
	{
		return text(sArrayIndLeft).text('i').text(sArrayIndRight);
	}

	/**
	 * Adds array index <code>i</code> with language-specific brackets
	 * only if variable in input is <code>true</code>. Or does nothing if variable
	 * in input is <code>false</code>.
	 *
	 * @param isArray   Flag pointing if it needs do something.
	 * @return          All methods of <code>EditorLanguage</code> related to test code generation
	 *                  return <code>this</code> to make possible convinient call chains
	 */
	protected final EditorLanguage arrayIndex(boolean isArray)
	{
		if (isArray)
			arrayIndex();
		return this;
	}

	/**
	 * Declares the variable. Variable can be any of builtin types or
	 * a class of problem-solver object.
	 *
	 * @param varName   Name of the variable to be declared.
	 * @param typeName  Type of the variable to be declared.
	 */
	protected void varDeclare(CharSequence varName, CharSequence typeName)
	{
		text(typeName).text(' ').text(varName).endCodeLine();
	}

	/**
	 * Abstract method for adding code of remembering current time.
	 * Necessary for checking running time of test cases.
	 *
	 * @param varName   Name of variable to remember time in.
	 */
	protected abstract void rememberCurTime(String varName);

	/**
	 * Abstract method for adding formula counting difference between two
	 * time points stored in two different variables in seconds (double value).
	 *
	 * @param varStart  Variable name of starting moment
	 * @param varEnd    Variable name of ending moment
	 * @return          All methods of <code>EditorLanguage</code> related to test code generation
	 *                  return <code>this</code> to make possible convinient call chains
	 */
	protected abstract EditorLanguage timeDiff(String varStart, String varEnd);

	/**
	 * Adds code for calling method <code>methodName</code> in object
	 * <code>objName</code>.
	 *
	 * @param objName       Name of the object variable.
	 * @param methodName    Name of method to be called.
	 * @return              All methods of <code>EditorLanguage</code> related to test code generation
	 *                      return <code>this</code> to make possible convinient call chains
	 */
    protected EditorLanguage methodCall(CharSequence objName, CharSequence methodName)
	{
		return text(objName).text(sObjMethodCall).text(methodName);
	}

	/**
	 * Adds code for deleting problem-solver object. Created specifically for C++.
	 *
	 * @param varName   Name of object variable to be deleted.
	 */
	private void deleteObj(String varName)
	{
		if (0 < sDeleteObjOp.length())
			text(sDeleteObjOp).text(varName).endCodeLine();
	}

	/**
	 * Inserts string, containing tab-character.
	 *
	 * @return  All methods of <code>EditorLanguage</code> related to test code generation
	 *          return <code>this</code> to make possible convinient call chains
	 */
	private EditorLanguage tabString()
	{
		return text(sTabString);
	}

	/**
	 * Inserts string, containing quote-character.
	 *
	 * @return  All methods of <code>EditorLanguage</code> related to test code generation
	 *          return <code>this</code> to make possible convinient call chains
	 */
	private EditorLanguage quoteString()
	{
		return text(sQuoteString);
	}

	/**
	 * Inserts printing of quote character if the printing type is string or
	 * string array. Also inserts print string concatenation operation before or
	 * after printed quote.
	 *
	 * @param type          Type of date to be printed
	 * @param isAddBefore   Flag pointing if concatenation operation must be added
	 *                      before or after quote string
	 * @return              All methods of <code>EditorLanguage</code> related to test code generation
	 *                      return <code>this</code> to make possible convinient call chains
	 *
	 * @see                 #printAddQuote(EditorDataType)
	 * @see                 #printQuoteAdd(EditorDataType)
	 */
	private EditorLanguage printAddQuote(EditorDataType type, boolean isAddBefore)
	{
		if (type.isString()) {
			if (isAddBefore)
				printAdd().quoteString();
			else
				quoteString().printAdd();
		}
		return this;
	}

	/**
	 * Inserts printing of quote character if the printing type is string or
	 * string array. Also inserts print string concatenation operation before
	 * printed quote.
	 *
	 * @param type      Type of date to be printed
	 * @return          All methods of <code>EditorLanguage</code> related to test code generation
	 *                  return <code>this</code> to make possible convinient call chains
	 *
	 * @see             #printAddQuote(EditorDataType, boolean)
	 * @see             #printQuoteAdd(EditorDataType)
	 */
	private EditorLanguage printAddQuote(EditorDataType type)
	{
		return printAddQuote(type, true);
	}

	/**
	 * Inserts printing of quote character if the printing type is string or
	 * string array. Also inserts print string concatenation operation after
	 * printed quote.
	 *
	 * @param type      Type of date to be printed
	 * @return          All methods of <code>EditorLanguage</code> related to test code generation
	 *                  return <code>this</code> to make possible convinient call chains
	 *
	 * @see             #printAddQuote(EditorDataType, boolean)
	 * @see             #printAddQuote(EditorDataType)
	 */
	private EditorLanguage printQuoteAdd(EditorDataType type)
	{
		return printAddQuote(type, false);
	}

	/**
	 * Adds code to print value of the variable.
	 *
	 * @param varName   Variable name to be printed
	 * @param type      Type of the printed variable
	 * @param isAlone   Flag pointing if this variable will be printed inside of chain
	 *                  of other printing (<code>false</code>) or must be printed
	 *                  alone on a separate line (<code>true</code>)
	 * @return          All methods of <code>EditorLanguage</code> related to test code generation
	 *                  return <code>this</code> to make possible convinient call chains
	 */
	private EditorLanguage printVarValue(String varName, EditorDataType type, boolean isAlone)
	{
		// Major separation: arrays and simple types are printed differently
		if (type.isArrayType()) {
			// If printing alone we have to add print operator
			if (isAlone)
				print().tabString();
			// Then we print array open bracket
			printAdd().text("\"{\"").endPrint();
			// And iterate throw all array elements
			iterFirstLine(varName);
				// For each element after first we put comma to the output
				iff().text("i > 0").then();
					print().text("\",\"").endPrint();
				endIf();
				// Then printing value
				print().printQuoteAdd(type).text(varName).arrayIndex()
						.printAddQuote(type).endPrint();
			iterLastLine();
			// Then closing curly bracket and if printing alone
			// then go to the next output line
			print(isAlone).text("\"}\"");
		}
		else {
			// If printing alone we have to add print operator
			if (isAlone)
				println().tabString();
			// Here all is much simpler: print value and that's all
			printAdd().printQuoteAdd(type).text(varName).printAddQuote(type);
		}
		// Finishing of print operator is necessary only if printing alone.
		// We will save test code lines drammatically.
		if (isAlone)
			endPrintln();
		return this;
	}

	/**
	 * Adds code of (in)equality checking on two variables
	 * that can be array. In latter case the array elements (in)equality
	 * checking takes place. Method respects TopCoder double values tolerance.
	 * In case of double comparison relative error is calculated according to
	 * right variable.
	 *
	 * @param varLeft       Name of first variable to compare
	 * @param varRight      Name of second variable to compare
	 * @param type          Type of variables to compare
	 * @param isNotEqual    flag pointing if equal (<code>false</code>) or not-equal
	 *                      (<code>true</code>) sign must be used
	 * @return              All methods of <code>EditorLanguage</code> related to test code generation
	 *                      return <code>this</code> to make possible convinient call chains
	 *
	 * @see                 #equal(String, String, EditorDataType)
	 * @see                 #unequal(String, String, EditorDataType)
	 */
	protected EditorLanguage equal(String varLeft, String varRight,
	                              EditorDataType type, boolean isNotEqual)
	{
		boolean isArray = type.isArrayType();
		if (type.isType(EditorDataType.Double)) {
			// For doubles comparing is as follows: get absolute value
			// of right variable or 1.0 (whichever is greater),
			// multiply it on 1e-9 and compare to absolute value of difference
			// between variables. Also make a simple check for NaN as x == x
			// because NaN != NaN by definition.
			text(varLeft).arrayIndex(isArray)
						.text(isNotEqual? sUnequalSign: sEqualSign)
						.text(varLeft).arrayIndex(isArray);
			if (isNotEqual)
				or();
			else
				and();
				abs().text("(").text(varRight).arrayIndex(isArray)
							.text(" - ").text(varLeft).arrayIndex(isArray)
					.text(") ").text(isNotEqual? ">": "<=")
					.text(" 1e-9 * ").max().text("(1.0, ")
					.abs().text('(').text(varRight).arrayIndex(isArray).text("))");
		}
		else {
			// For non-doubles comparing is much simpler
			text(varLeft).arrayIndex(isArray)
							.text(isNotEqual? sUnequalSign: sEqualSign)
							.text(varRight).arrayIndex(isArray);
		}
		return this;
	}

	/**
	 * Adds code of equality checking on two simple variables.
	 * Method respects TopCoder double values tolerance.
	 *
	 * @param varLeft       Name of first variable to compare
	 * @param varRight      Name of second variable to compare
	 * @param type          Type of variables to compare
	 * @return              All methods of <code>EditorLanguage</code> related to test code generation
	 *                      return <code>this</code> to make possible convinient call chains
	 *
	 * @see                 #equal(String, String, EditorDataType, boolean)
	 * @see                 #unequal(String, String, EditorDataType)
	 */
	private EditorLanguage equal(String varLeft, String varRight, EditorDataType type)
	{
		return equal(varLeft, varRight, type, false);
	}

	/**
	 * Adds code of inequality checking on two simple variables.
	 * Method respects TopCoder double values tolerance.
	 *
	 * @param varLeft       Name of first variable to compare
	 * @param varRight      Name of second variable to compare
	 * @param type          Type of variables to compare
	 * @return              All methods of <code>EditorLanguage</code> related to test code generation
	 *                      return <code>this</code> to make possible convinient call chains
	 *
	 * @see                 #equal(String, String, EditorDataType, boolean)
	 * @see                 #equal(String, String, EditorDataType)
	 */
	private EditorLanguage unequal(String varLeft, String varRight, EditorDataType type)
	{
		return equal(varLeft, varRight, type, true);
	}

	/**
	 * Adds code for checking of inequality of two array lengths.
	 *
	 * @param varLeft   Name of first array variable
	 * @param varRight  Name of second array variable
	 * @return          All methods of <code>EditorLanguage</code> related to test code generation
	 *                  return <code>this</code> to make possible convinient call chains
	 */
	protected EditorLanguage arrayLensUnequal(String varLeft, String varRight)
	{
		text(varLeft).text(sArrayLenFunc)
				.text(sUnequalSign).text(varRight).text(sArrayLenFunc);
		return this;
	}

	/**
	 * Adds code for checking of equality of two variables. If variables are arrays
	 * then they are checked element by element. Method respects TopCoder double
	 * values tolerance. In generated code the assumption made that result variable
	 * already initialized with value <code>true</code>.
	 *
	 * @param varLeft       Name of the first variable to compare
	 * @param varRight      Name of the second variable to compare
	 * @param type          Type of the compared variables
	 * @param resVarName    Name of the variable which will receive <code>false</code>
	 *                      result in case of variables' inequality
	 * @return              All methods of <code>EditorLanguage</code> related to test code generation
	 *                      return <code>this</code> to make possible convinient call chains
	 */
	private EditorLanguage varsEqual(String varLeft, String varRight,
	                                EditorDataType type, String resVarName)
	{
		if (type.isArrayType()) {
			// For arrays we first check array lengths
			iff().arrayLensUnequal(varLeft, varRight).then();
				text(resVarName).text(" = ").text(false).endCodeLine();
			els();
				// Then we iterate on all array elements and check them
				iterFirstLine(varLeft);
					iff().unequal(varLeft, varRight, type).then();
						text(resVarName).text(" = ").text(false).endCodeLine();
					endIf();
				iterLastLine();
			endIf();
		}
		else {
			// For non-arrays we simply assign into result variable the outcome
			// of comparing of these variables
			text(resVarName).text(" = ").equal(varLeft, varRight, type).endCodeLine();
		}
		return this;
	}

	/**
	 * Adds code for printing all test input information at the beginning of
	 * test calling function.
	 */
	private void printTestInputInfo()
	{
		// Print the test number
		print().text("\"Test \"").printAdd().text(sTestNumVarName)
								 .printAdd().text("\": [\"");
		// Print all parameters coma-separated
		for (int i = 0; paramTypes.length > i; ++i) {
			printVarValue(paramVarNames[i], paramTypes[i], false);
			if (paramTypes.length > i + 1)
				printAdd().text("\",\"");
		}
		endPrint();
		// Finally going to the next line in output
		println().text("\"]\"").endPrintln();
	}

	/**
	 * Adds all test parameters coma-separated. Used for passing these parameters
	 * to problem-solver or to function calling test.
	 *
	 * @return  All methods of <code>EditorLanguage</code> related to test code generation
	 *          return <code>this</code> to make possible convinient call chains
	 */
    protected EditorLanguage passTestParams()
	{
		text(paramVarNames[0]);
		for (int i = 1; paramTypes.length > i; ++i) {
			comma().text(paramVarNames[i]);
		}
		return this;
	}

	/**
	 * Adds code for test call of problem solver with remembering of it's working
	 * time.
	 */
	protected void callProblemSolver()
	{
		// First declare variables for problem solver and it's answer
		varDeclare(sSolverVarName, cl.getName());
		varDeclare(sAnswerVarName, getTypeName(retType));
		// Creating object
		text(sSolverVarName).text(" = new ").text(cl.getName()).text("()").endCodeLine();
		// Calling main solving method
		rememberCurTime(sStartTimeVarName);
		text(sAnswerVarName).text(" = ")
				.methodCall(sSolverVarName, cl.getMethod().getName())
				.text('(').passTestParams().text(')').endCodeLine();
		rememberCurTime(sEndTimeVarName);
		// Deleting object if language need to
		deleteObj(sSolverVarName);
	}

	/**
	 * Adds code printing all information about test output at the end of test
	 * calling function.
	 */
	private void printTestOutputInfo()
	{
		// First print the time that test took to execute
		println().text("\"Time: \"").printAdd()
				.timeDiff(sStartTimeVarName, sEndTimeVarName)
				.printAdd().text("\" seconds\"").endPrintln();
		// Then write the desired answer of this test
		iff().text(sHasAnswerVarName).then();
			println().text("\"Desired answer:\"").endPrintln();
			printVarValue(retValVarName, retType, true);
		endIf();
		// Write answer of problem solver
		println().text("\"Your answer:\"").endPrintln();
		printVarValue(sAnswerVarName, retType, true);

		// Now check that returned answer is right
		iff().text(sHasAnswerVarName).then();
			varsEqual(sAnswerVarName, retValVarName, retType, sCallerResVarName);
		endIf();
		// And print our conclusion about it
		iff().not().text(sCallerResVarName).then();
			println().text("\"DOESN'T MATCH!!!!\"").endPrintln();
		elseIf().timeDiff(sStartTimeVarName, sEndTimeVarName).text(" >= 2").then();
			println().text("\"FAIL the timeout\"").endPrintln();
			text(sCallerResVarName).text(" = ").text(false).endCodeLine();
		elseIf().text(sHasAnswerVarName).then();
			println().text("\"Match :-)\"").endPrintln();
		els();
			println().text("\"OK, but is it right?\"").endPrintln();
		endIf();

		// Print empty line to the output for beautiness
		println().text("\"\"").endPrintln();
	}

	/**
	 * Adds full code for function that calls individual test case and prints
	 * to output all information about it.
	 */
	private void makeTestCaller()
	{
		// Long function header
		funcDefPrefix(EditorDataType.Boolean).text(sRunTestFuncName).text('(')
				// Parameter for test case number
				.funcDefParam(sTestNumVarName, EditorDataType.Integer).comma();
				// All test case parameters
				for (int i = 0; paramTypes.length > i; ++i) {
					funcDefParam(paramVarNames[i], paramTypes[i]).comma();
				}
				// Variable pointing if this test case has correct answer
				funcDefParam(sHasAnswerVarName, EditorDataType.Boolean).comma()
				// And answer to test case itself
				.funcDefParam(retValVarName, retType).text(')');
		// Finishing function header
		funcDefPostfix(EditorDataType.Boolean);
			// Separate parts of function thrown away to other methods
			printTestInputInfo();
			callProblemSolver();
			// Variable with function result must be declared here or earlier
			// because it's used in printTestOutputInfo
			varDeclare(sCallerResVarName, getTypeName(EditorDataType.Boolean));
			text(sCallerResVarName).text(" = ").text(sTrue).endCodeLine();
			printTestOutputInfo();
			// That's all, we can return
			text("return ").text(sCallerResVarName).endCodeLine();
		funcEnd();
	}

	/**
	 * Adds header of main procedure. All signatures of main procedure in
	 * different languages are not alike, so this method does here only
	 * identical stuff that need to be done at the end in each language.
	 */
	protected void mainSubDef()
	{
		endLine();
		indentRight();
	}

	/**
	 * Adds code finalizing main procedure.
	 */
	protected void endMainSub()
	{
		indentLeft().text(sMainSubEnd).endLine();
	}

	/**
	 * Adds mark of the beginning of test. This mark will be used at code loading
	 * to find the place where test data begins.
	 *
	 * @param num   Number of the test
	 *
	 * @see         #getTestBeginPat()
	 */
	protected void markTestBegin(int num)
	{
		String comm = "----- test " + num + " -----";
		comment(comm);
	}

	/**
	 * Adds mark of ending of the code related to one test case. This mark is made
	 * basically on beauty of generated code purposes.
	 */
	protected void markTestEnd()
	{
        //Let us explicitly call getTestEndExpr to make sure it is right:
        text(getTestEndExpr() ).endLine();
	}

    /**
     * Expression in code that will be treated as the end of test case.
     * If this expression will be changed then perhaps code for generation of
     * main function will need to be changed too.
     *
     * Needs to be a method because sLineComment can/should be overriden. 
     *
     * @see     #callTest(int, Test)
     * @see     #extractTestCases(StringBuilder, ClassDecl, StringBuilder)
     */
    protected String getTestEndExpr()
    {
        return sLineComment + " ------------------";
    } 

	/**
	 * Gets prefix for given data type in this language. Retrieved via method
	 * and not constant 'cause it can change in runtime via properties' changing
	 * (for C++ implemented only).
	 *
	 * @param type      Data type to check for prefix.
	 * @return          Prefix string for this type and language.
	 *
	 * @see             #getNumTypePostfix(EditorDataType)
	 */
	protected String getNumTypePrefix(EditorDataType type)
	{
		return "";
	}

	/**
	 * Gets postfix for given data type in this language. Retrieved via method
	 * and not constant 'cause it can change in runtime via properties' changing
	 * (for C++ implemented only).
	 *
	 * @param type      Data type to check for postfix.
	 * @return          Postfix string for this type and language.
	 *
	 * @see             #getNumTypePrefix(EditorDataType)
	 */
	protected String getNumTypePostfix(EditorDataType type)
	{
		return "";
	}

	/**
	 * Escapes all special characters in string constants or string-array constants.
	 * All changes are made inplace. And all changes are made assuming that
	 * elements in string array enclosed in quotes with escaped inner quotes
	 * and backslashes.<br/>
	 * Method now works around only quotes and backslashes because
	 * I (Pavel Ivanov aka pivanof) didn't see in TopCoder problems any other
	 * escape-sequences used.
	 *
	 * @param val   In input it's constant to be escaped, in output it's
	 *              escaped constant ready for inserting in code.
	 * @param type  Data type represented by given constant.
	 *
	 * @see         #unescapeSequences(StringBuilder, EditorDataType)
	 */
	protected void escapeSequences(StringBuilder val, EditorDataType type)
	{
		assert type.isString();
		boolean isInQuote = false;
		for (int i = 0; val.length() > i; ++i) {
			char c = val.charAt(i);
			if ('\\' == c) {
				// anyway we will skip next character
				++i;
				if (val.length() > i) {
					c = val.charAt(i);
					// if not in array element or next char is not escaped one
					// then we need to escape this backslash
					if (!isInQuote || '"' != c && '\\' != c)
						val.insert(i, '\\');
				}
			}  // if ('\\' == c)
			else if ('"' == c) {
				if (type.isArrayType()) {
					isInQuote = !isInQuote;
				}
				else {
					// if it's not array, then all quotes won't be escaped
					// and we have to escape them here
					val.insert(i, '\\');
					++i;
				}
			}  // if ('"' == c), if ('\\' == c) else
		}  // for (int i = 0; val.length() > i; ++i)
	}

	/**
	 * Makes line wrapping in test case parameters if they are too long.
	 * All changes are made inplace. Method works only for string and string array
	 * values. All strings in array and single string have to be enclosed in quotes.
	 *
	 * @param val   String parameter value that need to be wrapped
	 *
	 * @see         #nMaxCodeLineLength
	 */
	private void guardLongLines(StringBuilder val)
	{
		if (nMaxCodeLineLength < val.length()) {
			StringBuilder sbTemp = new StringBuilder(20);
			boolean isInQuote = false;
			int curLineStart = 0;
			for (int i = 0; val.length() > i; ++i) {
				char c = val.charAt(i);
				// I use a kind of hack here: I know that double quotes won't ever be here
				// except the case of VB quoting and not in VB backslashes won't
				// ever be here except the case of backslash or quote escaping.
				// So in these cases of escaping and quoting I simply pass over
				// escaped symbol.
				if ('\\' == c && val.length() > i + 1) {
					c = val.charAt(i + 1);
					if ('\\' == c || '"' == c)
						++i;
				}
				else if ('"' == c) {
					if (val.length() > i + 1 && '"' == val.charAt(i + 1))
						++i;
					else
						isInQuote = !isInQuote;
				}
				if (nMaxCodeLineLength <= i - curLineStart) {
					// If we are already made our line, we need to go to the next line.
					// We will collect all that we need to insert and then make single
					// insertion
					sbTemp.setLength(0);

					// If we are not in quotes then string will not be torn and we need
					// not insert additional quotes and string concatenation
					if (isInQuote)
						sbTemp.append('"').append(sStringsAdd);
					sbTemp.append(sLineContinued).append(StringsUtil.CRLF).append(sCurIndent);
					// Make some additional indentation for beautiness
					sbTemp.append("\t\t");
					// Again we need additional quote only if we are at the middle
					// of the string
					if (isInQuote)
						sbTemp.append('"');

					// Now insert this to our value and shift current position
					val.insert(i + 1, sbTemp);
					i += sbTemp.length();
					// And remember position of line beginning
					curLineStart = i + 1;
				}  // if (nMaxCodeLineLength <= i - curLineStart)
			}  // for (int i = 0; val.length() > i; ++i)
		}  // if (nMaxCodeLineLength < val.length())
	}

	/**
	 * Makes some value suitable for inserting in the code. Makes different
	 * transformations depending on type of the value. All transformations
	 * are making inplace. All action is strictly opposite to
	 * <code>extractValFromCode</code>.
	 *
	 * @param val   On entering it's value taken from TopCoder problem statement
	 *              or from user input. On returning it's the same value ready
	 *              for inserting in the code.
	 * @param type  Data type of inspected value.
	 *
     * @see         #extractValFromCode(StringBuilder, EditorDataType)
	 **/
	private void prepareValForCode(StringBuilder val, EditorDataType type)
	{
		boolean isString = type.isString();
		boolean isArray = type.isArrayType();
		if (isString) {
			// for strings and string arrays we only need to do escapings
			escapeSequences(val, type);
			if (!isArray) {
				// for strings (not string arrays) we additionally put quotes around them
				StringsUtil.addStringMarks(val);
			}
			// Make long lines wrapping
			guardLongLines(val);
		}
		else {
			// all other types are numbers, so we need to put prefixes
			// and postfixes around all numbers
			// (in case it is array number will be not one)
		    String prefix = getNumTypePrefix(type);
		    String suffix = getNumTypePostfix(type);
		    if (0 < suffix.length() || 0 < prefix.length()) {
			    Matcher mat = patNumberVal.matcher(val);
			    int nextPos = 0;
			    // we do all inplace without creating new classes
			    while (mat.find(nextPos)) {
				    val.insert(mat.end(), suffix);
				    val.insert(mat.start(), prefix);
				    nextPos = mat.end() + suffix.length() + prefix.length();
			    }
		    }  // if (0 < suffix.length() || 0 < prefix.length())
		}  // if (type.isString()) else

		if (isArray) {
			// for arrays we additionally put curly brackets around all value
			// fortunately these symbols are the same for all languages
			StringsUtil.addArrayMarks(val);
		}
	}

	/**
	 * Adds code of array test case parameter initializing. Method have to end
	 * the code line.
	 *
	 * @param num       Sequential number of the parameter
	 * @param type      Type of the parameter
	 * @param value     Value of the parameter ready for inserting into the code
	 *
	 * @see             #initTestParam(int, EditorDataType, StringBuilder)
	 */
	protected void initArrayParam(int num, EditorDataType type, StringBuilder value)
	{
		text(paramVarNames[num]).text(" = new ").text(getTypeName(type))
			.text(value).endCodeLine();
	}

	/**
	 * Adds code of test case parameter initializing.
	 *
	 * @param num       Sequential number of the parameter
	 * @param type      Type of the parameter
	 * @param value     Value of the parameter in internal and user-friendly
	 *                  representation that needs to be converted by
	 *                  <code>EditorLanguage.prepareValForCode</code>
	 *
	 * @see             #prepareValForCode(StringBuilder, EditorDataType)
	 */
	private void initTestParam(int num, EditorDataType type, StringBuilder value)
	{
		// First we convert the value for code
		StringBuilder sbTemp = new StringBuilder(value);
		prepareValForCode(sbTemp, type);
		// Add the parameter initialization
		if (type.isArrayType())
			initArrayParam(num, type, sbTemp);
		else
			text(paramVarNames[num]).text(" = ").text(sbTemp).endCodeLine();
	}

	/**
	 * Adds the full bunch of code that calls one particular test case.
	 *
	 * @param testNum   Number of the test
	 * @param t         Test object itself
	 */
	private void callTest(int testNum, Test t)
	{
		// Let's start test
		markTestBegin(testNum);
		// Initializing parameters
		StringBuilder[] params = t.getParameters();
        text(sTestDisabled).text(" = ").text(t.isDisabled() ).endCodeLine();
		for (int i = 0; paramTypes.length > i; ++i) {
			initTestParam(i, paramTypes[i], params[i]);
		}
		// Initializing the answer if needed
		if (t.isWithAnswer())
			initTestParam(paramTypes.length, retType, t.getAnswer());
		// Now call the function running test and remembering the worst result
		// of all these callings
		// Function call must be the same as in parsing so we use common variable
        text(sMainResVarName).text(" = (").text(sTestDisabled).or()
                .text(sRunTestFuncName).text('(').text(testNum).comma()
				.passTestParams().comma().text(t.isWithAnswer())
				.comma().text(paramVarNames[paramTypes.length])
                .text(") )").and().text(sMainResVarName).endCodeLine();
        text(sMainOneDisabledName).text(" = ")
            .text(sMainOneDisabledName).or().text(sTestDisabled).endCodeLine();
        // Test is over. Mark the end of the test case, also useful for parsing:
		markTestEnd();
		endLine();
	}

	/**
	 * Adds main running function of the test program.
	 */
	private void mainSub()
	{
		mainSubDef();
			// Declaring variable that will collect the worst test result
			// (it means is all tests correct or some test failed)
			varDeclare(sMainResVarName, getTypeName(EditorDataType.Boolean));
            varDeclare(sTestDisabled, getTypeName(EditorDataType.Boolean));
            varDeclare(sMainOneDisabledName, getTypeName(EditorDataType.Boolean));
			text(sMainResVarName).text(" = ").text(sTrue).endCodeLine();
			text(sMainOneDisabledName).text(" = ").text(sFalse).endCodeLine();
			endLine();
			// Now declare all variables that will take all parameters of the test
			for (int i = 0; paramTypes.length > i; ++i)
				varDeclare(paramVarNames[i], getTypeName(paramTypes[i]));
			// And answer too
			varDeclare(paramVarNames[paramTypes.length], getTypeName(retType));
			endLine();
			// Calling all test cases
			for (int i = 0; cl.countTests() > i; ++i)
				callTest(i, cl.getTest(i));
			// Check if all is OK.
            iff().text(sMainResVarName).then();
                iff().text(sMainOneDisabledName).then();
                    println().text("\"You're a stud (but some test cases were disabled)!\"").endPrintln();
                els();
                    println().text("\"You're a stud (at least on given cases)!\"").endPrintln();
                endIf();
			els();
				println().text("\"Some of the test cases had errors.\"").endPrintln();
			endIf();
			// We are done
		endMainSub();
	}

	/**
	 * Adds current problem statement text as a comment 
	 */
	private final void problemStmtComment()
	{
		boolean needStmt = PrefFactory.getPrefs().getBoolean(ActID.actSaveStatement.preference, false);
		if (!needStmt) {
			return;
		}

		String stmt = ProblemContext.getStatement();
		if (stmt.length() == 0) {
			return;
		}
		
		stmt = stmt.replaceAll(StringsUtil.sCRLFregex, "\n" + sCurIndent + sLineComment + " ");
		text(sLineComment).text(' ').text(stmt).endLine();
	}
	
	/**
	 * Makes full generation of test code and returns it as <code>String</code>.
	 *
	 * @param classProblem  Problem class information for test code
	 * @return              Generated test code
	 */
	public final String getTestCode(ClassDecl classProblem)
	{
		// First initialize information about our problem solver class.
		// This information will be used further in different methods.
		cl = classProblem;
		paramTypes = cl.getMethod().getParamTypes();
		retType = cl.getMethod().getReturnType();

		// Precalculate test parameters names for the same purpose
		paramVarNames = new String[paramTypes.length + 1];
		for (int i = 0; paramTypes.length >= i; ++i) {
            paramVarNames[i] = sTestParamVarPrefix + i;
		}
		retValVarName = paramVarNames[paramTypes.length];

		// Full code generation is divided in several procedures
		// for readability and convinient understanding
		clear();
		// This comment is used for cutting testing code on loading from file
		comment(sTestRegionStart);
		comment("Generated by " + KawigiEdit.versionString);
		preamble();
		makeTestCaller();
		mainSub();
		postamble();
		problemStmtComment();
		// This comment is used for cutting testing code on loading too
		comment(sTestRegionEnd);
		finalizeCode();

		return sb.toString();
	}
	//============================================================================

	/*
	  ============================================================================
		Now goes code related to test code parsing and test cases extraction.
	 */
	/**
	 * Gets pattern for extracting number constant from code. Uses results of
	 * <code>getNumTypePrefix</code> and <code>getNumTypePostfix</code>.
	 * If there isn't any prefixes or suffixes for this type then method returns
	 * <code>null</code>.
	 *
	 * @param type      Data type of the constant
	 * @return          Pattern ready for extracting number values from this constant
	 *
	 * @see             #getNumTypePrefix(EditorDataType)
	 * @see             #getNumTypePostfix(EditorDataType)
	 * @see             #patNumberVal
	 */
	private Pattern getNumExtractPattern(EditorDataType type)
	{
		String pref = getNumTypePrefix(type);
		String suf = getNumTypePostfix(type);
		Pattern res = null;
		if (0 < pref.length() || 0 < suf.length()) {
			/* This parentheses-replacing stuff is made because for instance
			   I (Pavel Ivanov aka pivanof) using for local compilation VS.NET
			   and it has different from gcc long long type and suffix for
			   long long constants. So I use macro for long long constants
			   which has parentheses in prefix and postfix and these parentheses
			   interfere with regexp syntax.
			 */
			pref = pref.replaceAll("([()])", "\\\\$1");
			suf = suf.replaceAll("([()])", "\\\\$1");
			res = Pattern.compile(pref + '(' + patNumberVal.pattern() + ')' + suf);
		}  // if (0 < pref.length() && 0 < suf.length())
		return res;
	}

	/**
	 * Removes all escapings in string and string-array constants
	 * made by <code>escapeSequences</code>. Method assumes that in string array
	 * in output we need all elements to be enclosed in quotes and
	 * all inner quotes and backslashes to be escaped.
	 * All changes are made inplace.<br/>
	 * Method now works around only quotes and backslashes because
	 * I (Pavel Ivanov aka pivanof) didn't see in TopCoder problems any other
	 * escape-sequences used.
	 *
	 * @param val   In input it's escaped constant taken from code, in output
	 *              it's constant ready for showing to end-user
	 * @param type  Data type represented by given constant
	 *
	 * @see         #escapeSequences(StringBuilder, EditorDataType)
	 */
	protected void unescapeSequences(StringBuilder val, EditorDataType type)
	{
		assert type.isString();
		boolean isInQuote = false;
		for (int i = 0; val.length() > i; ++i) {
			char c = val.charAt(i);
			if ('\\' == c) {
				// if we won't skip next char, we'll adjust index later
				++i;
				if (val.length() > i) {
					c = val.charAt(i);
					if (('"' == c || '\\' == c) && !isInQuote) {
						// we found backslash or quote escaped in simple string -
						// let's delete unnecessary backslash
						--i;
						val.deleteCharAt(i);
					}
				}
			}  // if ('\\' == c)
			else if ('"' == c) {
				// we won't meet quote in other place than on array elements boundaries
				isInQuote = !isInQuote;
			}
		}  // for (int i = 0; val.length() > i; ++i)
	}

	/**
	 * Removes all line wrapping in test case parameters made by
	 * <code>guardLongLines</code>.
	 *
	 * @param val   String parameter value that need to be unwrapped.
	 *
	 * @see         #guardLongLines(StringBuilder)
	 */
	private void unguardLongLines(StringBuilder val)
	{
		// First we will form pattern for finding wrappings. And we will do this
		// only at the first call. We can't do this in constructor 'cause
		// language-specific constants are not set yet.
		if (null == patUnguardLongLines) {
			StringBuilder sbTemp = new StringBuilder(30);
			// Make string fully conformed to string added in guardLongLines
			sbTemp.append('"').append(sStringsAdd).append(sLineContinued);
			// Escape some symbols that are not allowed in regexps
			for (int i = 0; sbTemp.length() > i; ++i) {
				char c = sbTemp.charAt(i);
				if ('+' == c) {
					// For '+' we only insert backslash
					sbTemp.insert(i, '\\');
					++i;
				}
				else if (' ' == c) {
					// For space we replace it with any number of any whitespace
					sbTemp.setCharAt(i, '*');
					sbTemp.insert(i, "\\s");
					i += 2;
				}
			}
			sbTemp.append(StringsUtil.sCRLFregex).append("\\s*\"");
			// We have to use multiline pattern for catching line ends
			patUnguardLongLines = Pattern.compile(sbTemp.toString(), Pattern.DOTALL);
		}

		// Now we simply delete all that matches with our pattern
		Matcher mat = patUnguardLongLines.matcher(val);
		while (mat.find(0))
			val.delete(mat.start(), mat.end());
	}

	/**
	 * Removes all whitespace between elements in array value. All changes are
	 * made inplace.
	 *
	 * @param val           Value to be changed
	 */
	private static void removeSpaceInArray(StringBuilder val)
	{
		boolean isInQuote = false;
		for (int i = 0; val.length() > i; ++i) {
			char c = val.charAt(i);
			// If it is escaping symbol then we have to skip next quote to
			// not misinterpret it as element separator
			if ('\\' == c && val.length() > i + 1 && '"' == val.charAt(i + 1))
			    ++i;

			// If we are not inside quotes then no matter where we are we don't
			// need any white space
			if (!isInQuote) {
				StringsUtil.removeAllNextSpace(val, i);
				if (val.length() <= i)
					break;
				c = val.charAt(i);
			}

			if ('"' == c)
			    isInQuote = !isInQuote;
		}  // for (int i = 0; val.length() > i; ++i)
	}

	/**
	 * Makes some value taken from the code suitable for internal use and
	 * for showing to end-user. All transformations depend on type of the value
	 * and all are made inplace. All action is strictly opposite to
	 * <code>prepareValForCode</code>.
	 *
	 * @param val   On entering it's value taken from code. On returning it's
	 *              value ready for showing to user.
	 * @param type  Data type of inspected value.
	 *
	 * @see         #prepareValForCode(StringBuilder, EditorDataType)
	 **/
	private void extractValFromCode(StringBuilder val, EditorDataType type)
	{
		boolean isString = type.isString();
		boolean isArray = type.isArrayType();
		if (isArray) {
			// for arrays we need to remove curly brackets
			StringsUtil.removeArrayMarks(val);
		}  // if (isArray)
		else if (isString) {
			// for strings (and not string arrays) we need to remove quotes around
			StringsUtil.removeStringMarks(val);
		}

		if (isString) {
			// for strings and string arrays we need to collapse multiple lines
			// and remove escapings from value
			unguardLongLines(val);
			unescapeSequences(val, type);
		}  // if (isString)
		else {
			// all other types are numbers, so we need to remove prefixes
			// and postfixes around each number (in case it is array)
			Pattern pat = getNumExtractPattern(type);
			if (null != pat) {
				Matcher mat = pat.matcher(val);
				int nextPos = 0;
				// we do all inplace without creating new classes
				while (mat.find(nextPos)) {
					String grp = mat.group(1);
					val.replace(mat.start(), mat.end(), grp);
					nextPos = mat.start() + grp.length();
				}
			}  // if (null != pat)
		}  // if (isString) else

		// Additionally for arrays we need to erase all white space between elements
		if (isArray)
			removeSpaceInArray(val);
	}

	/**
	 * Gets pattern for finding beginning of test in the code. Closely related
	 * to code generated by <code>markTestBegin</code>.
	 *
	 * @return      Pattern to be used for searching test case beginning.
	 *
	 * @see         #markTestBegin(int)
	 * @see         #patTestBegin
	 */
	private Pattern getTestBeginPat()
	{
		// Pattern depends only on current language. So we can create ot only once.
		if (null == patTestBegin) {
			patTestBegin = Pattern.compile(sLineComment + "\\s*-+\\s*test\\s+\\d+\\s*-+",
			                               Pattern.DOTALL);
		}
		return patTestBegin;
    }
    
    /**
     * Gets pattern for finding the statement that marks the test case as disabled.
     **/
    private Pattern getDisabledTruePat()
    {
        // Pattern depends only on current language. So we can create ot only once.
        if (null == patDisabledTrue ) {
            patDisabledTrue = Pattern.compile( sTestDisabled + "\\s*=\\s*" + sTrue, Pattern.DOTALL );
        }
        return patDisabledTrue;
	}

	/**
	 * Gets regular expression for finding test case parameter initialization.
	 *
	 * @param num       Number of parameter
	 * @param type      Type of parameter
	 * @return          Regular expression for finding this parameter in code
	 */
	protected String getTestParamRegex(int num, EditorDataType type)
	{
		String res;
		// Expression slightly different for arrays and non-arrays and
		// is the same for all languages except C++.
        if (type.isArrayType()) {
			res = sArrayParamVarPrefix + num + ".*? = .*?(\\{.*?\\})"
			      + sLineEnd + StringsUtil.sCRLFregex;
        } else {
			res = sTestParamVarPrefix + num + " = (.*?)" + sLineEnd + StringsUtil.sCRLFregex;
        }
		return res;
	}

	/**
	 * Parses one param in test case call. When parameter successfully parsed
	 * it is put in <code>parsedParams</code> in newly created
	 * <code>StringBuilder</code>.
	 *
	 * @param num       Number of parameter that need to be parsed
	 * @param maxPos    Maximum position in test code where parameter can be
	 *                  (limited to end of the test case call)
	 * @return          If parameter parsed successfully then returns
	 *                  <code>true</code>, in other case returns <code>false</code>.
	 */
	private boolean parseTestParam(int num, int maxPos)
	{
		boolean res = false;
		if (matParams[num].find(curParseInd) && matParams[num].start() < maxPos) {
			parsedParams[num] = new StringBuilder(100);
			// Some workaround for C++: its pattern contains 2 parentheses and
			// one of them will match
			int grp = 1;
			if (matParams[num].start(grp) < 0)
				grp = 2;
			parsedParams[num].append(parsingTests,
								matParams[num].start(grp), matParams[num].end(grp));
			res = true;
		}
		return res;
	}

	/**
	 * Parses one test case in test code. Returns <code>true</code> if parsing
	 * process should be continued because test case parsed successfully or it has
	 * test case start and test case end but have some errors inside.
	 *
	 * @return      If test parsing process should be continued
	 */
	private boolean parseNextTest()
	{
		boolean res = false;
		// For success we need at least test start and test end
		if (matTestBegin.find(curParseInd)) {
			// We already can move on current parsing position to not come here again
			curParseInd = matTestBegin.start() + 1;
			if (matTestEnd.find(curParseInd)) {
				int endInd = matTestEnd.start();

				// Let's parse test case parameters
				boolean testValid = true;
				for (int i = 0; paramTypes.length > i; ++i) {
					if (!parseTestParam(i, endInd)) {
						testValid = false;
						break;
					}
				}
                // Let's parse disabled 
                testCaseDisabled = false;
                if ( matDisabledTrue.find(curParseInd) ) {
                    testCaseDisabled = (matDisabledTrue.start() < endInd);
                }
				if (testValid) {
					// If all parameters successfully parsed then try to parse
					// result and add test case to class declaration
                    if (!parseTestParam(paramTypes.length, endInd)) {
						parsedParams[paramTypes.length] = null;
                    }
					addTestCase();
				}

				// Don't forget to move on current parsing position and success result
				curParseInd = endInd;
				res = true;
			}  // if (matTestEnd.find(curParseInd))
		}  // if (matTestBegin.find(curParseInd))
		return res;
	}

	/**
	 * Adds test case constructeed from inner parameters filled during parsing.
	 */
	private void addTestCase()
	{
		// Make new parameters array that will not contain result
		StringBuilder[] params = new StringBuilder[paramTypes.length];
		for (int i = 0; paramTypes.length > i; ++i) {
			// Object will not be recreated here. So we pass to Test the same
			// objects that stored in this class.
			params[i] = parsedParams[i];
		}
        addTestCase(cl, params, parsedParams[paramTypes.length], testCaseDisabled);
	}

	/**
	 * Adds test case constructed from external parameters to class definition.
	 * Before creating test object all parameters and result are cleaned out by
	 * <code>extractValFromCode</code>.
	 * If test case must be without answer then <code>aResult</code> must be
	 * <code>null</code>.
	 *
	 * @param classProblem  Class definition to add test case to
	 * @param aParams       Array of test case parameters' values
	 * @param aResult       Result value of test case or <code>null</code> if this
	 *                      test must be without answer
	 *
     * @see                 #addTestCase(ClassDecl, StringBuilder[], StringBuilder, boolean)
	 */
	public void addTestCase(ClassDecl classProblem, StringBuilder[] aParams,
	                        StringBuilder aResult)
    {
        addTestCase(classProblem, aParams, aResult, false);
    }

        /**
     * Adds test case constructed from external parameters to class definition.
     * Before creating test object all parameters and result are cleaned out by
     * <code>extractValFromCode</code>.
     * If test case must be without answer then <code>aResult</code> must be
     * <code>null</code>.
     *
     * @param classProblem  Class definition to add test case to
     * @param aParams       Array of test case parameters' values
     * @param aResult       Result value of test case or <code>null</code> if this
     *                      test must be without answer
     * @param disabled      Is the test case disabled?
     *
     * @see                 #extractValFromCode(StringBuilder, EditorDataType)
     */
    public void addTestCase(ClassDecl classProblem, StringBuilder[] aParams,
                            StringBuilder aResult, boolean disabled)
	{
		EditorDataType parTypes[] = classProblem.getMethod().getParamTypes();
		for (int i = 0; parTypes.length > i; ++i)
			extractValFromCode(aParams[i], parTypes[i]);
		if (null != aResult)
			extractValFromCode(aResult, classProblem.getMethod().getReturnType());

		// Finally add test case to class definition
        classProblem.addTest(aParams, aResult, disabled);
	}

	/**
	 * Main function for extracting test cases from program code. Method deletes
	 * test code in <code>code</code>, put this test code in <code>testCode</code>,
	 * parses all test cases in it and puts them into <code>classProblem</code>.
	 *
	 * @param code              All program code for analyzing
	 * @param classProblem      Class definition for receiving test cases
	 * @param testCode          Buffer for receiving test code deleted
	 *                          from <code>code</code>
	 */
	public final void extractTestCases(StringBuilder code, ClassDecl classProblem,
	                                   StringBuilder testCode)
	{
		// First let's find our test code region
		int startInd = code.indexOf(sTestRegionStart);
		if (0 <= startInd) {
			int endInd = code.indexOf(sTestRegionEnd, startInd);
			if (0 <= endInd) {
				// A bit more searching to extract test code on line boundaries
				startInd = code.lastIndexOf("\n", startInd) + 1;
				int realEndInd = code.indexOf("\n", endInd);
				endInd = realEndInd > endInd? realEndInd: code.length();
				// Now extract test code and place instead of it our marker
				testCode.setLength(0);
				testCode.append(code, startInd, endInd);
				StringsUtil.replace(code, startInd, endInd, sTestingCodeTag);

				// Initialize instance variables used in parsing
				parsingTests = testCode;
				cl = classProblem;
				paramTypes = cl.getMethod().getParamTypes();
				retType = cl.getMethod().getReturnType();

				// For convenience arrays for params are made one element more
				// in size - for return parameter too.
				matParams = new Matcher[paramTypes.length + 1];
				parsedParams = new StringBuilder[paramTypes.length + 1];

				// Create all Matchers that will be needed during parsing
                matDisabledTrue = getDisabledTruePat().matcher(parsingTests);
				matTestBegin = getTestBeginPat().matcher(parsingTests);
                matTestEnd = Pattern.compile(getTestEndExpr(), Pattern.DOTALL)
										.matcher(parsingTests);
				for (int i = 0; paramTypes.length > i; ++i) {
					matParams[i] = Pattern.compile(getTestParamRegex(i, paramTypes[i]),
					                               Pattern.DOTALL).matcher(parsingTests);
				}
				matParams[paramTypes.length] = Pattern.compile(
												getTestParamRegex(paramTypes.length, retType),
				                                Pattern.DOTALL).matcher(parsingTests);

				// Now let's parse tests
				curParseInd = 0;
				while (parseNextTest());  // do nothing - all done in parseNextTest()
			}  // if (0 <= endInd)
		}  // if (0 <= startInd)
	}
}
