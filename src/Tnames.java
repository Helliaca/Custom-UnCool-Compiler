public enum Tnames {
	//---- INLCUDES ALL POSSIBLE TOKEN-NAMES WITH THEIR RESPECTIVE LEXEME AND TYPE ----
	
	//Operators:
	ADD("+", "OPERATOR"),
	SUB("-", "OPERATOR"),
	DIV("/", "OPERATOR"),
	MUL("*", "OPERATOR"),
	LT("<", "OPERATOR"),
	LE("<=", "OPERATOR"),
	ASSIGN("<-", "OPERATOR"),
	EQ("=", "OPERATOR"),
	TILDE("~", "OPERATOR"),

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
	EXPR("&", "SYMBOL"),

	//Other:
	CONSTANT("", "CONST"),
	TRUE("true", "CONST"),
	FALSE("false", "CONST"),
	COMMENT("", "COMMENT"),
	STRINGLITERAL("", "LITERAL"),
	ID("", "IDENTIFIER"),
	TYPEID("", "TYPE"),
	EOF("EOF", "END OF FILE"),
	ERROR("", "ERROR");
	
	
	private final String lexeme;
	private final String type;

	Tnames(String lexeme, String type) {
		this.lexeme = lexeme;
		this.type = type;
	}
	
	public String getLexeme() {
		return lexeme;
	}
	
	public String getType() {
		return type;
	}
}	
