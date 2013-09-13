package kawigi.problem;

import kawigi.util.*;


/**
 * This class takes some set of parameters and generates a ClassDecl from it.
 **/
public final class ClassDeclFactory
{
	/**
	 * Private constructor for utility class
	 */
	private ClassDeclFactory() {}

	/**
	 * Generator used in current working mode.
	 */
	private static ClassDeclGenerator generator;
	
	/**
	 * Determines the type of ClassDeclGenerator that should be used according
	 * to the current application mode.
	 *
	 * If we are in standalone mode, this returns the problem parser. If we
	 * are in plugin mode, this returns an object that converts TopCoder's
	 * ProblemComponent into a ClassDecl.
	 **/
	private static ClassDeclGenerator getGenerator()
	{
		if (generator == null) {
			if (AppEnvironment.getEnvironment() == AppEnvironment.PluginMode)
				generator = new TCProblemConverter();
			else
				generator = new ProblemParser();
		}
		
		return generator;
	}

	/**
	 * Generates a ClassDecl given some parameters.
	 *
	 * This calls getClassDecl on the ClassDeclGenerator returned by
	 * getGenerator.  If the application is in standalone mode, the parameters
	 * will be ignored.  If we are in plugin mode, the first parameter
	 * <b>must</b> be a TopCoder ProblemComponent and the second parameter
	 * <b>must</b> be a TopCoder Language. If either is not the right type or
	 * there are fewer than two parameters, things will crash and be generally
	 * unhappy.
	 *
	 * @param params        Parameters for ClassDecl generation
	 **/
	public static ClassDecl getClassDecl(Object ... params)
	{
		return getGenerator().getClassDecl(params);
	}

	/**
	 * Parses problem statement and generates a ClassDecl once more from the same
	 * parameters as previously was generated in getClassDecl.
	 *
	 * @see     #getClassDecl(Object...)
	 **/
	public static ClassDecl reparseClassDecl()
	{
		return getGenerator().reparseClassDecl();
	}
}
