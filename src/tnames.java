public enum tnames implements names {
	//---- INLCUDES ALL POSSIBLE TOKEN-NAMES WITH THEIR RESPECTIVE LEXEME AND TYPE ----
	
	//Operators:
	ADD("+", "OPERATOR"),
	SUB("-", "OPERATOR"),
	DIV("/", "OPERATOR"),
	MUL("*", "OPERATOR"),
	COMPLEMENT("~", "OPERATOR"),
	LT("<", "OPERATOR"),
	LE("<=", "OPERATOR"),
	ASSIGN("<-", "OPERATOR"),
	EQ("=", "OPERATOR"),
	
	//Keywords:
	CLASS("class", "KEYWORD"),
	INHERITS("inherits", "KEYWORD"),
	IN("in", "KEYWORD"),
	IF("if", "KEYWORD"),
	ISVOID("isvoid", "KEYWORD"),
	SELF("self", "KEYWORD"),
	THEN("then", "KEYWORD"),
	FI("fi", "KEYWORD"),
	NOT("not", "KEYWORD"),
	NEW("new", "KEYWORD"),
	SELF_TYPE("SELF_TYPE", "KEYWORD"),
	LET("let", "KEYWORD"),
	ELSE("else", "KEYWORD"),
	WHILE("while", "KEYWORD"),
	LOOP("loop", "KEYWORD"),
	POOL("pool", "KEYWORD"),
	
	//Types/Classes:
	TYPEID("", "TYPE IDENTIFIER"),
	
	//Symbols:
	DOT(".", "SYMBOL"),
	COLON(":", "SYMBOL"),
	SEMI(";", "SYMBOL"),
	COMMA(",", "SYMBOL"),
	BRACKETOPEN("(", "SYMBOL"),
	BRACKETCLOSE(")", "SYMBOL"),
	LISTOPEN("[", "SYMBOL"),
	LISTCLOSE("]", "SYMBOL"),
	BRACEOPEN("{", "SYMBOL"),
	BRACECLOSE("}", "SYMBOL"),
	QUOTATIONMARK("\"", "SYMBOL"),
	VOID("void", "SYMBOL"),
	
	//Other:
	CONSTANT("", "CONST"),
	TRUE("true", "CONST"),
	FALSE("false", "CONST"),
	COMMENT("", "COMMENT"),
	STRINGLITERAL("", "LITERAL"),
	ID("", "IDENTIFIER"),
	EOF("\0", "", "END OF FILE"),
	ERROR("", "ERROR"),
	
	EPSILON("", "EPSILON"),
	TOKENS("", "TOKENLIST");
	
	
	private final String lexeme;
	private final String type;
	private final String fullName;
	
	private tnames(String lexeme, String type, String fullName) {
		this.lexeme = lexeme;
		this.type = type;
		this.fullName = fullName;
	}
	
	private tnames(String lexeme, String type) {
		this.lexeme = lexeme;
		this.type = type;
		this.fullName = this.toString();
	}
	
	public String getLexeme() {
		return lexeme;
	}
	
	public String getType() {
		return type;
	}
	
	public String getFullName() {
		return fullName;
	}
	
	@Override
	public boolean isTnames() {
		return true;
	}
	
	@Override
	public tnames[] First(names[] prod) {
		return new tnames[] {this};
	}

	@Override
	public names[] getProduction(tnames f, tnames fo) {
		return null;
	}
}	
