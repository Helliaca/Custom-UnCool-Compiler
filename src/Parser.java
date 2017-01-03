import java.util.ArrayList;
import java.util.List;

class Parser {

	LexScanner lex;
	AST ast;
	Env env;
	Token current;

	Parser(LexScanner lex) {
		this.lex = lex;
		ast = new AST();
		env = new Env();
		current = lex.nextToken();
	}

	// Parse the file given to the Lexer-object. Returns an AST.
	AST parse() {
		// Match class definitions
		do {
			klass(ast);
		} while (matches(Tnames.CLASS));
		// There are no more classes. The input has to end here.
		match(Tnames.EOF);
		return ast;
	}

	// Quit with an error message. t1 and t2 are the expected tokens.
	// t2 may be empty.
	private void quit(Tnames t1, Tnames t2) {
		String out = lex.linenumber() + ":Error: Expected " + t1;

		if (t2 != null) {
			out += " or " + t2;
		}
		out += " got " + current.name;
		System.out.println(out);
		System.exit(-1);
	}

	// If the current token matches t, eat it and advance. If there is
	// a mismatch, the method quits the program with an error message.
	private Object match(Tnames t) {
		Token c = current;
		System.out.println("Ate: " + c.name); // TODO: remove this line
		if (current.name == t) {
			current = lex.nextToken();
			return c.attr;
		} else {
			quit(t, null);
		}
		return null;
	}

	// Returns true if the current token matches t.
	private boolean matches(Tnames t) {
		return current.name == t;
	}

	// Eat the current token.
	private void advance() {
		match(current.name);
	}

	// Parses a class definition.
	private void klass(AST ast) {
		String klass;
		String parent = null;
		Env.ClassInfo ci;
		AST cast;

		match(Tnames.CLASS);
		klass = (String) match(Tnames.TYPEID);

		// Check for the "inherits Type" declaration.
		if (matches(Tnames.INHERITS)) {
			match(Tnames.INHERITS);
			parent = (String) match(Tnames.TYPEID);
		}
		// Add the class definition to the environment.
		ci = env.addClass(klass, parent);
		cast = ast.addClass(ci);

		match(Tnames.BRACEOPEN);
		// Parse vars and methods.
		while (matches(Tnames.ID)) {
			String n = (String) match(Tnames.ID);
			if (matches(Tnames.COLON)) {
				// have: Name :
				var(cast, klass, n);
			} else if (matches(Tnames.BRACKETOPEN)) {
				// have: Name (
				method(cast, klass, n);
			} else {
				quit(Tnames.COLON, Tnames.BRACKETOPEN);
			}
		}
		// Classes end with };
		match(Tnames.BRACECLOSE);
		match(Tnames.SEMI);
	}

	// Parse a variable declaration. name is the name of
	// the variable, klass is the class it's defined in.
	private void var(AST ast, String klass, String name) {
		String type = null;
		Env.VarInfo vi;
		AST vast;

		match(Tnames.COLON);
		type = (String) match(Tnames.TYPEID);
		// Add the variable to the environment.
		vi = env.addVar(klass, name, type);
		vast = ast.addVariable(vi);

		if (matches(Tnames.ASSIGN)) {
			match(Tnames.ASSIGN);
			vast.children.add(expression(0));
		}

		match(Tnames.SEMI);
	}

	// Parse a method declaration. name is the name of
	// the method, klass is the class it's defined in.
	private void method(AST ast, String klass, String name) {
		AST mast;
		Env.MethInfo mi;
		String type;
		List<String> params = new ArrayList<>();

		// method: name([arg : type, ...]) : type { expr };
		match(Tnames.BRACKETOPEN);
		// have: name (
		if (!matches(Tnames.BRACKETCLOSE)) {
			parameters(params);
		}
		// have: name([arg:type, ..]
		match(Tnames.BRACKETCLOSE);
		match(Tnames.COLON);
		// The return type.
		type = (String) match(Tnames.TYPEID);
		mi = env.addMethod(klass, name, params, type);
		mast = ast.addMethod(mi);

		// have: name([arg:type, ..]) : Type
		match(Tnames.BRACEOPEN);
		mast.children.add(expression(0));
		match(Tnames.BRACECLOSE);
		match(Tnames.SEMI);

	}

	// Parse the parameters list of a method.
	private void parameters(List<String> params) {
		String type;

		match(Tnames.ID);
		match(Tnames.COLON);
		type = (String) match(Tnames.TYPEID);
		params.add(type);
		if (matches(Tnames.COMMA)) {
			match(Tnames.COMMA);
			parameters(params);
		}
	}

	// Parses the arguments of method calls.
	private void arguments(AST ast){
		ast.exprs.add(expression(0));
		if (matches(Tnames.BRACKETCLOSE)) {
			match(Tnames.BRACKETCLOSE);
		} else {
			match(Tnames.COMMA);
			arguments(ast);
		}
	}

	// Parses the variable declarations in let expressions.
	private void declarations(AST ast){
		LetInfo li;
		AST expr = null;
		String name = (String) match(Tnames.ID);
		match(Tnames.COLON);
		String type = (String) match(Tnames.TYPEID);

		if (matches(Tnames.ASSIGN)) {
			match(Tnames.ASSIGN);
			expr = expression(0);
		}
		li = new LetInfo(name, type, expr);
		ast.let.add(li);
		if (!matches(Tnames.IN)) {
			match(Tnames.COMMA);
			declarations(ast);
		}
	}

	private AST expression(int bp) {
		AST left = expr();
		while (current.name.getBindingPower() > bp) {
			AST ast = new AST();
			ast.t = AST.NodeType.BINOP;
			ast.token = current.name;
			match(current.name);
			ast.children.add(left); //TODO: add left.clone() instead of left
			ast.children.add(expression(current.name.getBindingPower()));
			left = ast;
		}
		return left;
	}

	private AST expr() {
		AST ast = new AST();

		switch (current.name) {
			case IF:
				ast.t = AST.NodeType.IF;
				match(Tnames.IF);
				ast.exprs.add(expression(0));
				match(Tnames.THEN);
				ast.exprs.add(expression(0));
				match(Tnames.ELSE);
				ast.exprs.add(expression(0));
				match(Tnames.FI);
				break;
			case WHILE:
				ast.t = AST.NodeType.WHILE;
				match(Tnames.WHILE);
				ast.exprs.add(expression(0));
				match(Tnames.LOOP);
				ast.exprs.add(expression(0));
				match(Tnames.POOL);
				break;
			case BRACKETOPEN:
				match(Tnames.BRACKETOPEN);
				ast = expression(0);
				match(Tnames.BRACKETCLOSE);
				break;
			case BRACEOPEN:
				match(Tnames.BRACEOPEN);
				ast.t = AST.NodeType.SEQ;
				ast.exprs.add(expression(0));
				match(Tnames.SEMI);
				while (!matches(Tnames.BRACECLOSE)) {
					ast.exprs.add(expression(0));
					match(Tnames.SEMI);
				}
				match(Tnames.BRACECLOSE);
				break;
			case ID:
				String name = (String) match(Tnames.ID);

				if (matches(Tnames.BRACKETOPEN)) {
					match(Tnames.BRACKETOPEN);
					ast.t = AST.NodeType.METHODCALL;
					ast.str = name;
					if (matches(Tnames.BRACKETCLOSE)) {
						match(Tnames.BRACKETCLOSE);
						return ast;
					}
					arguments(ast);
				} else {
					ast.t = AST.NodeType.ID;
					ast.str = name;
				}
				break;
			// Constants
			case CONSTANT:
				ast.t = AST.NodeType.CONST;
				ast.num = (int) current.attr;
				match(Tnames.CONSTANT);
				break;
			case STRINGLITERAL:
				ast.t = AST.NodeType.STRING;
				ast.str = (String) current.attr;
				match(current.name);
				break;
			case TRUE:
				ast.t = AST.NodeType.BOOL;
				ast.bool = true;
				match(current.name);
				break;
			case FALSE:
				ast.t = AST.NodeType.BOOL;
				ast.bool = false;
				match(current.name);
				break;
			case LET:
				match(Tnames.LET);
				declarations(ast);
				match(Tnames.IN);
				ast.children.add(expression(0));
				break;
			// Unary Operators
			case NOT:
			case ISVOID:
			case TILDE:
				ast.t = AST.NodeType.UNOP;
				ast.token = current.name;
				match(current.name);
				ast.children.add(expression(current.name.getBindingPower()));
				break;
			case NEW:
				ast.t = AST.NodeType.NEW;
				ast.token = current.name;
				match(Tnames.NEW);
				ast.str = (String) match(Tnames.TYPEID);
			default:
				quit(Tnames.EXPR, null);
		}
		return ast;
	}
}
