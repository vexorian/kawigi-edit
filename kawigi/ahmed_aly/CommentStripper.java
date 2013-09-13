package kawigi.ahmed_aly;

public class CommentStripper {

	int idx = 0;
	int ptr = 0;
	char ignore[][] = null;

	public CommentStripper() {
		ignore = new char[0][];
	}

	public String stripComments(String s) {
		idx = 0;
		ptr = 0;
		String s1 = code((" " + s + " ").toCharArray());
		char ac[] = new char[s1.length()];
		int i = 0;
		int j = 0;
		do {
			if (j >= s1.length())
				break;
			if (j < s1.length())
				ac[i++] = s1.charAt(j++);
		} while (true);
		return new String(ac, 0, i);
	}

	private String code(char ac[]) {
		boolean flag = false;
		int i = 0;
		char ac1[] = new char[ac.length];
		boolean aflag[][] = new boolean[ignore.length][];
		for (int j = 0; j < aflag.length; j++)
			aflag[j] = new boolean[ignore[j].length];

		do {
			if (idx >= ac.length)
				break;
			if (ac[idx] == '\'') {
				if (!flag)
					ac1[ptr++] = ac[idx++];
				else
					idx++;
				if (ac[idx] == '\\')
					if (flag)
						idx++;
					else if (idx < ac.length)
						ac1[ptr++] = ac[idx++];
				if (flag)
					idx++;
				else if (idx < ac.length)
					ac1[ptr++] = ac[idx++];
				if (flag)
					idx++;
				else if (idx < ac.length)
					ac1[ptr++] = ac[idx++];
			} else if (ac[idx] == '"') {
				if (flag)
					idx++;
				else
					ac1[ptr++] = ac[idx++];
				while (idx < ac.length && ac[idx] != '"') {
					if (ac[idx] == '\\')
						if (flag)
							idx++;
						else
							ac1[ptr++] = ac[idx++];
					if (flag)
						idx++;
					else
						ac1[ptr++] = ac[idx++];
				}
				if (flag)
					idx++;
				else if (idx < ac.length)
					ac1[ptr++] = ac[idx++];
			} else if (ac[idx] == '/' && ac[idx + 1] == '/')
				while (idx < ac.length && ac[idx] != '\n')
					idx++;
			else if (idx + 1 < ac.length && ac[idx] == '/'
					&& ac[idx + 1] == '*') {
				idx += 4;
				while (idx < ac.length
						&& (ac[idx - 2] != '*' || ac[idx - 1] != '/'))
					idx++;
			} else if (ac[idx] == ' ' || ac[idx] == '\t' || ac[idx] == '\n'
					|| ac[idx] == '\r') {
				for (int k = 0; k < aflag.length; k++) {
					for (int i1 = aflag[k].length - 2; i1 >= 0; i1--) {
						if (aflag[k][i1] && ignore[k][i1 + 1] == ' ')
							aflag[k][i1 + 1] = true;
						aflag[k][i1] = false;
					}

					if (aflag[k][aflag[k].length - 1]) {
						ptr = ptr - aflag[k].length;
						flag = true;
						for (int j1 = 0; j1 < aflag.length; j1++) {
							for (int i2 = 0; i2 < aflag[j1].length; i2++)
								aflag[j1][i2] = false;

						}

						i = -1;
					}
					if (' ' == ignore[k][0])
						aflag[k][0] = true;
				}

				if (flag)
					idx++;
				else
					ac1[ptr++] = ac[idx++];
			} else if (flag) {
				if (ac[idx] == ';' && i == -1)
					flag = false;
				if (ac[idx] == '{')
					if (i == -1)
						i = 1;
					else
						i++;
				if (ac[idx] == '}') {
					if (i == 1)
						flag = false;
					i--;
				}
				idx++;
			} else {
				ac1[ptr] = ac[idx];
				for (int l = 0; l < aflag.length; l++) {
					for (int k1 = aflag[l].length - 2; k1 >= 0; k1--)
						if (aflag[l][k1] && ignore[l][k1 + 1] == ac[idx]) {
							aflag[l][k1] = false;
							aflag[l][k1 + 1] = true;
						}

					if (aflag[l][aflag[l].length - 1]) {
						ptr = ptr - aflag[l].length;
						flag = true;
						for (int l1 = 0; l1 < aflag.length; l1++) {
							for (int j2 = 0; j2 < aflag[l1].length; j2++)
								aflag[l1][j2] = false;

						}

						i = -1;
					}
					if (ac[idx] == ignore[l][0])
						aflag[l][0] = true;
				}

				ptr++;
				idx++;
			}
		} while (true);
		String s = new String(ac1, 0, ptr);
		return s;
	}
}
