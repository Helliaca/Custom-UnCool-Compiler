public enum Tnames {
	//---- INLCUDES ALL POSSIBLE TOKEN-NAMES WITH THEIR RESPECTIVE LEXEME AND TYPE ----
	
	//Operators:
	ADD("+", "OPERATOR", 400),
	SUB("-", "OPERATOR", 400),
	DIV("/", "OPERATOR", 500),
	MUL("*", "OPERATOR", 500),
	LT("<", "OPERATOR", 300),
	LE("<=", "OPERATOR", 300),
	ASSIGN("<-", "OPERATOR", 100),
	EQ("=", "OPERATOR", 300),
	TILDE("~", "OPERATOR", 700),

	//Keywords:
	CLASS("class", "KEYWORD", 0),
	INHERITS("inherits", "KEYWORD", 0),
	IN("in", "KEYWORD", 0),
	IF("if", "KEYWORD", 0),
	ISVOID("isvoid", "KEYWORD", 600),
	THEN("then", "KEYWORD", 0),
	FI("fi", "KEYWORD", 0),
	NOT("not", "KEYWORD", 200),
	NEW("new", "KEYWORD", 0),
	SELF_TYPE("SELF_TYPE", "KEYWORD", 0),
	LET("let", "KEYWORD", 0),
	ELSE("else", "KEYWORD", 0),
	WHILE("while", "KEYWORD", 0),
	LOOP("loop", "Keyword", 0),
	POOL("pool", "Keyword", 0),

	//Symbols:
	DOT(".", "SYMBOL", 800),
	COLON(":", "SYMBOL", 0),
	SEMI(";", "SYMBOL", 0),
	COMMA(",", "SYMBOL", 0),
	BRACKETOPEN("(", "SYMBOL", 0),
	BRACKETCLOSE(")", "SYMBOL", 0),
	BRACEOPEN("{", "SYMBOL", 0),
	BRACECLOSE("}", "SYMBOL", 0),
	QUOTATIONMARK("\"", "SYMBOL", 0),
	EXPR("&", "SYMBOL", 0),

	//Other:
	CONSTANT("", "CONST", 0),
	TRUE("true", "CONST", 0),
	FALSE("false", "CONST", 0),
	COMMENT("", "COMMENT", 0),
	STRINGLITERAL("", "LITERAL", 0),
	ID("", "IDENTIFIER", 0),
	TYPEID("", "TYPE", 0),
	EOF("EOF", "END OF FILE", 0),
	ERROR("", "ERROR", 0);
	
	
	private final String lexeme;
	private final String type;
	private final int bp;

	Tnames(String lexeme, String type, int bp) {
		this.lexeme = lexeme;
		this.type = type;
		this.bp = bp;
	}
	
	public String getLexeme() {
		return lexeme;
	}
	
	public String getType() {
		return type;
	}
	public int getBindingPower() {
		return bp;
	}
}	
