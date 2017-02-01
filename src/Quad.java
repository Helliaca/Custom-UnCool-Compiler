
public class Quad {
	String arg1;
	String arg2;
	String op;
	String res;
	
	public Quad(String res, String arg1, String op, String arg2) {
		this.res=res;
		this.arg1=arg1;
		this.op=op;
		this.arg2=arg2;
		
		System.out.println("new Quad : " + fullExpr());
	}
	
	public Quad(Node res, Node arg1, Node op, Node arg2) {
		this.res=res.getAttr().toString();
		this.arg1=arg1.getAttr().toString();
		this.op=((tnames)op.getValue()).getLexeme();
		this.arg2=arg2.getAttr().toString();
		
		System.out.println("new Quad : " + fullExpr());
	}
	
	public String fullExpr() {
		return this.res + " = " + this.arg1 + " " + this.op + " " + this.arg2 + ";";
	}
}
