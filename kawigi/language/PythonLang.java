package kawigi.language;

import kawigi.properties.PrefFactory;
import kawigi.util.StringsUtil;

import java.io.File;

/**
 * Class introducing language-dependent features for Python.
 * Class is made as singleton.
 *
 * @see     EditorLanguage
 */
public final class PythonLang extends EditorLanguage
{
    /**
     * Single instance of this class
     */
    private static final PythonLang inst = new PythonLang();

    /**
     * Returns single instance of the class.
     *
     * @return      Instance of <code>CPPLang</code>
     */
    public static PythonLang getInstance()
    {
        return inst;
    }

    /**
     * Default constructor.
     */
    private PythonLang()
    {
        super();
        // Fill the map of our type names
        fillTypeNames(getAllTypeNames());

        // And fill all code-generation and local compiling variables which are
        // special for Python.
        sDefaultFileName = "$PROBLEM$.py";
        sDefaultCompileCommand = "";
        sDefaultExecuteCommand = "python $PROBLEM$.py";
        sPrintPrefix = "sys.stdout.write(str(";
        sPrintPostfix = "))";
        sPrintlnPrefix = "print(str(";
        sPrintlnPostfix = "))";
        sPrintStrAdd = ") + str(";
        sDoubleAbsFunc = "abs";
        sDoubleMaxFunc = "max";
        sArrayLenFunc = ".size()";

        sLineEnd = "";
        sLineComment = "#";
        sIfStat = "if (";
        sThenStat = "):";
        sElseStat = "else:";
        sElseIfStat = "elif (";
        sEndIfStat = "";
        sForEndStat = "";
        sLogicalOr = " or ";
        sLogicalAnd = " and ";
        sLogicalNot = "not ";
        sFuncEnd = "";
        sMainSubEnd = "";

        sTrue = "True";
        sFalse = "False";
    }

    @Override
    protected EditorLanguage arrayLensUnequal(String varLeft, String varRight)
    {
        text("len(").text(varLeft).text(")")
                .text(sUnequalSign).text("len(").text(varRight).text(")");
        return this;
    }

	/**
	 * Gets regular expression for finding test case parameter initialization.
	 * Python uses () instead of {} for arrays.
	 *
	 * @param num       Number of parameter
	 * @param type      Type of parameter
	 * @return          Regular expression for finding this parameter in code
	 *
	 * @see             EditorLanguage#getTestParamRegex(int, EditorDataType)
	 */
	protected String getTestParamRegex(int num, EditorDataType type)
	{
		String res = super.getTestParamRegex(num, type);
		if (type.isArrayType()) {
		    // Valid tuple syntax: (1,2,3..., 5): more than one element
		    // If there is only one lement (2) does not represent a tuple
		    // Must use (2,).
            res = sArrayParamVarPrefix + num + ".*? = .*?\\((.*?),?+\\)"
                  + sLineEnd + StringsUtil.sCRLFregex;
        }
		return res;
	}
    
	/*
	  ============================================================================
		Common procedures.
	 */
    /**
     * Returns array of all type names in C++. Type names order is related to
     * order of values in <code>EditorDataType</code>.
     *
     * @return      Array of type names.
     *
     * @see         EditorDataType
     */
    static String[] getAllTypeNames()
    {
        return new String[] {"string", "integer", "float", "long integer", "boolean", "tuple (string)", "tuple (integer)", "tuple (float)", "tuple (long integer)"};
    }

    /**
     * Returns the name of this language in lowercase ('cpp').
     * This name is used in name of category in properties for this language.
     *
     * @return      Name of this language - 'cpp'
     */
    public String toString()
    {
        return "py";
    }
    //============================================================================

	/*
	  ============================================================================
		Methods related to tag expansion in templates and templates themselves.
	 */
    /**
     * Add to source code taken from TopCoder server testing-code tag in place where
     * it should be normally.
     *
     * @param source	Source code given by TopCoder server
     * @return			Source code that should be shown at the code pane
     */
    public String addAutoTestTag(String source)
    {
        int line_ind = source.lastIndexOf('\n');
        if (line_ind != -1 && line_ind == source.length() - 1) {
            line_ind = source.lastIndexOf('\n', line_ind - 1);
        }
        if (line_ind != -1 && line_ind < source.length() - 2
                && source.charAt(line_ind + 1) == '/' && source.charAt(line_ind + 2) == '/')
        {
            line_ind = source.lastIndexOf('\n', line_ind - 1);
            if (line_ind != -1) {
                source = source.substring(0, line_ind + 1) + EditorLanguage.sTestingCodeTag
                        + source.substring(line_ind + 1);
            }
        }

        return source;
    }
    //============================================================================

	/*
	  ============================================================================
		Code related to test code generation.
	 */
    /**
     * Specific function for inserting some stuff before code generation
     * starts. Adds headers and "using namespace" statement to be able to compile
     * even if user template doesn't include this stuff.
     */
    protected void preamble()
    {
        text("import sys").endLine();
        text("import time").endLine();
    }

    /**
     * Function for inserting prefix of function header.
     *
     * @param funcRetType   The return type of function
     * @return              All methods of <code>EditorLanguage</code> related to test code generation
     *                      return <code>this</code> to make possible convinient call chains
     *
     * @see                 EditorLanguage#funcDefPrefix(EditorDataType)
     * @see                 EditorLanguage#funcDefPostfix(EditorDataType)
     */
    protected EditorLanguage funcDefPrefix(EditorDataType funcRetType)
    {
        return text("def ");
    }

    /**
     * Function for inserting postfix of function header -
     * the part that is put after closing parenthesis after parameters list.
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
        text(":").endLine();
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
        return text(paramName);
    }

    /**
     * Starts iteration over some array variable.
     *
     * @param arrayName     Name of the variable to be iterated
     *
     * @see                 EditorLanguage#iterFirstLine(String)
     */
    protected void iterFirstLine(String arrayName)
    {
        text("for i in range(len(").text(arrayName).text(")):").endLine();
        indentRight();
    }

    /**
     * Declares the variable. Variable can be any of builtin types or
     * a class of problem-solver object.
     *
     * @param varName   Name of the variable to be declared.
     * @param typeName  Type of the variable to be declared.
     *
     * @see             EditorLanguage#varDeclare(CharSequence, CharSequence)
     */
    protected void varDeclare(CharSequence varName, CharSequence typeName)
    {}

    /**
     * Method for adding code of remembering current time.
     *
     * @param varName   Name of variable to remember time in.
     *
     * @see             EditorLanguage#rememberCurTime(String)
     */
    protected void rememberCurTime(String varName)
    {
        text(varName).text(" = time.clock()").endCodeLine();
    }

    /**
     * Method for adding formula counting difference between two
     * time points stored in two different variables in seconds (double value).
     *
     * @param varStart  Variable name of starting moment
     * @param varEnd    Variable name of ending moment
     * @return          All methods of <code>EditorLanguage</code> related to test code generation
     *                  return <code>this</code> to make possible convinient call chains
     *
     * @see             EditorLanguage#timeDiff(String, String)
     */
    protected EditorLanguage timeDiff(String varStart, String varEnd)
    {
        text("(").text(varEnd).text(" - ").text(varStart).text(")");
        return this;
    }

    /**
     * Adds code for test call of problem solver with remembering of it's working
     * time.
     */
    protected void callProblemSolver()
    {
        // Creating object
        text(sSolverVarName).text(" = ").text(cl.getName()).text("()").endCodeLine();
        // Calling main solving method
        rememberCurTime(sStartTimeVarName);
        text(sAnswerVarName).text(" = ")
                .methodCall(sSolverVarName, cl.getMethod().getName())
                .text('(').passTestParams().text(')').endCodeLine();
        rememberCurTime(sEndTimeVarName);
    }

    /**
     * Creates string made of parameter names. paramNames should not be null for Python.
     *
     * @param res           Place for the final string
     * @param paramTypes    Types of parameters
     * @param paramNames    Names of parameters
     */
    protected void makeCStyleParams(StringBuilder res, EditorDataType[] paramTypes,
                                    StringBuilder[] paramNames)
    {
        res.setLength(0);
        if (null != paramNames)
            res.append(paramNames[0]);
        for (int i = 1; paramTypes.length > i; ++i) {
            res.append(", ");
            if (null != paramNames)
                res.append(paramNames[i]);
        }
    }



    /**
     * Adds header of main procedure.
     *
     * @see             EditorLanguage#mainSubDef()
     */
    protected void mainSubDef()
    {}

    /**
     * Adds code finalizing main procedure.
     *
     * @see             EditorLanguage#endMainSub()
     */
    protected void endMainSub()
    {}

    /**
     * Counts the number of elements in a string representing a tuple:
     * @param value    A StringBuilder containing the tuple "(a,b,...)"
     */
    private int countTupleElements(StringBuilder value)
    {
        // Need to beware about things like commas inside string literals.
        boolean stringOpen = false;
        boolean control = false;
        int totalElements = 1;
        boolean nonEmpty = false;
        if (    value.length() == 0 
             || value.charAt(0) != '(' || value.charAt(value.length()-1) != ')' ) {
                  // invalid tuple!?
                  return 0;  
           }
        
        for (int i = 1; i < value.length() - 1; i++) {
            char ch = value.charAt(i);
            if (stringOpen) {
                if ( control ) {
                    control = false;
                } else if (ch == '\\') {
                    control = true;
                } else if (ch == '"') {
                    stringOpen = false;
                }
            } else {
                if (ch > ' ') {
                    nonEmpty = true;
                    if (ch == '"') {
                        stringOpen = true;
                    } else if (ch == ',') {
                        totalElements++;
                    }
                }
            }
        }
        if (! nonEmpty) {
            totalElements = 0;
        }
        return totalElements;

    }
    
    /**
     * Adds code of array test case parameter initializing.
     *
     * @param num       Sequential number of the parameter
     * @param type      Type of the parameter
     * @param value     Value of the parameter ready for inserting into the code
     *
     * @see             EditorLanguage#initTestParam(int, EditorDataType, StringBuilder)
     */
    protected void initArrayParam(int num, EditorDataType type, StringBuilder value)
    {
        value.replace(0, 1, "(");
        value.deleteCharAt(value.length()-1);
        value.append(")");
        if ( countTupleElements(value) == 1) {
            value.deleteCharAt(value.length()-1);
            value.append(",)");
        }

        text(paramVarNames[num]).text(" = ").text(value).endCodeLine();
    }

}
