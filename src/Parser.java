import java.util.ArrayList;
import java.util.List;

public class Parser {

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
	public AST parse() {
		// Match class definitions
		do {
			klass(ast);
		} while (matches(tnames.CLASS));
		// There are no more classes. The input has to end here.
		match(tnames.EOF);
		return ast;
	}

	// Quit with an error message. t1 and t2 are the expected tokens.
	// t2 may be empty.
	private void quit(tnames t1, tnames t2) {
		String out = "Error: Expected " + t1;

		if (t2 != null) {
			out += " or " + t2;
		}
		out += " got " + current.name;
		System.out.println(out);
		System.exit(-1);
	}

	// If the current token matches t, eat it and advance. If there is
	// a mismatch, the method quits the program with an error message.
	private String match(tnames t) {
		Token c = current;
		if (current.name == t) {
			current = lex.nextToken();
			return (String) c.attr;
		} else {
			quit(t, null);
		}
		return null;
	}

	// Returns true if the current token matches t.
	private boolean matches(tnames t) {
		if (current.name == t) {
			return true;
		}
		return false;
	}

	// Parses a class definition.
	private void klass(AST ast) {
		String klass = null;
		String parent = null;
		Env.ClassInfo ci;
		AST cast;

		match(tnames.CLASS);
		klass = match(tnames.TYPEID);

		// Check for the "inherits Type" declaration.
		if (matches(tnames.INHERITS)) {
			match(tnames.INHERITS);
			parent = match(tnames.TYPEID);
		}
		// Add the class definition to the environment.
		ci = env.addClass(klass, parent);
		cast = ast.addClass(ci);

		match(tnames.BRACEOPEN);
		// Parse vars and methods.
		while (matches(tnames.ID)) {
			String n = match(tnames.ID);
			if (matches(tnames.COLON)) {
				// have: Name :
				var(cast, klass, n);
			} else if (matches(tnames.BRACKETOPEN)) {
				// have: Name (
				method(cast, klass, n);
			} else {
				quit(tnames.COLON, tnames.BRACKETOPEN);
			}
		}
		// Classes end with };
		match(tnames.BRACECLOSE);
		match(tnames.SEMI);
	}

	// Parse a variable declaration. name is the name of
	// the variable, klass is the class it's defined in.
	private void var(AST ast, String klass, String name) {
		String type = null;
		Env.VarInfo vi;
		AST vast;

		match(tnames.COLON);
		type = match(tnames.TYPEID);
		// Add the variable to the environment.
		vi = env.addVar(klass, name, type);
		vast = ast.addVariable(vi);

		if (matches(tnames.ASSIGN)) {
			match(tnames.ASSIGN);
			expr(vast);
		}

		match(tnames.SEMI);
	}

	// Parse a method declaration. name is the name of
	// the method, klass is the class it's defined in.
	private void method(AST ast, String klass, String name) {
		AST mast;
		Env.MethInfo mi;
		String type;
		List<String> params = new ArrayList<>();

		// method: name([arg : type, ...]) : type { expr };
		match(tnames.BRACKETOPEN);
		// have: name (
		if (!matches(tnames.BRACKETCLOSE)) {
			parameters(params);
		}
		// have: name([arg:type, ..]
		match(tnames.BRACKETCLOSE);
		match(tnames.COLON);
		// The return type.
		type = match(tnames.TYPEID);
		mi = env.addMethod(klass, name, params, type);
		mast = ast.addMethod(mi);

		// have: name([arg:type, ..]) : Type
		match(tnames.BRACEOPEN);
		expr(mast);
		match(tnames.BRACECLOSE);
		match(tnames.SEMI);

	}

	// Parse the parameters list of a method.
	private void parameters(List<String> params) {
		String type;

		match(tnames.ID);
		match(tnames.COLON);
		type = match(tnames.TYPEID);
		params.add(type);
		if (matches(tnames.COMMA)) {
			match(tnames.COMMA);
			parameters(params);
		}
		return;
	}

	// Parse an expression.
	private void expr(AST ast) {
		match(tnames.EXPR);
		ast.addExpr();
	}
}
