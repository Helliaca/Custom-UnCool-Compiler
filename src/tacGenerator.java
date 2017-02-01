
public class tacGenerator {

	private Node root;
	private int tvarc = 0;
	private int mvarc = 0;
	private SymbolTable st = new SymbolTable();
	
	public tacGenerator(Node root) {
		this.root = root;
		generate(root);
	}
	
	
	public void generate(Node n) {
		for(int qi=0; qi<n.children.size(); qi++) {
			if(!n.children.get(qi).getValue().isTnames()) {
				if(n.children.get(qi).getValue()==pnames.EXPR) genExpr(n.children.get(qi));
				else generate(n.children.get(qi));
			}
		}
	}
	
	
	public void genExpr(Node n) {
		if(n.getValue()!=pnames.EXPR) {
			System.out.println("Given arg is not expression:" + n);
			return;
		}
		
		if(n.children.get(1).getValue()==tnames.COLON) { //cases id : Type and id : Type <- E
			//special treatment
			Node var, type, expr;
			var=type=expr=null;
			var = n.children.get(0);
			Node subexpr = n.children.get(2);
			type = subexpr.children.get(0);
			expr = subexpr.children.get(2);
			
			//insert into symboltable
			st.put(((tnames)var.getValue()).getLexeme(), ((tnames)type.getValue()).getLexeme());
			
			type.replace(var); //now we get a expr like var <- expr
			genExpr(subexpr); // generate tac for that
			return;
		}
		
		while(!onlyTnames(n)) {
			for(int qi=0; qi<n.children.size(); qi++) {
				if(n.children.get(qi).getValue()==pnames.EXPR) genExpr(n.children.get(qi));
			}
		}
		
		
		
		if(n.children.size()==1) { //cases const, id and stuff
			n.replace(n.children.get(0));
		}
		else if(((tnames)n.children.get(1).getValue()).getType()=="BIN_OPERATOR") { //cases binary operator
			Node rep = new Node(tnames.ID, newTmpVar());
			n.replace(rep);
			Quad q = new Quad(rep, n.children.get(0), n.children.get(1), n.children.get(2));
		}
	}
	
	
	public boolean onlyTnames(Node n) {
		for(Node nc : n.children) {
			if(!nc.getValue().isTnames()) return false;
		}
		return true;
	}
	
	
	public String newTmpVar() {
		String newvar = "_t" + (++tvarc);
		st.put(newvar, "flex");
		return newvar;
	}
	
	
	public String newGotVar() {
		return "_m" + (++mvarc);
	}
}
