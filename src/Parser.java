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
			klass();
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
	private void match(tnames t) {
		// TODO: remove the next line
		System.out.println("matched: " + t);
		if (current.name == t) {
			current = lex.nextToken();
		} else {
			quit(t, null);
		}
	}

	// Returns true if the current token matches t.
	private boolean matches(tnames t) {
		if (current.name == t) {
			return true;
		}
		return false;
	}

	// Parses a class definition.
	private void klass() {
		String klass = null;
		String parent = null;

		match(tnames.CLASS);
		// Check for a name and save it.
		if (matches(tnames.TYPEID)) {
			klass = (String) current.attr;
			match(tnames.TYPEID);
		} else {
			quit(tnames.TYPEID, null);
		}
		// Check for the "inherits Type" declaration.
		if (matches(tnames.INHERITS)) {
			match(tnames.INHERITS);
			if (matches(tnames.TYPEID)) {
				parent = (String) current.attr;
				match(tnames.TYPEID);
			}
		}
		// Add the class definition to the environment.
		env.addClass(klass, parent);

		match(tnames.BRACEOPEN);
		// Parse vars and methods.
		while (matches(tnames.ID)) {
			String n = (String) current.attr;
			match(tnames.ID);
			if (matches(tnames.COLON)) {
				var(klass, n);
			} else if (matches(tnames.BRACKETOPEN)) {
				method(klass, n);
			} else {
				quit(tnames.COLON, tnames.BRACKETOPEN);
			}
		}
		// Classes end with };
		match(tnames.BRACECLOSE);
		match(tnames.SEMI);
	}
	
	// Parse a variable declaration.
	private void var(String klass, String name) {
		String type = null;

		match(tnames.COLON);
		if (matches(tnames.TYPEID)) {
			type = (String) current.attr;
			match(tnames.TYPEID);
		}
		if (matches(tnames.ASSIGN)) {
			match(tnames.ASSIGN);
			match(tnames.EXPR);
		}
		// Add the variable to the environment.
		env.addVar(klass, name, type);
		match(tnames.SEMI);
	}

	// Parse a method declaration.
	private void method(String klass, String name) {
		String type;
		List<String> params = new ArrayList<>();

		// method: name([arg : type, ...]) : type { expr };
		match(tnames.BRACKETOPEN);
		// have: name (
		while (matches(tnames.ID)) {
			String t = null;

			match(tnames.ID); // We don't care about the name of the params.
			match(tnames.COLON);
	
			if (matches(tnames.TYPEID)) {
				t = (String) current.attr;
				match(tnames.TYPEID);
			}
			// Collect the parameter types in a list.
			params.add(t);
			if(matches(tnames.COMMA)) {
				match(tnames.COMMA);
			} else {
				break;
			}
		}
		// have: name([arg:type, ..]
		match(tnames.BRACKETCLOSE);
		match(tnames.COLON);
		// The return type.
		if (matches(tnames.TYPEID)) {
			type = (String) current.attr;
			match(tnames.TYPEID);
			env.addMethod(klass, name, params, type);
		}
		// have: name([arg:type, ..]) : Type
		match(tnames.BRACEOPEN);
		match(tnames.EXPR);
		match(tnames.BRACECLOSE);
		match(tnames.SEMI);
		
	}
}
