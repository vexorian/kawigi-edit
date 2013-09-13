package kawigi.language;

import java.io.File;

import kawigi.properties.PrefFactory;
import kawigi.util.StringsUtil;


/**
 * Class introducing language-dependent features for C++.
 * Class is made as singleton.
 *
 * @see     EditorLanguage
 */
public final class CPPLang extends EditorLanguage
{
	/**
	 * Single instance of this class
	 */
	private static final CPPLang inst = new CPPLang();

	/**
	 * Returns single instance of the class.
	 *
	 * @return      Instance of <code>CPPLang</code>
	 */
	public static CPPLang getInstance()
	{
		return inst;
	}

    /**
     * Array variable regex may appear as both pNum and tNum
     * This variable is a quick hack:
     **/
	private final String sCombinedArrayPattern = "[tp]";

	/**
	 * Default constructor.
	 */
	private CPPLang()
	{
		super();
		// Fill the map of our type names
		fillTypeNames(getAllTypeNames());
		// And fill all code-generation and local compiling variables which are
		// special for C++.
		sDefaultFileName = "$PROBLEM$.cpp";
		sDefaultCompileCommand = "g++ $PROBLEM$.cpp";
		sDefaultExecuteCommand = '/' == File.separatorChar ? "./a.out" : "$CWD$\\a.exe";
		sPrintPrefix = "cout << ";
		sPrintPostfix = "";
		sPrintlnPrefix = "cout << ";
		sPrintlnPostfix = " << endl";
		sPrintStrAdd = " << ";
		sObjMethodCall = "->";
		sDoubleAbsFunc = "fabs";
		sDoubleMaxFunc = "max";
		sArrayLenFunc = ".size()";
		sDeleteObjOp = "delete ";
		sStringsAdd = "";
		sArrayParamVarPrefix = "t";
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
		return new String[] {"string", "int", "double", "long long", "bool", "vector <string>", "vector <int>", "vector <double>", "vector<long long>"};
	}

	/**
	 * Gets type name in C++ given representation of necessary type
	 * in EditorDataType enumeration. For C++ method especially takes care
	 * of long long type which name can differ from standard in local compiler.
	 *
	 * @param type      Type to convert to name
	 * @return          Type name in C++
	 */
	public String getTypeName(EditorDataType type)
	{
	    String res = super.getTypeName(type);
	    // Special case for long long in C++ - it have to be changed
		// (perhaps for correct compile in Visual Studio or for some other purposes)
	    if (res.contains("long long")) {
		    String repl = PrefFactory.getPrefs().getProperty(getPropertyCategory() + ".lltype", "long long");
	        res = res.replaceAll("long long", repl);
	    }
	    return res;
	}

	/**
	 * Returns the name of this language in lowercase ('cpp').
	 * This name is used in name of category in properties for this language.
	 *
	 * @return      Name of this language - 'cpp'
	 */
	public String toString()
	{
		return "cpp";
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
		text("#include <iostream>").endLine();
		text("#include <string>").endLine();
		text("#include <vector>").endLine();
        text("#include <ctime>").endLine();
		text("#include <cmath>").endLine();
		text("using namespace std").endCodeLine();
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
		return text(getTypeName(funcRetType)).text(' ');
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
		text("for (int i = 0; int(").text(arrayName).text(sArrayLenFunc)
							.text(") > i; ++i) {").endLine();
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
	{
		text(typeName).text(' ');
		// If this is not the builtin type and this is not the type generated
		// by custom "long long" replacement, then it is problem solver class.
		// In this case we need to make pointer variable, not class itself.
		if (null == EditorLanguage.getType(typeName)
		    && !StringsUtil.isEqual(typeName, getTypeName(EditorDataType.Long))
		    && !StringsUtil.isEqual(typeName, getTypeName(EditorDataType.LongArray)))
		{
			text('*');
		}
		text(varName).endCodeLine();
	}

	/**
	 * Method for adding code of remembering current time.
	 *
	 * @param varName   Name of variable to remember time in.
	 *
	 * @see             EditorLanguage#rememberCurTime(String)
	 */
	protected void rememberCurTime(String varName)
	{
		text("clock_t ").text(varName).text(" = clock()").endCodeLine();
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
		text("double(").text(varEnd).text(" - ").text(varStart).text(") / CLOCKS_PER_SEC");
		return this;
	}

	/**
	 * Adds header of main procedure.
	 *
	 * @see             EditorLanguage#mainSubDef()
	 */
	protected void mainSubDef()
	{
		text("int main() {");
		super.mainSubDef();
	}

	/**
	 * Adds code finalizing main procedure.
	 *
	 * @see             EditorLanguage#endMainSub()
	 */
	protected void endMainSub()
	{
		text("return 0").endCodeLine();
		super.endMainSub();
	}

	/**
	 * Adds mark of the beginning of test. This mark will be used at code loading
	 * to find the place where test data begins. For pre-c++11 C++ we need also put each test
	 * call to different block to be able declare array variables with the same name.
	 *
	 * @param num   Number of the test
	 *
	 * @see         EditorLanguage#markTestBegin(int)
	 */
	protected void markTestBegin(int num)
	{
	    if ( ! PrefFactory.getPrefs().getBoolean(getPropertyCategory()+".cpp11", true) ) {
	        text('{').endLine();
		}
		super.markTestBegin(num);
	}

	/**
	 * Adds mark of ending of the code related to one test case. This mark is made
	 * basically on beauty of generated code purposes. For pre-c++11 C++ we additionally close
	 * block opened in <code>markTestBegin</code>.
	 *
	 * @see         #markTestBegin(int)
	 * @see         EditorLanguage#markTestEnd()
	 */
	protected void markTestEnd()
	{
		super.markTestEnd();
		if ( ! PrefFactory.getPrefs().getBoolean(getPropertyCategory()+".cpp11", true) ) {
		    text('}').endLine();
		}
	}

	/**
	 * Gets prefix for given data type in C++.
	 *
	 * @param type      Data type to check for prefix.
	 * @return          Prefix string for this type in C++.
	 *
	 * @see             #getNumTypePostfix(EditorDataType)
	 * @see             EditorLanguage#getNumTypePrefix(EditorDataType)
	 */
	protected String getNumTypePrefix(EditorDataType type)
	{
		String res;
		// For 'long long' we have special prefix
		if (type.isType(EditorDataType.Long)) {
	        res = PrefFactory.getPrefs().getProperty(
			                           getPropertyCategory() + ".llprefix", "");
		} else {
			res = super.getNumTypePrefix(type);
		}
		return res;
	}

	/**
	 * Gets postfix for given data type in C++.
	 *
	 * @param type      Data type to check for postfix.
	 * @return          Postfix string for this type in C++.
	 *
	 * @see             #getNumTypePrefix(EditorDataType)
	 * @see             EditorLanguage#getNumTypePostfix(EditorDataType)
	 */
	protected String getNumTypePostfix(EditorDataType type)
	{
		String res;
		// For 'long long' we have special postfix
		if (type.isType(EditorDataType.Long)) {
	        res = PrefFactory.getPrefs().getProperty(
			                           getPropertyCategory() + ".llpostfix", "ll");
		} else {
			res = super.getNumTypePostfix(type);
		}
		return res;
	}

	/**
	 * Escapes all special characters in string constants or string-array constants.
	 * For C++ it additionally gets rid of double question marks as they form
	 * trigraphs.
	 *
	 * @param val   In input it's constant to be escaped, in output it's
	 *              escaped constant ready for inserting in code.
	 * @param type  Data type represented by given constant.
	 *
	 * @see         #unescapeSequences(StringBuilder, EditorDataType)
	 * @see         EditorLanguage#escapeSequences(StringBuilder, EditorDataType)
	 */
	protected void escapeSequences(StringBuilder val, EditorDataType type)
	{
		assert type.isString();
		super.escapeSequences(val, type);
		// Removing the posibility of trigraph interpretation
		// We don't need any trigraphs at all
		for (int i = 0; val.length() > i; ++i) {
			if ('?' == val.charAt(i)) {
				++i;
				if (val.length() > i && '?' == val.charAt(i)) {
					// I deliberately insert one space to make this sequence different
					// from VB quote escaping
					val.insert(i, "\" \"");
				}
			}
		}
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
	    if (PrefFactory.getPrefs().getBoolean(getPropertyCategory()+".cpp11", true) ) {
	        text(paramVarNames[num]).text(" = ").text(value).endCodeLine();
	    } else {
            if ("{}".equals(value.toString())) {
                text(paramVarNames[num]).text(".clear() /*{}*/").endCodeLine();
            } else {
                text(getTypeName(type.getPrimitiveType())).text(' ')
                        .text(sArrayParamVarPrefix).text(num)
                        .text("[] = ").text(value).endCodeLine();
                text("\t\t").text(paramVarNames[num])
                        .text(".assign(").text(sArrayParamVarPrefix).text(num).comma()
                        .text(sArrayParamVarPrefix).text(num).text(" + sizeof(")
                        .text(sArrayParamVarPrefix).text(num).text(") / sizeof(")
                        .text(sArrayParamVarPrefix).text(num).text("[0]))").endCodeLine();
            }
        }
	}
	//============================================================================

	/*
	  ============================================================================
		Code related to test code parsing and test cases extraction.
	 */
	/**
	 * Removes all escapings in string and string-array constants
	 * made by <code>escapeSequences</code>.
	 *
	 * @param val   In input it's escaped constant taken from code, in output
	 *              it's constant ready for showing to end-user
	 * @param type  Data type represented by given constant
	 *
	 * @see         #escapeSequences(StringBuilder, EditorDataType)
	 * @see         EditorLanguage#unescapeSequences(StringBuilder, EditorDataType)
	 */
	protected void unescapeSequences(StringBuilder val, EditorDataType type)
	{
		assert type.isString();
		// Return double question marks back
		for (int i = 0; val.length() > i; ++i) {
			char c = val.charAt(i);
			if ('?' == c) {
				++i;
				// In escapeSequences we inserted three characters so here we'll check
				// all three
				if (val.length() > i + 3 && '"' == val.charAt(i)
				        && ' ' == val.charAt(i + 1) && '"' == val.charAt(i + 2)
						&& '?' == val.charAt(i + 3))
				{
					val.deleteCharAt(i);
					val.deleteCharAt(i);
					val.deleteCharAt(i);
					--i;
				}
			}  // if ('?' == c)
		}
		// I deliberately put call to main process here. In such manner we ensure
		// that we don't change string "?\" \"?"
		super.unescapeSequences(val, type);
	}

	/**
	 * Gets regular expression for finding test case parameter initialization.
	 * C++ needs to support both the previous test case format (Including taking
	 * care of the empty vector special case). And also code generated taking
	 * advantage of c++11 support.
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
            res = sCombinedArrayPattern + num + " *\\[? *\\]? *= *?(\\{.*?\\})"
                  + sLineEnd + StringsUtil.sCRLFregex;

			res += "|" + sTestParamVarPrefix + num + "\\.clear\\(\\) /\\*(\\{\\})\\*/"
			            + sLineEnd + StringsUtil.sCRLFregex;
		}
		return res;
	}
}
