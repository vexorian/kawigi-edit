package kawigi.ahmed_aly;

import static java.lang.Character.isWhitespace;

public class CPPProcessor {
	private String code = null;
	private String originalCode = null;
	private String defines[][] = null;
	private int definesUsed = 0;
	private boolean deleted[] = null;
	private boolean quoted[] = null;

	public CPPProcessor(String s) {
		originalCode = s;
		defines = new String[10000][5];
		definesUsed = 0;
	}

	public String getCode() {
		if (code == null) {
			if (originalCode == null)
				return "";
			return originalCode;
		}
		return code;
	}

	public boolean validChar(char c) {
		if (c >= 'a' && c <= 'z')
			return true;
		if (c >= 'A' && c <= 'Z')
			return true;
		if (c >= '0' && c <= '9')
			return true;
		if (c == '_')
			return true;
		if (c == '$')
			return true;
		return false;
	}

	public void cleanCode() {
		try {
			code = (new CommentStripper()).stripComments(originalCode)
					.replaceAll("\r\n?", "\n");

			quoted = new boolean[code.length()];
			boolean quote = false;
			char c = ' ';
			for (int i = 0; i < code.length(); i++) {
				quoted[i] = quote;
				if ((code.charAt(i) == '\"' || code.charAt(i) == '\'')
						&& (i == 0 || code.charAt(i - 1) != '\\')) {
					if (quote) {
						if (code.charAt(i) == c)
							quote = false;
					} else {
						quote = true;
						c = code.charAt(i);
						quoted[i] = quote;
					}
				}
			}

			getDefines();
			iterateThroughDefines();

			deleted = new boolean[code.length()];

			for (int i = 0; i < definesUsed; i++) {
				int start = Integer.parseInt(defines[i][2]);
				int end = Integer.parseInt(defines[i][3]);

				if (defines[i][4].equals("seen"))
					continue;
				for (int j = start; j <= end; j++)
					deleted[j] = true;
			}

			StringBuilder build = new StringBuilder();
			for (int i = 0; i < code.length(); i++)
				if (!deleted[i])
					build.append(code.charAt(i));
			code = build.toString();

			code = EmptyLineStripper.stripEmptyLines(code);
		} catch (Exception e) {
			code = null;
		}
	}

	public void getDefines() {
		int i = 0;
		while (true) {
			int j = code.indexOf("#define", i);
			if (j == -1)
				break;
			if (!quoted[j] && (j == 0 || isWhitespace(code.charAt(j - 1)))) {
				int start = j + "#define".length();
				if (start == code.length() || !isWhitespace(code.charAt(start)))
					continue;
				while (start < code.length()
						&& isWhitespace(code.charAt(start)))
					start++;
				if (start == code.length() || !validChar(code.charAt(start)))
					continue;
				int end = start;
				while (end < code.length() && validChar(code.charAt(end)))
					end++;
				defines[definesUsed][0] = code.substring(start, end);
				start = end;
				while (end < code.length()) {
					if (code.charAt(end) == '\n') {
						if (code.charAt(end - 1) != '\\')
							break;
					}
					end++;
				}
				defines[definesUsed][1] = code.substring(start, end).trim();
				defines[definesUsed][2] = "" + j;
				defines[definesUsed][3] = "" + (end - 1);
				defines[definesUsed][4] = "not seen";
				definesUsed++;
			}

			i = j + 1;
		}
		i = 0;
		while (true) {
			int j = code.indexOf("typedef", i);
			if (j == -1)
				break;
			if (!quoted[j] && (j == 0 || !validChar(code.charAt(j - 1)))) {
				int start = j + "typedef".length();
				if (start == code.length() || !isWhitespace(code.charAt(start)))
					continue;
				int k = code.indexOf(';', j);
				if (k == -1)
					continue;
				int a1 = j + "typedef".length();
				while (a1 < k && isWhitespace(code.charAt(a1)))
					a1++;
				if (a1 == k)
					continue;
				int b2 = k - 1;
				while (isWhitespace(code.charAt(b2)))
					b2--;
				if (!validChar(code.charAt(b2)))
					continue;
				int b1 = b2;
				while (validChar(code.charAt(b1)))
					b1--;
				b2++;
				b1++;
				if (!isWhitespace(code.charAt(b1 - 1)))
					continue;
				int a2 = b1 - 1;
				while (isWhitespace(code.charAt(a2)))
					a2--;
				a2++;
				if (a1 >= a2)
					continue;
				defines[definesUsed][0] = code.substring(b1, b2);
				defines[definesUsed][1] = code.substring(a1, a2);
				defines[definesUsed][2] = "" + j;
				defines[definesUsed][3] = "" + k;
				defines[definesUsed][4] = "not seen";
				definesUsed++;
			}
			i = j + 1;
		}
	}

	public void iterateThroughDefines() {
		for (int i = 0; i < definesUsed; i++) {
			int j = 0;
			int start = Integer.parseInt(defines[i][2]);
			int end = Integer.parseInt(defines[i][3]);
			while (true) {
				int k1 = code.indexOf(defines[i][0], j);
				if (k1 == -1)
					break;
				j = k1 + 1;
				if (quoted[k1])
					continue;
				if (k1 >= start && k1 <= end)
					continue;
				int k2 = k1 + defines[i][0].length();
				k1--;
				if (k1 >= 0 && validChar(code.charAt(k1)))
					continue;
				if (k2 < code.length() && validChar(code.charAt(k2)))
					continue;
				defines[i][4] = "seen";

			}
		}
	}

}
