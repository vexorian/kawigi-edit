package kawigi.problem;

import com.topcoder.shared.language.*;
import com.topcoder.shared.problem.*;

import kawigi.cmd.ProblemContext;
import kawigi.language.*;


/**
 * This is perhaps one of the most important source files in KawigiEdit,
 * because it converts TopCoder's problem statements into a format that allows
 * KawigiEdit to generate skeleton and test code in plugin mode.
 **/
public final class TCProblemConverter implements ClassDeclGenerator
{
	/**
	 * TopCoder ProblemComponent. Saved here for "reparsing" purposes.
	 */
	private static ProblemComponent component;
	/**
	 * TopCoder Language. Saved here for "reparsing" purposes.
	 */
	private static Language tclang;

	/**
	 * Generates a ClassDecl from the given parameters.
	 *
	 * In this implementation, the first parameter <b>must</b> be a TopCoder
	 * ProblemComponent, and the second parameter <b>must</b> be a TopCoder
	 * Language.
	 *
	 * As a side effect, the KawigiEdit language corresponding to the TopCoder
	 * language given is set as the current language in the ProblemContext
	 * class.
	 *
	 * @param parameters        TopCoder ProblemComponent and TopCoder Language
	 **/
	public ClassDecl getClassDecl(Object ... parameters)
	{
		component = (ProblemComponent)parameters[0];
		tclang = (Language)parameters[1];

		EditorLanguage lang = null;
		if (CPPLanguage.CPP_LANGUAGE == tclang)
			lang = CPPLang.getInstance();
		else if (JavaLanguage.JAVA_LANGUAGE == tclang)
			lang = JavaLang.getInstance();
		else if (CSharpLanguage.CSHARP_LANGUAGE == tclang)
			lang = CSharpLang.getInstance();
		else if (VBLanguage.VB_LANGUAGE == tclang)
			lang = VBLang.getInstance();
        else if (PythonLanguage.PYTHON_LANGUAGE == tclang)
            lang = PythonLang.getInstance();

		ProblemContext.setLanguage(lang);
		return parseClassDecl();
	}

	/**
	 * Convert TopCoder data type into our enum object representing data type.
	 * 
	 * @param tcType	TopCoder type object
	 * @return			KawigiEdit type enum
	 */
	private static EditorDataType getType(DataType tcType)
	{
		EditorDataType resType = EditorDataType.getTypeByTopCoderID(tcType.getID());
		if (resType == null)
			resType = EditorLanguage.getType(tcType.getDescriptor(tclang));
		return resType;
	}
	
	/**
	 * Converting saved component and tclang into ClassDecl.
	 **/
	private static ClassDecl parseClassDecl()
	{
		EditorLanguage lang = ProblemContext.getLanguage();

		// Converting TopCoder return type
		EditorDataType returnType = getType(component.getReturnType());

		// Converting TopCoder parameter types
		DataType[] tcParamTypes = component.getParamTypes();
		EditorDataType[] paramTypes = new EditorDataType[tcParamTypes.length];
		for (int i=0; i<tcParamTypes.length; i++)
			paramTypes[i] = getType(tcParamTypes[i]);

		// Here we already can create final class declaration
		ClassDecl retval = new ClassDecl(component.getClassName(),
		                        new MethodDecl(component.getMethodName(), returnType,
		                                       paramTypes, component.getParamNames()));

		// Converting test cases:
		TestCase[] tests = component.getTestCases();
		if (null != tests)
		{
			for (TestCase test : tests)
			{
				StringBuilder out = new StringBuilder(test.getOutput());
				// We must not change array elements of getInput()
				// because as of 03.12.2006 it changes text of problem statement
				// shown to the user
				String[] tcInputs = test.getInput();
				StringBuilder[] inputs = new StringBuilder[tcInputs.length];
				for (int j = 0; inputs.length > j; ++j) {
					inputs[j] = new StringBuilder(tcInputs[j]);
				}
				lang.addTestCase(retval, inputs, out);
			}
		}

		return retval;
	}

	/**
	 * Converting saved component and tclang into ClassDecl once more.
	 **/
	public ClassDecl reparseClassDecl()
	{
		return parseClassDecl();
	}
}
