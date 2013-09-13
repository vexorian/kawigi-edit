package kawigi.problem;

/**
 * Represents the combination of a String of source code and a location for the
 * caret to be set.
 **/
public final class Skeleton
{
	/**
	 * String representation of the skeleton code.
	 **/
	private StringBuilder source;
	/**
	 * Index within source that the caret should be in the beginning.
	 **/
	private int caretLocation;

	/**
	 * Creates a new Skeleton object with the given parameters for the code and
	 * caret location. Source object is simply stored, so it should not be changed
	 * later.
	 *
	 * @param source            The source code
	 * @param caretLocation     Position of the caret
	 **/
	public Skeleton(StringBuilder source, int caretLocation)
	{
		this.source = source;
		this.caretLocation = caretLocation;
	}

	/**
	 * Returns the source code for this skeleton. Returned value should not be
	 * changed.
	 *
	 * @return      Source code
	 **/
	public StringBuilder getSource()
	{
		return source;
	}

	/**
	 * Returns the index within the skeleton at which the caret should start out.
	 **/
	public int getCaret()
	{
		return caretLocation;
	}
}
