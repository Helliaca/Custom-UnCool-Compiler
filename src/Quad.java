import java.util.ArrayList;
import java.util.Collections;


public class Quad {
	String arg1="";
	String arg2="";
	String op="";
	String res="";
	static ArrayList<Quad> qs = new ArrayList<Quad>();
	static int c_indentation = 0;
	int inden = 0;
	SymbolTable st = null;
	
	public Quad(String res, String arg1, String op, String arg2) {
		this.res=res;
		this.arg1=arg1;
		this.op=op;
		this.arg2=arg2;
		qs.add(this);
		inden = c_indentation;
		
		//System.out.println("new Quad : " + fullExpr());
	}
	
	public Quad(SymbolTable st) {
		inden = c_indentation;
		this.st = st;
		qs.add(this);
	}
	
	public Quad(Node res, Node arg1, Node op, Node arg2) {
		if(res!=null) this.res=res.getAttr().toString();
		if(arg1!=null) this.arg1=arg1.getAttr().toString();
		if(op!=null) this.op=((tnames)op.getValue()).getLexeme();
		if(arg2!=null) this.arg2=arg2.getAttr().toString();
		//this.arg2 = arg2.toString();
		
		//System.out.println("new Quad : " + fullExpr());
		inden = c_indentation;
		qs.add(this);
	}
	
	public Quad(String res) {
		inden = c_indentation;
		this.res = res;
		qs.add(this);
	}
	
	public String fullExpr() {
		String indent = "";
		for(int i=0; i<inden; i++) indent += "   ";
		if(st!=null) return indent + st.toString();
		if(this.op.equals("=") && this.arg2.equals("")) return indent+this.res+" = "+this.arg1+";";
		if(arg1.equals("") && op.equals("") && arg2.equals("")) return indent+res;
		if (res.equals("")) return indent+this.arg1 + " " + this.op + " " + this.arg2 + ";";
		return indent+this.res + " = " + this.arg1 + " " + this.op + " " + this.arg2 + ";";
	}
	
	static void incInd() {
		c_indentation++;
	}
	
	static void decInd() {
		c_indentation--;
	}
	
	static ArrayList<Quad> getAll() {
		return qs;
	}
}
