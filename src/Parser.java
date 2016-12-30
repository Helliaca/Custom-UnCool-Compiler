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
		} while (matches(Tnames.CLASS));
		// There are no more classes. The input has to end here.
		match(Tnames.EOF);
		return ast;
	}

	// Quit with an error message. t1 and t2 are the expected tokens.
	// t2 may be empty.
	private void quit(Tnames t1, Tnames t2) {
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
	private String match(Tnames t) {
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
	private boolean matches(Tnames t) {
		if (current.name == t) {
			return true;
		}
		return false;
	}

	// Parses a class definition.
	private void klass(AST ast) {
		String klass = null;
		String parent = null; // TODO: initialize ot object?
		Env.ClassInfo ci;
		AST cast;

		match(Tnames.CLASS);
		klass = match(Tnames.TYPEID);

		// Check for the "inherits Type" declaration.
		if (matches(Tnames.INHERITS)) {
			match(Tnames.INHERITS);
			parent = match(Tnames.TYPEID);
		}
		// Add the class definition to the environment.
		ci = env.addClass(klass, parent);
		cast = ast.addClass(ci);

		match(Tnames.BRACEOPEN);
		// Parse vars and methods.
		while (matches(Tnames.ID)) {
			String n = match(Tnames.ID);
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
		type = match(Tnames.TYPEID);
		// Add the variable to the environment.
		vi = env.addVar(klass, name, type);
		vast = ast.addVariable(vi);

		if (matches(Tnames.ASSIGN)) {
			match(Tnames.ASSIGN);
			expr(vast);
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
		type = match(Tnames.TYPEID);
		mi = env.addMethod(klass, name, params, type);
		mast = ast.addMethod(mi);

		// have: name([arg:type, ..]) : Type
		match(Tnames.BRACEOPEN);
		expr(mast);
		match(Tnames.BRACECLOSE);
		match(Tnames.SEMI);

	}

	// Parse the parameters list of a method.
	private void parameters(List<String> params) {
		String type;

		match(Tnames.ID);
		match(Tnames.COLON);
		type = match(Tnames.TYPEID);
		params.add(type);
		if (matches(Tnames.COMMA)) {
			match(Tnames.COMMA);
			parameters(params);
		}
		return; // TODO: remove
	}

	// Parse an expression.
	private void expr(AST ast) {
		match(Tnames.EXPR);
		ast.addExpr();
	}
}
