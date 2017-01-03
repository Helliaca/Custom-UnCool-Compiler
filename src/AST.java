import javax.xml.soap.Node;
import java.util.ArrayList;

public class AST {
	enum NodeType {
		ROOT, CLASS, METHOD, VARIABLE, EXPR, IF, WHILE, BINOP, UNOP, LET, CONST, ID, STRING, BOOL, METHODCALL, NEW, SEQ
	};

	NodeType t;
	Tnames token;
	private Env.ClassInfo ci;
	private Env.VarInfo vi;
	private Env.MethInfo mi;
	int num;
	boolean bool;
	String str;
	ArrayList<LetInfo> let;
	ArrayList<AST> exprs;
	ArrayList<AST> children;
	private final String indent = "\t";

	AST() {
		t = NodeType.ROOT;
		ci = null;
		vi = null;
		mi = null;
		exprs = new ArrayList<>();
		children = new ArrayList<>();
	}

	AST addClass(Env.ClassInfo ci) {
		AST c = new AST();
		c.t = NodeType.CLASS;
		c.ci = ci;
		children.add(c);
		return c;
	}

	AST addVariable(Env.VarInfo vi) {
		AST c = new AST();
		c.t = NodeType.VARIABLE;
		c.vi = vi;
		children.add(c);
		return c;
	}

	AST addMethod(Env.MethInfo mi) {
		AST c = new AST();
		c.t = NodeType.METHOD;
		c.mi = mi;
		children.add(c);
		return c;
	}

	AST addIF() {
		AST c = new AST();
		c.t = NodeType.IF;
		children.add(c);
		return c;
	}

	public void print() {
		_print(0);
	}

	private void _print(int ind) {
		// indent
		for (int i = 0; i < ind; i++) {
			System.out.print(indent);
		}
		// print the node
		if (t == NodeType.ROOT) {
			System.out.println("Root");
		} else if (t == NodeType.CLASS) {
			System.out.println("Class " + ci.name + " : " + ci.parent.name);
		} else if (t == NodeType.VARIABLE) {
			System.out.println("Variable " + vi.name + " : " + vi.type.name);
		} else if (t == NodeType.METHOD) {
			System.out.print("Method " + mi.name + "(");
			for (Env.ClassInfo ci : mi.params) {
				System.out.print(ci.name + ", ");
			}
			// TODO: don't print a trailing comma.
			System.out.print(") : " + mi.type.name);
		} else if (t == NodeType.EXPR) {
			System.out.println("&");
		} else if(t == NodeType.IF) {
			System.out.println("If");
			System.out.println("Test");
			exprs.get(0)._print(ind + 1);
			System.out.println("Then");
			exprs.get(1)._print(ind + 1);
			System.out.println("Else");
			exprs.get(2)._print(ind + 1);
		} else if (t == NodeType.WHILE) {
			System.out.println("While");
			System.out.println("Test");
			exprs.get(0)._print(ind + 1);
			System.out.println("Loop");
			exprs.get(1)._print(ind + 1);
		} else if (t == NodeType.BINOP) {
			System.out.println("Binop " + token);
			System.out.println("chldren: " + children.size()); // TODO: remove this line
			for (AST ast : children) {
				ast._print(ind + 1);
			}
		} else if (t == NodeType.UNOP) {
			System.out.println("Unop " + token);
			children.get(0)._print(ind + 1);
		} else if (t == NodeType.LET) {
			System.out.println("Let");
			System.out.println("Declarations");
			for (LetInfo li : let) {
				System.out.println(li.name + " : " + li.type);
				if (li.expr != null)
					li.expr._print(ind + 1);
			}
			System.out.println("Body");
			children.get(0)._print(ind + 1);
		} else if (t == NodeType.CONST) {
			System.out.println("Const " + num);
		} else if (t == NodeType.ID) {
			System.out.println("ID " + str);
		} else if (t == NodeType.STRING) {
			System.out.println("String " + str);
		} else if (t == NodeType.BOOL) {
			System.out.println("Bool " + bool);
		} else if (t == NodeType.METHODCALL) {
			System.out.println("Method call " + str);
			for (AST ast : exprs) {
				ast._print(ind + 1);
			}
		} else if (t == NodeType.NEW) {
			System.out.println("new " + str);
		} else if (t == NodeType.SEQ){
				for (AST exp : exprs) {
					exp._print(ind + 1);
				}
		} else {
			System.out.println("ERROR");
		}
		// print a newline
		System.out.println();
		// print the children
		for (AST ast : children) {
			ast._print(ind + 1);
		}
	}
}