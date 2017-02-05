import java.util.ArrayList;


public class tacGenerator {

	private Node root;
	private int tvarc = 0;
	private int mvarc = 0;
	private SymbolTable st = new SymbolTable();
	
	public tacGenerator(Node root) {
		new Quad("#include <stdio.h>\n\n");
		this.root = root;
		generate(root);
	}
	
	
	public void generate(Node c) {
		if(!c.getValue().isTnames()) {
			
			if(c.getValue()==pnames.EXPR) genExpr(c);
			
			else if (c.getValue()==pnames.CLASSDEF) {
				String classname = c.children.get(1).getAttr().toString();
				//if class inheritance was implemented, it would go here
				if(!classname.equals("Main")) System.out.println("Warning: Not compiling class " + classname + "\n\tOnly class 'Main' will be compiled.");
				else for(int qi=0; qi<c.children.size(); qi++) {generate(c.children.get(qi));}
			}
			
			else if (c.getValue()==pnames.FEATUREDEF && c.children.size()>1) {
				String featurename = c.children.get(0).getAttr().toString();
				Node feature = c.children.get(1);
				
				if(feature.children.get(0).getValue()==tnames.COLON) { // : type Expr' Featuredef
					String featuretype = feature.children.get(1).getAttr().toString();
					featuretype = fixType(featuretype);
					Node aexpr = feature.children.get(2);
					
					if(aexpr.children.get(0).getValue()==tnames.ASSIGN) { // <- val;
						genExpr(aexpr.children.get(1));
						new Quad(featuretype + " " + featurename + " = " + aexpr.children.get(1).getAttr() + ";");
					}
					else new Quad(featuretype + " " + featurename + ";"); //;
					generate(feature.children.get(3)); //gen next featuredef
				}
				else {													// (Formal) : type {Expr} Featuredef
					generate(feature.children.get(1)); //Formal
					Node formal = feature.children.get(1);
					String featuretype = feature.children.get(4).getAttr().toString();
					featuretype = fixType(featuretype);
					if(featurename.equals("main")) new Quad("int main(" + formal.getAttr().toString() + ") {");
					else new Quad(featuretype + " " + featurename + "(" + formal.getAttr().toString() + ") {");
					Quad.incInd();
					new Quad(st);
					genExpr(feature.children.get(6));
					st = new SymbolTable();
					Quad.decInd();
					if(featurename.equals("main")) new Quad("return 0;\n}");
					else new Quad("}");
					generate(feature.children.get(9)); //gen next featuredef
				}
			}
			else if (c.getValue()==pnames.FORMAL) {
				if(c.children.get(0).getValue()==tnames.EPSILON) {c.replace(new Node(tnames.FORMALS, ""));}
				else {
					String id, type;
					id = c.children.get(0).getAttr().toString();
					type = c.children.get(2).getAttr().toString();
					type = fixType(type);
					generate(c.children.get(3));
					c.replace(new Node(tnames.FORMALS, type + " " + id + c.children.get(3).getAttr().toString()));
				}
			}
			else if (c.getValue()==pnames.FORMAL_) {
				if(c.children.get(0).getValue()==tnames.EPSILON) {c.replace(new Node(tnames.FORMALS, ""));}
				else {
					String id, type;
					id = c.children.get(1).getAttr().toString();
					type = c.children.get(3).getAttr().toString();
					type = fixType(type);
					generate(c.children.get(4));
					c.replace(new Node(tnames.FORMALS, ", " + type + " " + id + c.children.get(4).getAttr().toString()));
				}
			}
			else for(int qi=0; qi<c.children.size(); qi++) {generate(c.children.get(qi));}
		}
	}
	
	
	public void genExpr(Node n) {
		//System.out.println("still here" + n);
		if(n.getValue()!=pnames.EXPR) {
			System.out.println("Given arg is not expression:" + n + " of parent : " + n.getParent());
			return;
		}
		
		ArrayList<Node> ch = n.children;
		
		/*
		 * var : Type				INTO		Type var;
		 * 
		 * var : Type <- Expr		INTO		Type var;
		 * 										var = Expr;
		 */
		if(n.children.size() > 1 && n.children.get(1).getValue()==tnames.COLON) { //cases var : E, where E -> type | type <- subexpr
			Node var, type, expr;
			var=type=expr=null;
			var = ch.get(0);
			expr = ch.get(2);
			type = expr.children.get(0);
			st.put(var.getAttr().toString(), type.getAttr().toString());
			
			//If declared variable is also assigned
			if(expr.children.size()>1){
				type.replace(var); 	//turn variable declaration into normal var assign (aka var : E <- val ===> var <- val
				genExpr(expr);
			}
			replace(n, tnames._PHE);
			return;
		}
		
		/*
		 * while x loop y pool		INTO		m1:		
		 * 										t1 = x;
		 * 										if(!t1) goto m2;
		 * 										y
		 * 										goto m1;
		 * 										m2:
		 */
		if(n.children.size() > 1 && ch.get(0).getTValue()==tnames.WHILE) {
			String m1 = newGotVar();
			String m2 = newGotVar();
			new Quad(m1+":");
			genExpr(ch.get(1));
			Node x = ch.get(1);
			new Quad("if(!" + x.getAttr() + ") goto " + m2 + ";");
			genExpr(ch.get(3));
			new Quad("goto " + m1 + ";");
			new Quad(m2+":");
			replace(n, tnames._PHE);
			return;
		}
		
		/*
		 * if x then y else z		INTO		t1 = x;
		 * 										if(t1) goto m1;
		 * 										z
		 * 										goto m2;
		 * 										m1:
		 * 										y
		 * 										m2:
		 */
		if(ch.size() > 1 && ch.get(0).getTValue()==tnames.IF) {
			String m1 = newGotVar();
			String m2 = newGotVar();
			genExpr(ch.get(1)); //t1=x
			Node t1 = ch.get(1);
			new Quad("if(" + t1.getAttr() + ") goto " + m1 + ";");
			genExpr(ch.get(5)); // z
			new Quad("goto " + m2 + ";");
			new Quad(m1+":");
			genExpr(ch.get(3)); // y
			new Quad(m2+":");
			replace(n,tnames._PHE);
			return;
		}
		
		/*
		 * id(E)	INTO	id(E)
		 */
		if(ch.size()>2 && ch.get(0).getTValue()==tnames.ID && ch.get(1).getTValue()==tnames.BRACKETOPEN) {
			if(ch.get(2).getValue()==pnames.EXPR) {
				ch.get(2).mark();
				genExpr(ch.get(2)); //GenExpr if there are arguments to the method
			}
			if(ch.get(0).getAttr().equals("out_string")) {
				new Quad("printf(\"%s\\n\", " + ch.get(2).getAttr() + ");");
				replace(n, tnames._PHE);
				return;
			}
			else if (ch.get(0).getAttr().equals("out_int")) {
				new Quad("printf(\"%d\\n\", " + ch.get(2).getAttr() + ");");
				replace(n, tnames._PHE);
				return;
			}
			else if (ch.get(0).getAttr().equals("in_string")) {
				replace(n, tnames.IN_STRING);
				return;
			}
			else if (ch.get(0).getAttr().equals("in_int")) {
				replace(n, tnames.IN_INT);
				return;
			}
			new Quad(ch.get(0).getAttr()+"("+ch.get(2).getAttr()+");");
			return;
		}
		
		 if(ch.size()>1 && ch.get(1).getTValue() == tnames.COMMA && n.marked) {
			ch.get(0).mark();
			ch.get(2).mark();
			genExpr(ch.get(0));
			genExpr(ch.get(2));
			n.replace(new Node(tnames._PHE, ch.get(0).getAttr().toString() + ", "+ch.get(2).getAttr().toString()));
			return;
		}
		
		while(!onlyTnames(n)) {
			for(int qi=0; qi<n.children.size(); qi++) {
				if(n.children.get(qi).getValue()==pnames.EXPR) genExpr(n.children.get(qi));
			}
		}
		
		if(n.children.size()==1) { //cases const, id and stuff
			if(ch.get(0).getTValue()==tnames.FALSE) n.replace(new Node(tnames._PHE, "0"));
			else if(ch.get(0).getTValue()==tnames.TRUE) n.replace(new Node(tnames._PHE, "1"));
			else if(ch.get(0).getTValue()==tnames.STRINGLITERAL) n.replace(new Node(tnames._PHE, "\""+n.children.get(0).getAttr().toString()+"\""));
			else n.replace(n.children.get(0));
		}
		else if(ch.get(1).getTValue() == tnames.ASSIGN) {
			replace(n, tnames._PHE);
			if(ch.get(2).getTValue()==tnames.IN_STRING) {
				new Quad("scanf(\"%s\", &" + ch.get(0).getAttr() + ");");
				return;
			}
			else if(ch.get(2).getTValue()==tnames.IN_INT) {
				new Quad("scanf(\"%i\", &" + ch.get(0).getAttr() + ");");
				return;
			}
			new Quad(ch.get(0).getAttr().toString(), ch.get(2).getAttr().toString(), "=", "");
		}
		else if(ch.get(1).getTValue() == tnames.COMMA || ch.get(1).getTValue() == tnames.SEMI || ch.get(0).getTValue() == tnames.BRACEOPEN) {
			replace(n, tnames._PHE);
			return;
		}
		else if(((tnames)n.children.get(1).getValue()).getType()=="BIN_OPERATOR") { //cases binary operator
			tnames op = ch.get(1).getTValue();
			Node rep = new Node(tnames.ID, newTmpVar("Int"));
			n.replace(rep);
			new Quad(rep, n.children.get(0), n.children.get(1), n.children.get(2));
		}
		else if(((tnames)n.children.get(0).getValue()).getType()=="SIN_OPERATOR") { //cases binary operator
			if(ch.get(0).getTValue()==tnames.COMPLEMENT) {
				Node rep = new Node(tnames.ID, newTmpVar("Int"));
				n.replace(rep);
				new Quad(rep.getAttr().toString(), " = ", "-"+ch.get(1).getAttr().toString(), "");
				return;
			}
			else if(ch.get(0).getTValue()==tnames.NOT) {
				Node rep = new Node(tnames.ID, newTmpVar("Int"));
				n.replace(rep);
				new Quad(rep.getAttr().toString(), " = ", "!"+ch.get(1).getAttr().toString(), "");
				return;
			}
		}
		else if(ch.get(0).getTValue()==tnames.LET) {
			n.replace(new Node(tnames._PHE));
		}
		else {
			System.out.println("Could not genExpr for " + n);
		}
	}
	
	public void replace(Node n, tnames name) {
		Node rep = new Node(name);
		n.replace(rep);
	}
	
	public boolean onlyTnames(Node n) {
		for(Node nc : n.children) {
			if(!nc.getValue().isTnames()) return false;
		}
		return true;
	}
	
	
	public String newTmpVar(String type) {
		String newvar = "_t" + (++tvarc);
		st.put(newvar, type);
		return newvar;
	}
	
	
	public String newGotVar() {
		return "_m" + (++mvarc);
	}
	
	public void simplify() {
		ArrayList<Quad> list = Quad.getAll();
		for(int i=1; i<list.size(); i++) {
			Quad q = list.get(i);
			if(q.op.equals("=") && q.arg2.equals("") && list.get(i-1).res.equals(q.arg1)) { //Copy statement
				st.dict.remove(list.get(i-1).res);
				list.get(i-1).res = q.res;
				list.remove(i);
				i--;
			}
		}
	}
	
	public void printAll() {
		simplify();
		System.out.println("--");
		for(Quad q : Quad.getAll()) {
			System.out.println(q.fullExpr());
		}
	}
	
	public String fixType(String feat) {
		if(feat.equals("Int") || feat.equals("Bool")) return "int";
		if(feat.equals("String")) return "char *";
		return feat;
	}
}
