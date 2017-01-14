import java.util.ArrayList;

public class Node {
	
	static int idcounter = 1;
	private int id;
	public int depth = 0;
	private Node parent;

	tnames value=null;
	pnames value_p=null;
	ArrayList<Node> children = new ArrayList<Node>();;
	boolean leaf;
	Object attr=null;
	
	public Node(names val) {
		parent = null;
		if(val.isTnames()){
			this.value = (tnames) val;
			leaf=true;
		} else {
			this.value_p = (pnames) val;
			leaf = false;
		}
		id=idcounter++;
	}
	
	public Node(Node parent, names val) {
		this(val);
		this.parent = parent;
		depth = parent.depth+1;
		parent.addChild(this);
	}
	
	public Node(Token tok) {
		this(tok.name);
		this.attr = tok.attr;
	}
	
	public Node(Node parent, Token tok) {
		this(parent, tok.name);
		this.attr = tok.attr;
	}
	
	public names getValue() {
		if(value_p==null) return value;
		return value_p;
	}
	
	public Node addChild(Node n) {
		if(n==null) return null;
		if(!this.leaf) {
			children.add(n);
			n.parent = this;
			n.update();
		}
		return n;
	}
	
	public Node addChild(names val) {
		return this.addChild(new Node(val));
	}
	
	public void update() {
		if(parent!=null) this.depth = parent.depth+1;
		for(Node n : children) n.update();
	}
	
	public String toString() {
		String s = "\n";
		for(int i=0; i<depth; i++) s+="   "; //Indentation for output
		if(leaf && attr!=null) return s+value.toString()+" <"+attr.toString()+">";
		if(leaf) return s+value.toString(); 
		s+="Node #" + id + " of type "+ value_p.toString() + " with children: (";
		for(Node f : children) s += f.toString()+", ";
		s+=")";
		return s;
	}
	

	public Node getParent() {return parent;}
	public void setParent(Node p) {parent = p;}
}
