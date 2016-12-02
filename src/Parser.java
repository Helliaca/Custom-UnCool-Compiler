public class Parser {
	
	private LexScanner lex;
	private Token current;
	private Node root = new Node(pnames.PROGRAM);
	
	
	public Parser(LexScanner lex) {
		this.lex = lex;
		current = lex.nextToken();
		for(pnames p : pnames.values()) p.fixparts(); //Make sure pnames is fixed before running
	}
	
	
	//Parse full tree on Node 'root' and return it.
	public Node parse() {
		parse(pnames.PROGRAM.getProduction(current.name), root);
		return root;
	}
	
	
	//Takes as argument the Node n whose children we are adding and the production prod we need to analyze.
	private boolean parse(names[] prod, Node n) {
		if(prod==null || n==null) return error("Production or root node are null.");
		if(prod.length<=0) return error("Production empty, use tnames.EPSILON for an empty production.");
		
		for(int i=0; i<prod.length; i++) {
			//If prod[i] is a production and not a terminal, parse it
			if(!prod[i].isTnames()) parse(prod[i].getProduction(current.name), new Node(n, prod[i]));
			
			//Production is empty, insert EPSILON into tree at this point
			else if(prod[i]==tnames.EPSILON) new Node(n, prod[i]); 
			
			//Token does not correspond to the terminal expected in the production.
			else if(prod[i]!=current.name) return error("Token expected: " + prod[i] + ", Token got:" + current.name.toString());
			
			//Token does correspond to the expected token, insert it into tree and get next one
			else if(prod[i]==current.name) {
				new Node(n, current.name);
				current = lex.nextToken();
				if(current.name==tnames.EOF) return true;
			}
		}
		return true;
	}
	
	private boolean error(String s) {
		System.out.println("Error: "+s);
		return false;
	}

}
