package kawigi.problem;

import java.util.ArrayList;
import java.util.List;


/**
 * This class represents the code side of a problem statement.
 *
 * This means it has the class name, the information on the method you are
 * required to write, and a list of tests (example or added by you).
 *
 * This serves many of the same purposes that TopCoder's ProblemComponent does,
 * and most of the data from a ProblemComponent is converted into one of these
 * objects for KawigiEdit's use.  The reason I don't just use the
 * ProblemComponent instead of having this class is so that I can have this
 * class independent of TopCoder's classes and use it in stadalone mode.  Then,
 * assuming I can properly parse a problem statement
 * I can generate code the exact same way I would in
 * plugin mode, and standalone won't suffer from having re-implemented code
 * generation (it can just suffer from inadequate problem parsing).
 **/
public final class ClassDecl
{
	/**
	 * Name of the class you need to implement.
	 **/
	private StringBuilder className;
	/**
	 * Information on the method you need to implement.
	 **/
	private MethodDecl method;
	/**
	 * List of test cases for this problem.
	 **/
	private List<Test> tests = new ArrayList<Test>();

	/**
	 * Constructs a new ClassDecl with the given name and method. Given objects
	 * are simply stored in the class, so they shouldn't be changed later.
	 **/
	public ClassDecl(StringBuilder className, MethodDecl method)
	{
		this.className = className;
		this.method = method;
	}

	/**
	 * Constructs a new ClassDecl with the given name and method. Given MethodDecl
	 * is simply stored in the class, so it shouldn't be changed later.
	 **/
	public ClassDecl(String className, MethodDecl method)
	{
		this.className = new StringBuilder(className);
		this.method = method;
	}

	/**
	 * Adds this test to the list of tests for this problem.
	 *
	 * @param t     Test to add to the problem
	 **/
	public void addTest(Test t)
	{
		tests.add(t);
	}

	/**
	 * Adds test constructed from parameters and result to the list of tests.
	 *
	 * @param params   Parameter values for test
	 * @param result   Desired result of this test
	 */
	public void addTest(StringBuilder[] params, StringBuilder result)
	{
		addTest(new Test(result, params));
	}
	/**
	 * Adds test constructed from parameters and result to the list of tests.
	 *
	 * @param params   Parameter values for test
	 * @param result   Desired result of this test
	 * @param disabled Is it disabled?
	 */
	public void addTest(StringBuilder[] params, StringBuilder result, boolean disabled)
	{
		addTest(new Test(result, params, disabled));
	}

	/**
	 * Returns the name of this class. Returned object shouldn't be changed.
	 *
	 * @return      Name of the class
	 **/
	public StringBuilder getName()
	{
		return className;
	}

	/**
	 * Returns the method declaration that needs to be implemented in this class.
	 * Returned object shouldn't be changed.
	 *
	 * @return      Method declaration
	 **/
	public MethodDecl getMethod()
	{
		return method;
	}

	/**
	 * Returns the current number of tests for this problem.
	 *
	 * @return      Number of tests in class
	 **/
	public int countTests()
	{
		return tests.size();
	}

	/**
	 * Returns the test case at the given index. If index is out of bounds then
	 * <code>IndexOutOfBoundsException</code> is thrown.
	 *
	 * @param index     Index of test to retrieve
	 * @return          Test at the specified index
	 **/
	public Test getTest(int index)
	{
		return tests.get(index);
	}

	/**
	 * Changes the test case at the given index.
	 *
	 * @param index     Index of test to change
	 * @param t         Test to be put at the specified index
	 **/
	public void setTest(int index, Test t)
	{
		tests.set(index, t);
	}

	/**
	 * Deletes the test case at the given index.
	 *
	 * @param index     Index of the test to be removed
	 **/
	public void removeTest(int index)
	{
		tests.remove(index);
	}

	/**
	 * Deletes all test cases.
	 **/
	public void removeAllTests()
	{
		tests.clear();
	}
}
