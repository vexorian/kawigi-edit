package kawigi.language;

/**
 * Class introducing language-dependent features for Java.
 * Class is made as singleton.
 *
 * @see     EditorLanguage
 */
public final class JavaLang extends EditorLanguage
{
	/**
	 * Single instance of this class
	 */
	private static final JavaLang inst = new JavaLang();

	/**
	 * Returns single instance of the class.
	 *
	 * @return      Instance of <code>JavaLang</code>
	 */
	public static JavaLang getInstance()
	{
		return inst;
	}

	/**
	 * Default constructor.
	 */
	private JavaLang()
	{
		super();
		// Fill the map of our type names
		fillTypeNames(getAllTypeNames());
		// And fill all code-generation and local compiling variables which are
		// special for Java.
		sDefaultFileName = "$PROBLEM$.java";
		sDefaultCompileCommand = "javac $PROBLEM$.java";
		sDefaultExecuteCommand = "java $PROBLEM$";
		sPrintPrefix = "System.out.print(";
		sPrintlnPrefix = "System.out.println(";
		sArrayLenFunc = ".length";
		sDoubleAbsFunc = "Math.abs";
		sDoubleMaxFunc = "Math.max";
	}

	/*
	  ============================================================================
		Common procedures.
	 */
	/**
	 * Returns array of all type names in Java. Type names order is related to
	 * order of values in <code>EditorDataType</code>.
	 *
	 * @return      Array of type names.
	 *
	 * @see         EditorDataType
	 */
	static String[] getAllTypeNames()
	{
		return new String[] {"String", "int", "double", "long", "boolean", "String[]", "int[]", "double[]", "long[]"};
	}

	/**
	 * Returns the name of this language in lowercase ('java').
	 * This name is used in name of category in properties for this language.
	 *
	 * @return      Name of this language - 'java'
	 */
	public String toString()
	{
		return "java";
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
		return source;
	}
	//============================================================================

	/*
	  ============================================================================
		Code related to test code generation.
	 */
	/**
	 * Clears all data stored in class for generating new pack of test code.
	 *
	 * @see     EditorLanguage#clear()
	 */
	protected void clear()
	{
		super.clear();
		indentRight();
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
		return text("private static ").text(getTypeName(funcRetType)).text(' ');
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
		text("long ").text(varName).text(" = System.currentTimeMillis()").endCodeLine();
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
		return text('(').text(varEnd).text(" - ").text(varStart).text(") / 1000.0");
	}

	/**
	 * Adds code of (in)equality checking on two variables that can be arrays.
	 * Make Java-specific string comparison.
	 *
	 * @param varLeft       Name of first variable to compare
	 * @param varRight      Name of second variable to compare
	 * @param type          Type of variables to compare
	 * @param isNotEqual    flag pointing if equal (<code>false</code>) or not-equal
	 *                      (<code>true</code>) sign must be used
	 * @return              All methods of <code>EditorLanguage</code> related to test code generation
	 *                      return <code>this</code> to make possible convinient call chains
	 *
	 * @see                 EditorLanguage#equal(String, String, EditorDataType, boolean)
	 */
	protected EditorLanguage equal(String varLeft, String varRight,
	                              EditorDataType type, boolean isNotEqual)
	{
		if (type.isString()) {
			// If this is string or string array we call method equals
			boolean isArray = type.isArrayType();
			if (isNotEqual)
				not();
			text(varLeft).arrayIndex(isArray).text(".equals(")
					.text(varRight).arrayIndex(isArray).text(')');
		}
		else
			// If this is not string or string array all stuff is realized in EditorLanguage
			super.equal(varLeft, varRight, type, isNotEqual);
		return this;
	}

	/**
	 * Adds header of main procedure.
	 *
	 * @see             EditorLanguage#mainSubDef()
	 */
	protected void mainSubDef()
	{
		text("public static void main(String[] args) {");
		super.mainSubDef();
	}

	/**
	 * Gets postfix for given data type in Java.
	 *
	 * @param type      Data type to check for postfix.
	 * @return          Postfix string for this type in C++.
	 *
	 * @see             EditorLanguage#getNumTypePostfix(EditorDataType)
	 */
	protected String getNumTypePostfix(EditorDataType type)
	{
		String res;
		if (type.isType(EditorDataType.Double))
	        res = "D";
		else if (type.isType(EditorDataType.Long))
	        res = "L";
		else
			res = super.getNumTypePostfix(type);

		return res;
	}
}
