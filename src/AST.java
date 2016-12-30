import java.util.ArrayList;

public class AST {
	enum NodeType {
		ROOT, CLASS, METHOD, VARIABLE, EXPR
	};

	private NodeType t;
	private Env.ClassInfo ci;
	private Env.VarInfo vi;
	private Env.MethInfo mi;
	private ArrayList<AST> children;
	private final String indent = "\t";

	AST() {
		t = NodeType.ROOT;
		ci = null;
		vi = null;
		mi = null;
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

	AST addExpr() {
		AST c = new AST();
		c.t = NodeType.EXPR;
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