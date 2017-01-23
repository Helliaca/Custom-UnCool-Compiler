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
			
			//if prod[i] is an expression:
			if(!prod[i].isTnames() && prod[i]==pnames.EXPR) {
				if(++i>prod.length) return error("A tnames token must follow EXPR in pnames production.");
				ArrayList<Token> expr = new ArrayList<Token>();
				
				//Keep scanning until we hit the end of the expression, any brackets inside the expression must be counted with counters
				int counterBK = 0; //counts brackets
				int counterBC = 0; //counts braces
				//run nextToken until counterBK = counterBC = 0 and current is the symbol that comes after the expression
				while(current.name!=prod[i] || counterBK>0 || counterBC>0) {
					if(current.name==tnames.BRACEOPEN) counterBC++;
					if(current.name==tnames.BRACECLOSE) counterBC--;
					if(current.name==tnames.BRACKETOPEN) counterBK++;
					if(current.name==tnames.BRACKETCLOSE) counterBK--;
					if(current.name==tnames.EOF) return error("Expression has no proper ending.");
					expr.add(current);
					nextToken();
				}
				expr.add(new Token(tnames.$)); //The entire expression is stored in expr, add $ to the end and parse it
				nextToken();
				Node ch = parseExpr( expr.toArray(new Token[expr.size()]) );
				if(ch==null) return error("Parse unsuccesful.");
				n.addChild( ch );		//Add the expression to the AST
				new Node(n, prod[i]);   //Add the ending symbol to the AST as well
			}
			
			//If prod[i] is a production that is not an expression and not a terminal, parse it
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
	
	//Takes an array of tokens that make up an expression. Returns the AST of given expression
	private Node parseExpr(Token[] input) {
		Stack<Node> symbols = new Stack<Node>();
		Stack<tnames> stack = new Stack<tnames>();
		stack.push(tnames.$); //Push ending-symbol to stack
		
		for(int i = 0; i<input.length;) { //Go over the input
			
			int u = PrecTable.tableEntry(stack.peek(), input[i].name); //Get precedence of stack.peek() and current input token
			
			//precedence is accept (see PrecTable). Accept the input if the expression is not empty
			if(u == -1) {
				if(symbols.size()<=0) {
					error("Expression is empty.");
					return null;
				}
				return symbols.get(0);
			}
			
			//precedence is less or equal, shift token onto stack and symbols and increase input-pointer
			else if(u == 1 || u == 0) {
				stack.push(input[i].name);
				symbols.push(new Node(input[i]));
				i++;
			}
			
			//precedence is higher, reduce
			else if(u == 2) {
				if(!reduce(stack, symbols, stack.peek())) return null;
			}
			
			//precedence does not exist, output error and return
			else {
				error("Could not parse " + stack.peek() + " with " + input[i]);
				String s = "Stack: ";
				for(Node tn : symbols) s+=" - " + tn.getValue().toString();
				error(s);
				return null;
			}
		}
		error("Parsing ended abruptly");
		return null;
	}
	
	//Takes stack and symbols from parseExpr and reduces the production depending on r.
	private boolean reduce(Stack<tnames> stack, Stack<Node> symbols, tnames r) {
		
		//Expression is a binary operation. Cases: E+E, E-E, E*E, E/E, E<E, E<=E, E=E, ID<-E
		if(r.getType()=="BIN_OPERATOR") {
			reduce(stack, symbols, 3); //Parent 3 symbols to an EXPR-Node
		}
		
		//Expression is an identifier, constant or string-literal. Cases: ID, string, integer, true, false
		else if(r.getType()=="IDENTIFIER" || r.getType()=="CONSTANT") {
			reduce(stack, symbols, 1); //Take one symbol off symbols and parent it to an EXPR-Node
		}
		
		//Expression is a single operation. //Cases: new E, not E, isvoid E, ~E
		else if(r.getType()=="SIN_OPERATOR") {
			reduce(stack, symbols, 2);
		}
		
		//Expression is an if-statement. Case: if E then E else E fi
		else if(r==tnames.FI) {
			reduce(stack, symbols, 7); //parent seven symbols to one EXPR-Node
		}
		
		//Expression is a colon-statement. Case: TypeID : E
		else if(r==tnames.COLON) {
			reduce(stack, symbols, 3);
		}
		
		//Expression is a let-statement. Case: let expr in expr
		else if(r==tnames.IN) {
			reduce(stack, symbols, 4);
		}
		
		//Expression is a while-statement. Case: while E loop E pool
		else if(r==tnames.POOL) {
			reduce(stack, symbols, 5);
		}
		
		//Expression is a list of expression. Case: E; E; E; ...
		else if(r==tnames.SEMI) {
			//If the semicolon does not have an expression following it, treat it as a single operation. 'E;' => 'E'
			if(symbols.peek().getValue()==tnames.SEMI) {
				reduce(stack, symbols, 2);
			}
			//If the semicolon does have an expression following it, treat it as a binary operation. 'E;E' => 'E'
			else if(symbols.peek().getValue()==pnames.EXPR) {
				reduce(stack, symbols, 3);
			}
			else error("An expression must come before a semicolon.");
		}
		
		//Expression is another expression in brackets or braces. //Cases: {E}, (E), ID(E), ID.ID(E)
		else if(r==tnames.BRACKETCLOSE || r==tnames.BRACECLOSE) {
			ArrayList<Node> tmp = new ArrayList<Node>(); //temporary storage
			Node p = new Node(pnames.EXPR);
			tmp.add(symbols.pop());	//pop right bracket/brace
			stack.pop();
			if(symbols.peek().getValue()==pnames.EXPR) tmp.add(symbols.pop()); 	//pop expression inbetween if there is one
			tmp.add(symbols.pop());	//pop left bracket
			stack.pop();
			
			//Cases ID(expr) and ID.ID(expr)
			if(r==tnames.BRACKETCLOSE && symbols.size()>0 && symbols.peek().getValue()==tnames.ID) {
				tmp.add(symbols.pop()); //pop ID
				stack.pop();
			}
			//Case ID.ID(expr)
			if(symbols.size()>0 && symbols.peek().getValue()==tnames.DOT) {
				tmp.add(symbols.pop()); //pop DOT
				stack.pop();
				tmp.add(symbols.pop()); //pop ID
				stack.pop();
			}
			
			for(int q=tmp.size()-1; q>=0; q--) p.addChild(tmp.get(q)); //Add tokens to EXPR-Node
			symbols.push(p); //Push that node onto symbols
		}
		
		//Expression not found
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













