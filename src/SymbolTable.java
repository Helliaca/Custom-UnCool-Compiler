import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

	Map<String, String> dict = new HashMap<String, String>();

	public SymbolTable() {}
	
	public void put(String name, String type) {
		dict.put(name, type);
	}
	
	public String get(String name) {
		return dict.get(name);
	}
	
	public boolean contains(String name) {
		return dict.containsKey(name);
	}

}
