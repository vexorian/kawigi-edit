package kawigi.util;

import java.util.Comparator;


/**
 */
public final class StringsUtil
{
	/**
	 * Line separator in current Operating System.
	 */
	public static final String CRLF = System.getProperty("line.separator");
	/**
	 * Regular expression for line separator matching.
	 */
	public static final String sCRLFregex = "\r?\n";

	private static StringsComparator compar = new StringsComparator();

	private StringsUtil() {}

	public static int getFirstNonSpaceInd(CharSequence val, int startInd)
	{
		int res = startInd;
		while (val.length() > res) {
			char c = val.charAt(res);
			if (!Character.isSpaceChar(c) && !Character.isWhitespace(c))
				break;
			++res;
		}
		return res;
	}

	public static int getFirstNonSpaceInd(CharSequence val)
	{
		return getFirstNonSpaceInd(val, 0);
	}

	public static int getLastNonSpaceInd(CharSequence val, int startInd)
	{
		int res = startInd;
		while (0 <= res) {
			char c = val.charAt(res);
			if (!Character.isSpaceChar(c) && !Character.isWhitespace(c))
				break;
			--res;
		}
		return res;
	}

	public static int getLastNonSpaceInd(CharSequence val)
	{
		return getLastNonSpaceInd(val, val.length() - 1);
	}

	public static void removeAllNextSpace(StringBuilder val, int startInd)
	{
		int endInd = getFirstNonSpaceInd(val, startInd);
		val.delete(startInd, endInd);
	}

	public static void removeAllPrevSpace(StringBuilder val, int endInd)
	{
		int startInd = getLastNonSpaceInd(val, endInd);
		val.delete(startInd + 1, endInd + 1);
	}

	public static void trim(StringBuilder val)
	{
		removeAllNextSpace(val, 0);
		removeAllPrevSpace(val, val.length() - 1);
	}

	public static void addArrayMarks(StringBuilder val)
	{
		val.insert(0, '{').append('}');
	}

	public static void removeArrayMarks(StringBuilder val)
	{
		trim(val);
		if (0 < val.length()) {
		    if ('{' == val.charAt(0)) {
		        val.deleteCharAt(0);
					if ('}' == val.charAt(val.length() - 1))
						val.setLength(val.length() - 1);
		    }
		}
	}

	public static void addStringMarks(StringBuilder val)
	{
		val.insert(0, '"').append('"');
	}

	public static void removeStringMarks(StringBuilder val)
	{
		trim(val);
		if (0 < val.length()) {
			if ('"' == val.charAt(0)) {
				val.deleteCharAt(0);
					if ('"' == val.charAt(val.length() - 1))
						val.setLength(val.length() - 1);
			}
		}
	}

	public static Comparator<CharSequence> getComparator()
	{
		return compar;
	}

	/**
	 * Compares two strings in CharSequences on equality. CharSequences can't
	 * do this check themselves, so I was bound to write this method.
	 *
	 * @param val1      First value to compare
	 * @param val2      Second value to compare
	 * @return          <code>true</code> if two values given are equal,
	 *                  <code>false</code> otherwise
	 */
	public static boolean isEqual(CharSequence val1, CharSequence val2)
	{
		return 0 == compar.compare(val1, val2);
	}

	/**
	 * Converts string value to lowercase. Converting made inplace.
	 *
	 * @param val       Value to be converted and place to make result
	 */
	public static void toLower(StringBuilder val)
	{
		for (int i = 0; val.length() > i; ++i) {
			val.setCharAt(i, Character.toLowerCase(val.charAt(i)));
		}
	}

	/**
	 * Replaces the value in StringBuilder with another CharSeqence.
	 *
	 * @param val   StringBuilder for the result to be placed
	 * @param seq   Value that must be placed into <code>val</code>
	 */
	public static void reset(StringBuilder val, CharSequence seq)
	{
		val.setLength(0);
		val.append(seq);
	}

	/**
	 * Replaces the part of string with another string without unnecessary
	 * deletions and insertions. Value is changed inplace.
	 *
	 * @param val       Value to be changed
	 * @param start     Start index of replacement object
	 * @param end       End index (one char behind end) of replacement object
	 * @param seq       Sequence to be inserted instead
	 */
	public static void replace(StringBuilder val, int start, int end, CharSequence seq)
	{
		int valPos = start, seqPos = 0;
		// First we char-by-char replace what we can
		for (; valPos != end && seqPos != seq.length(); ++valPos, ++seqPos)
			val.setCharAt(valPos, seq.charAt(seqPos));

		// Then we do deletion and insertion of the rest
		if (valPos < end)
			val.delete(valPos, end);
		else if (seqPos < seq.length())
			val.insert(end, seq, seqPos, seq.length());
	}

	/**
	 * Finds first occurence of character in CharSequence starting from index start.
	 *
	 * @param val       Sequence to search for character
	 * @param c         Character to search for
	 * @param start     Starting index in sequence to search
	 * @return          Index in sequence where character is found or -1
	 *                  if it wasn't found
	 */
	public static int indexOf(CharSequence val, char c, int start)
	{
		int res = -1;
		for (int i = start; val.length() > i; ++i) {
			if (val.charAt(i) == c) {
				res = i;
				break;
			}
		}
		return res;
	}

	/**
	 * Finds first occurence of character in CharSequence starting from the beginning.
	 *
	 * @param val       Sequence to search for character
	 * @param c         Character to search for
	 * @return          Index in sequence where character is found or -1
	 *                  if it wasn't found
	 */
	public static int indexOf(CharSequence val, char c)
	{
		return indexOf(val, c, 0);
	}

	/**
	 * Finds last occurence of character in CharSequence starting from index start.
	 *
	 * @param val       Sequence to search for character
	 * @param c         Character to search for
	 * @param start     Starting index in sequence to search
	 * @return          Index in sequence where character is found or -1
	 *                  if it wasn't found
	 */
	public static int lastIndexOf(CharSequence val, char c, int start)
	{
		int res = -1;
		int i = start;
		if (val.length() <= i)
			i = val.length() - 1;
		for (; 0 <= i; --i) {
			if (val.charAt(i) == c) {
				res = i;
				break;
			}
		}
		return res;
	}

	/**
	 * Finds last occurence of character in CharSequence starting from the beginning.
	 *
	 * @param val       Sequence to search for character
	 * @param c         Character to search for
	 * @return          Index in sequence where character is found or -1
	 *                  if it wasn't found
	 */
	public static int lastIndexOf(CharSequence val, char c)
	{
		return lastIndexOf(val, c, val.length() - 1);
	}

	/**
	 * Checks if some string ('needle') stands at particular point ('startInd') in
	 * another string ('hay').
	 *
	 * @param hay           String to look for 'needle' in
	 * @param needle        String to check for appearance in 'hay'
	 * @param startInd      Index in 'hay' at which 'needle' should stand
	 * @return              <code>true</code> if 'needle' stands in 'hay' at position
	 *                      'startInd'. <code>false</code> otherwise.
	 */
	public static boolean isStringAt(CharSequence hay, CharSequence needle, int startInd)
	{
		boolean res = false;
		if (hay.length() >= startInd + needle.length()) {
			res = true;
			for (int i = 0, j = startInd; needle.length() > i; ++i, ++j) {
				if (hay.charAt(j) != needle.charAt(i)) {
					res = false;
					break;
				}
			}
		}
		return res;
	}

	/**
	 * Appends some excerpt from character sequence to StringBuilder trimming all
	 * white space from excerpt beforehand.
	 *
	 * @param val       Value to be modified
	 * @param fromVal   Sequence to make excerpt from
	 * @param start     Starting index of excerpt from sequence
	 * @param end       Ending index of excerpt from sequence (one character after end)
	 */
	public static void appendTrimmed(StringBuilder val, CharSequence fromVal, int start, int end)
	{
		val.append(fromVal, getFirstNonSpaceInd(fromVal, start),
						    getLastNonSpaceInd(fromVal, end - 1) + 1);
	}
}
