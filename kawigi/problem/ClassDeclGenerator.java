package kawigi.problem;

/**
 * Interface implemented by the TopCoder problem converter and the problem
 * statement parser.
 **/
public interface ClassDeclGenerator
{
	/**
	 * Returns the ClassDecl of the current problem.
	 *
	 * @param parameters    Parameters for ClassDecl generation
	 **/
	public ClassDecl getClassDecl(Object ... parameters);

	/**
	 * "Parses" problem statement and returns the ClassDecl from the same parameters
	 * as previously made in getClassDecl.
	 **/
	public ClassDecl reparseClassDecl();
}
