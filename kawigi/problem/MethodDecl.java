package kawigi.problem;

import kawigi.language.*;


/**
 * Represents the method to be implemented for a problem.
 **/
public final class MethodDecl
{
	/**
	 * The name of the method.
	 **/
	private StringBuilder methodName;
	/**
	 * The return type of the method.
	 **/
	private EditorDataType returnType;
	/**
	 * The names of the parameters of the method.
	 **/
	private StringBuilder[] paramNames;
	/**
	 * The types of the parameters of the method.
	 **/
	private EditorDataType[] paramTypes;

	/**
	 * Constructs a MethodDecl with the given parameters. All given arrays and
	 * objects are saved in the class, so they shouldn't be changed later.
	 **/
	public MethodDecl(StringBuilder methodName, EditorDataType returnType,
	                  EditorDataType[] paramTypes, StringBuilder[] paramNames)
	{
		this.methodName = methodName;
		this.returnType = returnType;
		this.paramTypes = paramTypes;
		this.paramNames = paramNames;
	}

	/**
	 * Constructs a MethodDecl with the given parameters. Array of types
	 * is saved in the class, so it shouldn't be changed later.
	 **/
	public MethodDecl(String methodName, EditorDataType returnType,
	                  EditorDataType[] paramTypes, String[] paramNames)
	{
		this.methodName = new StringBuilder(methodName);
		this.returnType = returnType;
		this.paramTypes = paramTypes;
		this.paramNames = new StringBuilder[paramNames.length];
		for (int i = 0; paramNames.length > i; ++i) {
			this.paramNames[i] = new StringBuilder(paramNames[i]);
		}
	}

	/**
	 * Returns the name of this method. Returned StringBuilder is the one saved
	 * int the class, so it shouldn't be changed.
	 *
	 * @return      Name of the method
	 **/
	public StringBuilder getName()
	{
		return methodName;
	}

	/**
	 * Returns the return type of this method.
	 *
	 * @return      Return type of the method
	 **/
	public EditorDataType getReturnType()
	{
		return returnType;
	}

	/**
	 * Returns the names of the parameters to this method. Returned array and
	 * it's objects are the ones stored in the class, so they shouln't be changed.
	 * Element i of returned array corresponds to element i of the array returned
	 * by getParamTypes.
	 *
	 * @return      Names of the method parameters
	 *
	 * @see         #getParamTypes()
	 **/
	public StringBuilder[] getParamNames()
	{
		return paramNames;
	}

	/**
	 * Returns the types of the parameters to this method. Returned array
	 * is the one stored in the class, so it shouln't be changed.
	 * Element i of this array corresponds to element i of the array returned
	 * by getParamNames.
	 *
	 * @return      Types of the method parameters
	 *
	 * @see         #getParamNames()
	 **/
	public EditorDataType[] getParamTypes()
	{
		return paramTypes;
	}
}
