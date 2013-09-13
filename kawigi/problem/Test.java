package kawigi.problem;

import kawigi.util.StringsUtil;


/**
 * Represents a test case.
 *
 * Normally when one writes a class called "Test", it's not meant to be in the
 * final code base, but this case is an exception :-)
 **/
public final class Test implements Cloneable
{
	/**
	 * The value of the intended answer, as a string.
	 **/
	private StringBuilder answer;
	/**
	 * The flag showing if checking of the answer correctness is necessary.
	 **/
	private boolean needAnswer;
	/**
	 *	The values of the parameters for the test.
	 **/
	private StringBuilder[] parameters;
	/**
	 *  True if and only if the test case is disabled?
	 **/
	private boolean disabled;

	/**
	 * Constructs a new Test with the given parameters without answer checking.
	 * Array and StringBuilder's themselves are saved in class so that they shouldn't
	 * be changed later.
	 *
	 * @param parameters    Parameters for the test
	 *
	 * @see                 #Test(StringBuilder, StringBuilder[])
	 **/
	public Test(StringBuilder[] parameters)
	{
		this.parameters = parameters;
		needAnswer = false;
		answer = new StringBuilder(0);
		disabled = false;
	}

	/**
	 * Constructs a new Test with the given answer and parameters.
	 * Array and StringBuilder's themselves are saved in class so that they shouldn't
	 * be changed later.
	 *
	 * @param answer       Answer of the test
	 * @param parameters   Parameters for the test
	 *
	 * @see                 #Test(StringBuilder[])
	 **/
	public Test(StringBuilder answer, StringBuilder[] parameters)
	{
		this.answer = null == answer? new StringBuilder(0): answer;
		needAnswer = null != answer;
		this.parameters = parameters;
		disabled = false;
	}

	/**
	 * Constructs a new Test with the given answer and parameters.
	 * Array and StringBuilder's themselves are saved in class so that they shouldn't
	 * be changed later.
	 *
	 * @param answer       Answer of the test
	 * @param parameters   Parameters for the test
	 * @param disabled     Determines if Test case is disabled 
	 *
	 * @see                 #Test(StringBuilder, StringBuilder[])
	 **/
	public Test(StringBuilder answer, StringBuilder[] parameters, boolean disabled)
	{
		this.answer = null == answer? new StringBuilder(0): answer;
		needAnswer = null != answer;
		this.parameters = parameters;
		this.disabled = disabled;
	}
	
	
	/**
	 * Returns the intended output for this test. Returned StringBuilder shouldn't
	 * be changed later because inner value is returned.
	 *
	 * @return      Output of the test in StringBuilder
	 **/
	public StringBuilder getAnswer()
	{
		return answer;
	}

	/**
	 * Changes the intended output for this test. Passed StringBuilder is saved in
	 * class, so it shouldn't be changed later
	 *
	 * @param answer        New value of the intended answer
	 **/
	public void setAnswer(StringBuilder answer)
	{
		this.answer = null == answer? new StringBuilder(0): answer;
	}

	/**
	 * Checks the necessarity of the answer.
	 *
	 * @return      Boolean value pointing if answer must be checked for this test
	 **/
	public boolean isWithAnswer()
	{
		return needAnswer;
	}
	
	/**
	 * Changes the necessarity of the answer.
	 *
	 * @param needAnswer    Boolean value pointing if answer must be checked for this test
	 **/
	public void setWithAnswer(boolean needAnswer)
	{
		this.needAnswer = needAnswer;
	}
	
	/**
	 * Returns true if the test case is disabled.
	 */
	public boolean isDisabled() {
	    return disabled;
	}
	/**
	 * Disables/Enables the test case.
	 *
	 * @param disabled    Boolean value: if true, disables the case else it enables it.
	 **/
	public void setDisabled(boolean disabled)
	{
		this.disabled = disabled;
	}

	/**
	 * Returns the input parameters for this test. Returned array and its elements
	 * are those saved in class, so thay shouldn't be changed later.
	 *
	 * @return      Array of string parameter values for the test
	 **/
	public StringBuilder[] getParameters()
	{
		return parameters;
	}

	/**
	 * Copies this object into another one.
	 *
	 * @return      Copy of this Test object
	 **/
	public Object clone() throws CloneNotSupportedException
	{
		Test o = (Test)super.clone();
		// All parameters' values must be copied and be unique for class
		o.parameters = new StringBuilder[parameters.length];
		for (int i = 0; parameters.length > i; ++i)
			o.parameters[i] = new StringBuilder(parameters[i]);
		o.answer = new StringBuilder(answer);
		o.disabled = disabled;
		return o;
	}

	/**
	 * Checks if this test case is the same as another test case.
	 *
	 * @param obj       Object to compare with
	 * @return          Boolean value stating if this object is equal to <code>obj</code>
	 **/
	public boolean equals(Object obj)
	{
		boolean res = false;
		if (obj instanceof Test) {
			Test o = (Test)obj;
			// Checking all that we have on equality
			if (needAnswer == o.needAnswer &&
			        (!needAnswer || StringsUtil.isEqual(answer, o.answer))
			        && parameters.length == o.parameters.length)
			{
				res = true;
				for (int i = 0; parameters.length > i; ++i) {
					if (!StringsUtil.isEqual(parameters[i], o.parameters[i])) {
						res = false;
						break;
					}
				}
			}  // if (needAnswer == o.needAnswer && ...
		}  // if (obj instanceof Test)
		return res;
	}
}
