import java.util.ArrayList;

public class Node {
	
	static int idcounter = 1;
	private int id;
	public int depth = 0;

	tnames value=null;
	pnames value_p=null;
	ArrayList<Node> children = new ArrayList<Node>();;
	boolean leaf;
	
	public Node(names val) {
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
		depth = parent.depth+1;
		parent.addChild(this);
	}
	
	public void addChild(Node n) {
		if(!this.leaf) {
			children.add(n);
			n.depth=depth+1;
		}
	}
	
	public String toString() {
		String s = "\n";
		for(int i=0; i<depth; i++) s+="\t"; //Indentation for output
		if(leaf) return s+value.toString();
		s+="Node #" + id + " of type "+ value_p.toString() + " with children: (";
		for(Node f : children) s += f.toString()+", ";
		s+=")";
		return s;
	}
	
}
