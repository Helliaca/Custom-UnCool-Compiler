import java.util.ArrayList;
import java.util.Stack;

public class Parser {
	
	private LexScanner lex;
	private Token current;
	private Token next;
	private Node root = new Node(pnames.PROGRAM);
	
	
	public Parser(LexScanner lex) {
		this.lex = lex;
		nextToken();
		nextToken();
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
			
			//In case of an expression:
			if(!prod[i].isTnames() && prod[i]==pnames.EXPR) {
				if(++i>prod.length) return error("A tnames token must follow EXPR in pnames production.");
				ArrayList<Token> expr = new ArrayList<Token>();
				
				//Keep scanning until we hit the end of the expression, any brackets inside the expression must be counted with counters
				int counterBK = 0; //counts brackets
				int counterBC = 0; //counts braces
				while(current.name!=prod[i] || counterBK>0 || counterBC>0) {
					if(current.name==tnames.BRACEOPEN) counterBC++;
					if(current.name==tnames.BRACECLOSE) counterBC--;
					if(current.name==tnames.BRACKETOPEN) counterBK++;
					if(current.name==tnames.BRACKETCLOSE) counterBK--;
					if(current.name==tnames.EOF) return error("Expression has no proper ending.");
					expr.add(current);
					nextToken();
				}
				expr.add(new Token(tnames.$)); //The expression is stored in expr, add $ to the end and parse it
				nextToken();
				Node ch = parseExpr( expr.toArray(new Token[expr.size()]) );
				if(ch==null) return error("Parse unsuccesful.");
				n.addChild( ch );
				new Node(n, prod[i]);  //Add the expression-AST to the complete AST
			}
			
			//If prod[i] is a production and not a terminal, parse it
			else if(!prod[i].isTnames()) parse(prod[i].getProduction(current.name), new Node(n, prod[i]));
			
			//Production is empty, insert EPSILON into tree at this point
			else if(prod[i]==tnames.EPSILON) new Node(n, prod[i]); 
			
			//Token does not correspond to the terminal expected in the production.
			else if(prod[i]!=current.name) return error("Token expected: " + prod[i] + ", Token got:" + current);
			
			//Token does correspond to the expected token, insert it into tree and get next one
			else if(prod[i]==current.name) {
				new Node(n, current);
				nextToken();
				if(current.name==tnames.EOF) return true;
			}
		}
		return true;
	}
	
	private boolean error(String s) {
		System.out.println("Error(Line " + lex.linenumber() + "): "+s);
		return false;
	}
	
	private void nextToken() {
		current = next;
		next = lex.nextToken();
	}
	
	private Node parseExpr(Token[] input) {
		Stack<Node> symbols = new Stack<Node>();
		Stack<tnames> stack = new Stack<tnames>();
		stack.push(tnames.$);		
		
		for(int i = 0; i<input.length;) {
			
			int u = PrecTable.tableEntry(stack.peek(), input[i].name); //Get precedence
			if(u == -1) { //Accept if not empty
				if(symbols.size()<=0) {
					error("Expression is empty.");
					return null;
				}
				return symbols.get(0);
			}
			else if(u == 1 || u == 0) { //Shift
				stack.push(input[i].name);
				symbols.push(new Node(input[i]));
				i++;
			}
			else if(u == 2) { //Reduce
				if(!reduce(stack, symbols, stack.peek())) return null;
			}
			else { //Error
				error("Could not parse " + stack.peek() + " with " + input[i]);
				return null;
			}
		}
		error("Parsing ended abruptly");
		return null;
	}
	
	private boolean reduce(Stack<tnames> stack, Stack<Node> symbols, tnames r) {
		if(r.getType()=="BIN_OPERATOR") { //Case: +, -, *, /, <, <=, =, <-
			reduce(stack, symbols, 3); //Turn 3 symbols into one expression
		}
		else if(r.getType()=="IDENTIFIER" || r.getType()=="CONSTANT") { //ID, string, integer, true, false
			reduce(stack, symbols, 1);
		}
		else if(r.getType()=="SIN_OPERATOR") { //Case: new, not, isvoid, ~
			reduce(stack, symbols, 2);
		}
		else if(r==tnames.FI) { //Case: if expr then expr else expr fi
			reduce(stack, symbols, 7);
		}
		else if(r==tnames.POOL) { //Case: while expr loop expr pool
			reduce(stack, symbols, 5);
		}
		else if(r==tnames.SEMI) { //Case: expr;
			if(symbols.peek().getValue()==tnames.SEMI) {
				reduce(stack, symbols, 2); //if there is no other expr after the semicolon reduce 2 
			}
			else if(symbols.peek().getValue()==pnames.EXPR) {
				reduce(stack, symbols, 3); //Otherwise reduce 3, so that 'expr; expr' -> expr
			}
			else error("An expression must come before a semicolon.");
		}
		else if(r==tnames.BRACKETCLOSE || r==tnames.BRACECLOSE) { //Case: {expr}, (expr), ID(expr), ID.ID(expr)
			ArrayList<Node> tmp = new ArrayList<Node>();
			Node p = new Node(pnames.EXPR);
			tmp.add(symbols.pop());	//pop right bracket
			stack.pop();
			if(symbols.peek().getValue()==pnames.EXPR) tmp.add(symbols.pop()); 	//pop expression if there is one
			tmp.add(symbols.pop());	//pop left bracket
			stack.pop();
			//Cases ID(expr) and ID.ID(expr)
			if(r==tnames.BRACKETCLOSE && symbols.size()>0 && symbols.peek().getValue()==tnames.ID) {
				tmp.add(symbols.pop()); //pop ID
				stack.pop();
			}//Case ID.ID(expr)
			if(symbols.size()>0 && symbols.peek().getValue()==tnames.DOT) {
				tmp.add(symbols.pop()); //pop DOT
				stack.pop();
				tmp.add(symbols.pop()); //pop ID
				stack.pop();
			}
			for(int q=tmp.size()-1; q>=0; q--) p.addChild(tmp.get(q)); //unreverse
			symbols.push(p);
		}
		else{
			error("Could not reduce " + r);
			return false;
		}
		return true;
	}
	
	private void reduce(Stack<tnames> stack, Stack<Node> symbols, int r) {
		ArrayList<Node> tmp = new ArrayList<Node>();
		Node p = new Node(pnames.EXPR);
		for(int i=0; i<r; i++) {
			if(symbols.peek().getValue().isTnames()) stack.pop(); //pop stack aswell if it is a terminal
			tmp.add(symbols.pop());
		}
		for(int q=tmp.size()-1; q>=0; q--) p.addChild(tmp.get(q));
		symbols.push(p);
	}
}













