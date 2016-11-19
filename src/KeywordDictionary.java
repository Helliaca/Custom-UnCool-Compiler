import java.util.HashMap;
import java.util.Map;

public class KeywordDictionary {

	Map<String, tnames> dict = new HashMap<String, tnames>();

	public KeywordDictionary() {
		// Write all possible Token-names from tnames into Token-Dictionary
		for (tnames t : tnames.values())
			dict.put(t.getLexeme(), t);
	}

	public Token getToken(String s) {
		// If string corresponds to a lexeme in the dictionary, return
		// corresponding token
		tnames ret = getKey(s);

		if(ret != null)
			return new Token(ret);

		// If string does not correspond to a lexeme but is valid name, return identifier
		if (Character.isLetter(s.charAt(0))) {
			char[] buf = s.toCharArray();
			for (char c : buf) {
				if (! (Character.isLetter(c) || Character.isDigit(c) || c == '_'))
					return new Token(tnames.ERROR, s);
			}
			if (Character.isUpperCase(s.charAt(0)))
				return new Token(tnames.TYPEID, s);
			return new Token(tnames.ID, s); // TODO: Write new id into symbol table
		}

		// If not, check if it is a constant
		else if (Character.isDigit(s.charAt(0))) {
			char[] buf = s.toCharArray();
			int val = 0;
			for (char c : buf) {
				if (Character.isDigit(c))
					val = (val * 10) + Character.getNumericValue(c);
				else
					return new Token(tnames.ERROR, s);
			}
			return new Token(tnames.CONSTANT, s);
		}

		// If none of the above, check if it is a string literal
		else if (s.length() >= 2 && s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"') {
			return new Token(tnames.STRINGLITERAL, s.substring(1, s.length() - 1));
		}

		// If none of the above, check if string is comment
		else if (s.length() >= 2 && s.charAt(0) == '-' && s.charAt(1) == '-') {
			return new Token(tnames.COMMENT, s.substring(2, s.length()));
		}

		// If none of the above, the lexeme is invalid. Return null.
		else {
			return new Token(tnames.ERROR, s);
		}
	}

	public tnames getKey(String s) {
		return dict.get(s);
	}
}
