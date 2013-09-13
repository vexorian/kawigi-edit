package kawigi.language;

/**
 *	This enum represents all the data types supported by TopCoder as input or
 *	output types.
 *
 *	Technically, CharacterArray probably won't be used.
 **/
public enum EditorDataType
{
	/**
	 * String type.
	 **/
	String(null, 18),
	/**
	 * int type.
	 **/
	Integer(null, 1),
	/**
	 * double type.
	 **/
	Double(null, 15),
	/**
	 * long (or long long) type.
	 **/
	Long(null, 14),
	/**
	 * boolean type.
	 **/
	Boolean(null, 0),
	/**
	 * String[] or vector<string> type.
	 **/
	StringArray(String, 22),
	/**
	 * int[] or vector<int> type.
	 **/
	IntegerArray(Integer, 20),
	/**
	 * double[] or vector<double> type.
	 **/
	DoubleArray(Double, 21),
	/**
	 * long[] or vector<long long> type.
	 **/
	LongArray(Long, 24);

	/**
	 * If this type is an array type, this is the type of its elements.  If it
	 * isn't an array type, this is null.
	 **/
	private final EditorDataType primitiveType;
	/**
	 * Id of this type in TopCoder Arena.
	 */
	private final int topcoderID;

	/**
	 * Transform TopCoder ID of the type into our enum object.
	 * 
	 * @param tcId	TopCoder id
	 * @return		enum object of the type
	 */
	public static EditorDataType getTypeByTopCoderID(int tcId)
	{
		for (EditorDataType type : EditorDataType.values()) {
			if (type.getID() == tcId)
				return type;
		}
		return null;
	}
	
	/**
	 * Constructs enum values.
	 **/
	private EditorDataType(EditorDataType primType, int tcID)
	{
		primitiveType = primType;
		topcoderID = tcID;
	}

	/**
	 * Returns the type of elements of this type if this is an array type.
	 **/
	public EditorDataType getPrimitiveType()
	{
		return primitiveType;
	}

	/**
	 * Returns TopCoder ID of the type.
	 */
	public int getID()
	{
		return topcoderID;
	}
	
	/**
	 * Returns true if this type represents an array type.
	 **/
	public boolean isArrayType()
	{
		return null != primitiveType;
	}

	/**
	 * Returns true if this type represents an array type of type primType.
	 **/
	public boolean isArrayType(EditorDataType type)
	{
		return type == primitiveType;
	}

	/**
	 * Returns true if type is the same as this type, or if either this type or
	 * the given type represents an array/vector of the other.
	 **/
	public boolean isType(EditorDataType type)
	{
		return this == type || type.isArrayType(this) || isArrayType(type);
	}

	/**
	 * Check if this type is string or array of strings 
	 */
	public boolean isString() {
		return isType(String);
	}
}
