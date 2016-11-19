public class Token {

	public tnames name;
	public Object attr;

	public Token(tnames name, Object attr) {
		this.name = name;
		this.attr = attr;
	}

	public Token(tnames name) {
		this.name = name;
		this.attr = null;
	}

	@Override
	public String toString() {
		if (attr != null)
			return "<" + name.getType() + ", " + attr.toString() + ">";
		if (name.getType() != "")
			return "<" + name.getType() + ", " + name.getFullName() + ">";
		return "<" + name.getFullName() + ">";
	}

}
