public enum tnames implements names {
	//---- INLCUDES ALL POSSIBLE TOKEN-NAMES WITH THEIR RESPECTIVE LEXEME AND TYPE ----
	
	//Operators:
	ADD("+", "BIN_OPERATOR"),
	SUB("-", "BIN_OPERATOR"),
	DIV("/", "BIN_OPERATOR"),
	MUL("*", "BIN_OPERATOR"),
	LT("<", "BIN_OPERATOR"),
	LE("<=", "BIN_OPERATOR"),
	ASSIGN("<-", "BIN_OPERATOR"),
	EQ("=", "BIN_OPERATOR"),
	COMMA(",", "BIN_OPERATOR"),
	NOT("not", "SIN_OPERATOR"),
	NEW("new", "SIN_OPERATOR"),
	COMPLEMENT("~", "SIN_OPERATOR"),
	ISVOID("isvoid", "SIN_OPERATOR"),
	
	//Keywords:
	CLASS("class", "KEYWORD"),
	INHERITS("inherits", "KEYWORD"),
	IN("in", "KEYWORD"),
	IF("if", "KEYWORD"),
	THEN("then", "KEYWORD"),
	FI("fi", "KEYWORD"),
	LET("let", "KEYWORD"),
	ELSE("else", "KEYWORD"),
	WHILE("while", "KEYWORD"),
	LOOP("loop", "KEYWORD"),
	POOL("pool", "KEYWORD"),
	INT("INT", "KEYWORD"),
	STR("STR", "KEYWORD"),
	
	//Symbols:
	DOT(".", "SYMBOL"),
	COLON(":", "SYMBOL"),
	SEMI(";", "SYMBOL"),
	BRACKETOPEN("(", "SYMBOL"),
	BRACKETCLOSE(")", "SYMBOL"),
	BRACEOPEN("{", "SYMBOL"),
	BRACECLOSE("}", "SYMBOL"),
	QUOTATIONMARK("\"", "SYMBOL"),
	
	//Other:
	CONSTANT("", "CONSTANT"),
	TRUE("true", "CONSTANT"),
	FALSE("false", "CONSTANT"),
	COMMENT("", "COMMENT"),
	STRINGLITERAL("", "CONSTANT"),
	ID("", "IDENTIFIER"),
	SELF("self", "IDENTIFIER"),
	TYPEID("", "IDENTIFIER"),
	EOF("\0", "", "END OF FILE"),
	ERROR("", "ERROR"),
	
	EPSILON("", "EPSILON"),
	$("$", "END OF LINE"),
	_PHE("", "PHE"),
	IN_STRING("", "IN_STRING"),
	IN_INT("", "IN_INT"),
	FORMALS("", "FORMALS"),
	PRODUCTION("", "P"),
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
	public names[] getProduction(names fo) {
		return null;
	}
}	
