public class Token {

	Tnames name;
	Object attr;

	public Token(Tnames name, Object attr) {
		this.name = name;
		this.attr = attr;
	}

	public Token(Tnames name) {
		this.name = name;
		this.attr = null;
	}

	@Override
	public String toString() {
		if (attr != null)
			return "<" + name.getType() + ", " + attr.toString() + ">";
		else
			return "<" + name.getLexeme() + ">";
	}

}
