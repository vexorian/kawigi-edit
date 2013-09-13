package kawigi.ahmed_aly;

import static java.lang.Character.isWhitespace;

public class EmptyLineStripper {
	public static String stripEmptyLines(String code) {
		code = code.trim();
		boolean[] deleted = new boolean[code.length()];
		int last = -1;
		for (int i = 0; i < code.length(); i++)
			if (code.charAt(i) == '\n') {
				boolean del = true;
				for (int j = last + 1; j < i; j++)
					if (!isWhitespace(code.charAt(j))) {
						del = false;
						break;
					}
				if (del)
					for (int j = last + 1; j < i; j++)
						deleted[j] = true;
				last = i;
			}

		StringBuilder temp = new StringBuilder();
		for (int i = 0; i < code.length(); i++)
			if (!deleted[i])
				temp.append(code.charAt(i));

		code = temp.toString();

		StringBuilder build = new StringBuilder();
		build.append(code.charAt(0));
		build.append(code.charAt(1));
		for (int i = 2; i < code.length(); i++) {
			if (code.charAt(i) == '\n' && code.charAt(i - 1) == '\n'
					&& code.charAt(i - 2) == '\n')
				continue;
			build.append(code.charAt(i));
		}
		build.append('\n');
		build.append('\n');
		build.append('\n');
		build.append('\n');
		build.append("//Powered by "+kawigi.KawigiEdit.versionString+"\n");
		build.append("//With unused code cleaner (beta) by ahmed_aly\n");
		return build.toString();
	}
}
