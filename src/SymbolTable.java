import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

	Map<String, String> dict = new HashMap<String, String>();

	public SymbolTable() {}
	
	public void put(String name, String type) {
		if(type.equals("Int") || type.equals("Bool")) type = "int";
		if(type.equals("String")) type = "string";
		dict.put(name, type);
	}
	
	public String get(String name) {
		return dict.get(name);
	}
	
	public boolean contains(String name) {
		return dict.containsKey(name);
	}

	public Collection<String> values() {
		return dict.keySet();
	}
	
	public String toString() {
		if (dict.size()<=0) return "";
		String ints = "int ";
		String nonints = "";
		for(String s : this.values()) {
			if(this.get(s).equals("int") && ints.length()<=4) ints += s;
			else if(this.get(s).equals("int") && ints.length()>4) ints += ", " + s;
			else nonints += this.get(s) + " " + s + ";\n";
		}
		return ints + ";\n" + nonints;
	}
}
