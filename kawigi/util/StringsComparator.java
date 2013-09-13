package kawigi.util;

import java.util.Comparator;


/**
 */
public final class StringsComparator implements Comparator<CharSequence> {
	StringsComparator()
	{}

	public int compare(CharSequence o1, CharSequence o2)
	{
		int res = 0;
		int len1 = o1.length();
		int len2 = o2.length();
		for (int i = 0; len1 > i && len2 > i; ++i) {
			char c1 = o1.charAt(i);
			char c2 = o2.charAt(i);
			if (c1 != c2) {
				res = c1 - c2;
				break;
			}
		}
		if (0 == res)
			res = len1 - len2;
		return res;
	}

	public boolean equals(Object obj)
	{
		return obj instanceof StringsComparator;
	}
}
