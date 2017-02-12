import java.util.ArrayList;


public class tacGenerator {

	private int tvarc = 0;
	private int mvarc = 0;
	private SymbolTable st = new SymbolTable();
	
	public tacGenerator(Node root) {
		new Quad("#include <stdio.h>\n\n");
		generate(root);
	}
	
	/*
	 * Generates Three-Address-Code as Quadruples into Quad.getAll() for a given Parse-tree.
	 */
	public void generate(Node c) {
		if(!c.getValue().isTnames()) {
			
			//If we get an Expression, call a different method
			if(c.getValue()==pnames.EXPR) genExpr(c, false);
			
			//Class definition
			else if (c.getValue()==pnames.CLASSDEF) {
				String classname = c.children.get(1).getAttr().toString();
				//if class inheritance was implemented, it would go here
				if(!classname.equals("Main")) System.out.println("Warning: Not compiling class " + classname + "\n\tOnly class 'Main' will be compiled.");
				else for(int qi=0; qi<c.children.size(); qi++) {generate(c.children.get(qi));}
			}
			
			//Feature definition
			else if (c.getValue()==pnames.FEATUREDEF && c.children.size()>1) {
				String featurename = c.children.get(0).getAttr().toString(); 	//Get features name
				Node feature = c.children.get(1);								//Get feature-Node
				
				 //Case: feature = ": type Expr' Featuredef"
				if(feature.children.get(0).getValue()==tnames.COLON) {
					String featuretype = fixType(feature.children.get(1).getAttr().toString());
					Node aexpr = feature.children.get(2);
					
					if(aexpr.children.get(0).getValue()==tnames.ASSIGN) { //Case: Expr' = "<- val;"
						genExpr(aexpr.children.get(1), true);
						new Quad(featuretype + " " + featurename + " = " + aexpr.children.get(1).getAttr() + ";");
					}
					else new Quad(featuretype + " " + featurename + ";"); //Case: Expr' = ";"
					generate(feature.children.get(3)); //generate next feature definition
				}
				//Case: feature = "(Formal) : type {Expr} Featuredef"
				else {
					generate(feature.children.get(1)); //Generate Formal/Argument-List
					Node formal = feature.children.get(1);
					String featuretype = feature.children.get(4).getAttr().toString();
					featuretype = fixType(featuretype);
					if(featurename.equals("main")) new Quad("int main(" + formal.getAttr().toString() + ") {");
					else new Quad(featuretype + " " + featurename + "(" + formal.getAttr().toString() + ") {");
					Quad.incInd(); 						//Set code-Indentation for code
					new Quad(st);  						//Declare list of variables used by this function
					genExpr(feature.children.get(6), !featurename.equals("main"));	//Generate the Expression inside the function-body
					simplify(); 						//Simplify code before clearing symbol-table
					st = new SymbolTable();				//Create new symboltable, since we are outside the functions reach
					if(featurename.equals("main")) new Quad("return 0;");
					else new Quad("return " + feature.children.get(6).getAttr().toString() + ";");
					Quad.decInd();						//Set indentation back
					new Quad("}");
					generate(feature.children.get(9)); //generate next feature definition of this class
				}
			}
			
			//Formal definition / Argument-List
			else if (c.getValue()==pnames.FORMAL) {
				if(c.children.get(0).getValue()==tnames.EPSILON) {replace(c, tnames.FORMALS, "", true);} //Empty. Function takes no args
				else {
					String id, type;
					id = c.children.get(0).getAttr().toString();
					type = c.children.get(2).getAttr().toString();
					type = fixType(type);
					generate(c.children.get(3)); //Next Argument
					//Replace the current Node with a "Formals"-Node that has a list of Formals as its Attribute
					replace(c, tnames.FORMALS, type + " " + id + c.children.get(3).getAttr().toString(), true);
				}
			}
			
			//Formal' definition. Similar to Formal, just with added ','
			else if (c.getValue()==pnames.FORMAL_) {
				if(c.children.get(0).getValue()==tnames.EPSILON) {replace(c, tnames.FORMALS, "", true);}
				else {
					String id, type;
					id = c.children.get(1).getAttr().toString();
					type = c.children.get(3).getAttr().toString();
					type = fixType(type);
					generate(c.children.get(4));
					replace(c, tnames.FORMALS, ", " + type + " " + id + c.children.get(4).getAttr().toString(), true);
				}
			}
			
			//Different kind of Production. generate for all its children.
			else for(int qi=0; qi<c.children.size(); qi++) {generate(c.children.get(qi));}
		}
	}
	
	/*
	 * Generates Three-Address-Code for a given Expression into Quad.getAll().
	 */
	private void genExpr(Node n, boolean needVal) {
		//System.out.println("NEED : " + needVal + " ON " + n);
		//If we weren't given an Expression-Node, quit.
		if(n.getValue()!=pnames.EXPR) {
			System.out.println("Given arg is not expression:" + n + " of parent : " + n.getParent());
			return;
		}
		
		ArrayList<Node> ch = n.children;
		
		/* Variable Declaration:
		 * var : Type				INTO		Type var;
		 * 
		 * var : Type <- Expr		INTO		Type var;
		 * 										var = Expr;
		 */
		if(ch.size() > 1 && ch.get(1).getTValue()==tnames.COLON) {
			Node var, type, expr;
			var=type=expr=null;
			var = ch.get(0);
			expr = ch.get(2);
			type = expr.children.get(0);
			st.put(var.getAttr().toString(), type.getAttr().toString()); //write var into table
			
			//If declared variable is also assigned
			if(expr.children.size()>1){
				type.replace(var); 	//turn variable declaration into normal var assign (aka var : E <- val ===> var <- val
				genExpr(expr, false);		//Generate 3ac for generic assign-operation. ("var = val;")
			}
			replace(n, tnames._PHE, var.getAttr(), needVal); //Replace current Node in parse-tree with placeholder node
			return;
		}
		
		/* Let Statement:
		 * let Expr in Expr		INTO    Expr Expr 
		 */
		else if(ch.size() > 1 && ch.get(0).getTValue()==tnames.LET) {
			genExpr(ch.get(1), false);
			genExpr(ch.get(3), needVal);
			replace(n, tnames._PHE, ch.get(3).getAttr(), needVal);
			return;
		}
		
		/* Braces:
		 * {Expr} INTO Expr
		 */
		else if(ch.size() >= 3 && ch.get(0).getTValue() == tnames.BRACEOPEN) {
			genExpr(ch.get(1), needVal);
			replace(n, tnames._PHE, ch.get(1).getAttr(), needVal);
			return;
		}
		
		/* Expr-List
		 * Expr; Expr;...,  INTO Expr Expr ...
		 */
		else if(ch.size() > 1 && ch.get(1).getTValue() == tnames.SEMI) {
			if(ch.size()>=3) {
				genExpr(ch.get(0), false);
				genExpr(ch.get(2), needVal);
				replace(n, tnames._PHE, ch.get(2).getAttr(), needVal);
			}
			else {
				genExpr(ch.get(0), needVal);
				replace(n, tnames._PHE, ch.get(0).getAttr(), needVal);
			}
			return;
		}
		
		/* While Loop:
		 * while x loop y pool		INTO		m1:		
		 * 										t1 = x;
		 * 										if(!t1) goto m2;
		 * 										y
		 * 										goto m1;
		 * 										m2:
		 */
		if(ch.size() > 1 && ch.get(0).getTValue()==tnames.WHILE) {
			String m1 = newGotVar();
			String m2 = newGotVar();
			new Quad(m1+":");
			genExpr(ch.get(1), true); //gen x
			Node x = ch.get(1);
			new Quad("if(!" + x.getAttr() + ") goto " + m2 + ";");
			genExpr(ch.get(3), false); //gen y
			new Quad("goto " + m1 + ";");
			new Quad(m2+":");
			if(needVal) System.out.println("While has no return value!");
			replace(n, tnames._PHE);
			return;
		}
		
		/* If Statement:
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
			genExpr(ch.get(1), true); //t1=x
			Node t1 = ch.get(1);
			new Quad("if(" + t1.getAttr() + ") goto " + m1 + ";");
			genExpr(ch.get(5), needVal); // z
			new Quad("goto " + m2 + ";");
			new Quad(m1+":");
			genExpr(ch.get(3), needVal); // y
			new Quad(m2+":");
			if(needVal) System.out.println("Function cant return if statement.");
			replace(n,tnames._PHE);
			return;
		}
		
		/* Function Call:
		 * id(E)	INTO	id(E)
		 */
		if(ch.size()>2 && ch.get(0).getTValue()==tnames.ID && ch.get(1).getTValue()==tnames.BRACKETOPEN) {
			if(ch.get(2).getValue()==pnames.EXPR) {
				ch.get(2).mark();
				genExpr(ch.get(2), true); //GenExpr if there are arguments to the method
			}
			if(ch.get(0).getAttr().equals("out_string")) {
				new Quad("printf(");
				new Quad("\"%s\\n\", ");
				new Quad(ch.get(2).getAttr() + ");");
				replace(n, tnames._PHE);
				if(needVal) System.out.println("Function cant return out_string.");
				return;
			}
			else if (ch.get(0).getAttr().equals("out_int")) {
				new Quad("printf(");
				new Quad("\"%d\\n\", ");
				new Quad(ch.get(2).getAttr() + ");");
				if(needVal) System.out.println("Function cant return out_int.");
				replace(n, tnames._PHE);
				return;
			}
			else if (ch.get(0).getAttr().equals("out_nl")) {
				new Quad("printf(");
				new Quad("\"\\n\");");
				if(needVal) System.out.println("Function cant return out_nl.");
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
			if(needVal) {
				Node rep = new Node(tnames.ID, newTmpVar("Int"));
				new Quad(rep.getAttr().toString(), ch.get(0).getAttr()+"(\n   "+ch.get(2).getAttr()+")", "=", "");
				n.replace(rep);
			}
			else {
				new Quad(ch.get(0).getAttr()+"(\n   "+ch.get(2).getAttr()+");");
				replace(n, tnames._PHE);
			}
			return;
		}
		
		/* Marked Comma Statement:
		 * Expr , Expr 		INTO	Expr , Expr
		 * 
		 * A marked comma statement means that the commas are relevant (such as function-arguments)
		 * An unmarked comma statement (such as a series of declarations) will be further down 
		 */
		if(ch.size()>1 && ch.get(1).getTValue() == tnames.COMMA && n.marked) {
			ch.get(0).mark();
			ch.get(2).mark();
			genExpr(ch.get(0), true);
			genExpr(ch.get(2), true);
			replace(n, tnames._PHE, "\n"+ch.get(0).getAttr().toString() + ", \n"+ch.get(2).getAttr().toString(), true);
			return;
		}
		
		//No 'special treatment' required. Just generate all subexpressions in order
		while(!onlyTnames(n)) {
			for(int qi=0; qi<ch.size(); qi++) {
				if(ch.get(qi).getValue()==pnames.EXPR) genExpr(ch.get(qi), false);
			}
		}
		
		//Cases: Constant, ID, etc.
		if(ch.size()==1) {
			if(ch.get(0).getTValue()==tnames.FALSE) replace(n, tnames._PHE, "0", true);
			else if(ch.get(0).getTValue()==tnames.TRUE) replace(n, tnames._PHE, "1", true);
			else if(ch.get(0).getTValue()==tnames.STRINGLITERAL) replace(n, tnames._PHE, "\""+n.children.get(0).getAttr().toString()+"\"", true);
			else replace(n, tnames._PHE, ch.get(0).getAttr(), true);
		}
		//Case: val <- Expr
		else if(ch.get(1).getTValue() == tnames.ASSIGN) {
			replace(n, tnames._PHE);
			if(needVal) System.out.println("Function cant return assign statement.");
			if(ch.get(2).getTValue()==tnames.IN_STRING) {
				new Quad("scanf(");
				new Quad("\"%s\", ");
				new Quad("&" + ch.get(0).getAttr() + ");");
				return;
			}
			else if(ch.get(2).getTValue()==tnames.IN_INT) {
				new Quad("scanf(");
				new Quad("\"%i\", ");
				new Quad("&" + ch.get(0).getAttr() + ");");
				return;
			}
			new Quad(ch.get(0).getAttr().toString(), ch.get(2).getAttr().toString(), "=", "");
			return;
		}
		//Case: Expr, Expr (unmarked)
		else if(ch.get(1).getTValue() == tnames.COMMA) {
			replace(n, tnames._PHE);
			System.out.println("Function cant return comma statement.");
			return;
		}
		//Case: Expr op Expr
		else if(ch.get(1).getTValue().getType()=="BIN_OPERATOR") {
			Node rep = new Node(tnames.ID, newTmpVar("Int"));
			n.replace(rep);
			if(n.children.get(1).getTValue()==tnames.EQ) new Quad(rep.getAttr().toString(), n.children.get(0).getAttr().toString(), "==", n.children.get(2).getAttr().toString());
			else new Quad(rep, n.children.get(0), n.children.get(1), n.children.get(2));
			return;
		}
		//Case: op Expr
		else if(ch.get(0).getTValue().getType()=="SIN_OPERATOR") {
			Node rep = new Node(tnames.ID, newTmpVar("Int"));
			if(ch.get(0).getTValue()==tnames.COMPLEMENT) 
				new Quad(rep.getAttr().toString(), "-", ch.get(1).getAttr().toString(), "");
			else if(ch.get(0).getTValue()==tnames.NOT)
				new Quad(rep.getAttr().toString(), "!", ch.get(1).getAttr().toString(), "");
			else 
				new Quad(rep.getAttr().toString(), ch.get(0).getTValue().getLexeme(), ch.get(1).getAttr().toString(), "");
			n.replace(rep);
			return;
		}
		else {
			System.out.println("Could not genExpr for " + n);
		}
	}
	
	//Replaces a given Node in Parse-tree with a new Node of type name
	public void replace(Node n, tnames name) {
		Node rep = new Node(name);
		n.replace(rep);
	}
	
	//Replaces a given Node in Parse-tree with a new Node of type name + attribute
	public void replace(Node n, tnames name, Object attr) {
		Node rep = new Node(name, attr);
		n.replace(rep);
	}
		
	//Replaces a given Node in Parse-tree with a new Node of type name + attribute
	public void replace(Node n, tnames name, Object attr, boolean needVal) {
		Node rep;
		if(needVal) rep = new Node(name, attr);
		else rep = new Node(name);
		n.replace(rep);
	}
	
	//Returns true if the given Node has only children of type tnames
	public boolean onlyTnames(Node n) {
		for(Node nc : n.children) {
			if(!nc.getValue().isTnames()) return false;
		}
		return true;
	}
	
	//Returns new temporary variable-name
	public String newTmpVar(String type) {
		String newvar = "_t" + (++tvarc);
		st.put(newvar, type);
		return newvar;
	}
	
	//Returns new jump-point name
	public String newGotVar() {
		return "_m" + (++mvarc);
	}
	
	/* Removes all unneeded copy-operations. Aka:
	 * Transforms all
	 * _tx = a op b;
	 * c = _tx;
	 * INTO
	 * c = a op b;
	 */
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
	
	//Prints all Quadruples in order
	public void printAll() {
		for(Quad q : Quad.getAll()) {
			System.out.println(q.fullExpr());
		}
	}
	
	//Keeps standard-types from UnCool Int, Bool and String in mind.
	public String fixType(String feat) {
		if(feat.equals("Int") || feat.equals("Bool")) return "int";
		if(feat.equals("String")) return "char *";
		return feat;
	}
	
}
