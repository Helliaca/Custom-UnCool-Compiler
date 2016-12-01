import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import com.oracle.xmlns.internal.webservices.jaxws_databinding.ExistingAnnotationsType;

public class Env {
	ClassInfo object;

	// Stores information about classes
	private class ClassInfo {
		String name;
		ClassInfo parent;
		List<VarInfo> vars;
		List<MethInfo> methods;
		
		ClassInfo(String name, ClassInfo parent) {
			this.name = name;
			this.parent = parent;
			vars = new ArrayList<>();
			methods = new ArrayList<>();
		}
	}

	// Stores information about variables.
	private class VarInfo {
		String name;
		ClassInfo type;
		
		VarInfo(String name, ClassInfo type) {
			this.name = name;
			this.type = type;
		}
	}

	// Stores information about Methods.
	private class MethInfo {
		String name;
		ClassInfo type;
		List<ClassInfo> params;
		
		MethInfo(String name, List<ClassInfo> params, ClassInfo type){
			this.name = name;
			this.type = type;
			this.params = params;
		}
	}
	
	HashMap<String, ClassInfo> classTable;

	public Env() {
		classTable = new HashMap<>();
		object = new ClassInfo("object", object);
		ClassInfo io = new ClassInfo("IO", object);
		ClassInfo Int = new ClassInfo("Int", object);
		ClassInfo String = new ClassInfo("String", object);
		ClassInfo bool = new ClassInfo("Bool", object);
		// Add predefined classes.
		classTable.put("Object", object);
		classTable.put("IO", io);
		classTable.put("Int", Int);
		classTable.put("String", String);
		classTable.put("Bool", bool);
	}

	// Add a class to the environment.
	public void addClass(String name, String parent) {
		ClassInfo c = classTable.get(name);
		ClassInfo p;
		// Check if there is a class called name.
		if (c != null) {
			System.out.println("Error: " + name + " is already defined.");
			System.exit(-1);
		}
		if (parent != null) {
			// Check if the inherited class exists.
			p = classTable.get(parent);
			if (p == null) {
				System.out.println("Error: " + parent + " is not defined.");
				System.exit(-1);
			}
		} else {
			p = object; // By default, classes inherit from object.
		}
		classTable.put(name, new ClassInfo(name, p));
	}

	// Check if the variable name already exists.
	private void checkVar(ClassInfo klass, String name){
		if(klass == object)
			return;

		for(VarInfo vi : klass.vars){
			if (vi.name.equals(name)){
				System.out.println("Error: Varibale " + name +
						" has already be defined in class " + klass.name);
				System.exit(-1);
			}
		}
		// Check if we inherited a variable called name.
		checkVar(klass.parent, name);
	}

	// Add a variable to the environment.
	public void addVar(String klass, String name, String type) {
		ClassInfo k;
		ClassInfo vType;
		VarInfo v;
		
		// Check if the variables type exists.
		vType = classTable.get(type);
		if(vType == null){
			System.out.println("Error: Class " + vType + " does not exist.");
			System.exit(-1);
		}
		v = new VarInfo(name, vType);
		k = classTable.get(klass);
		// Check if the variable is already defined.
		checkVar(k, name);
		k.vars.add(v);
	}
	
	// Check if the method declaration is valid.
	private void checkMeth(ClassInfo klass, String name, List<ClassInfo> params, ClassInfo type){
		if(klass == object)
			return;
		
		// For all methods in the class ...
		for(MethInfo mi : klass.methods){
			// TODO: redefinition in same class
			if(mi.name.equals(name)){
				// We found a method with the same name.
				// Check the return type.
				if(mi.type != type){
					System.out.println("Error: Attempt to overwrite " + name
							+ " but return type is wrong.");
					System.exit(-1);
				}
				// Check the number of arguments.
				if(mi.params.size() != params.size()){
					System.out.println("Error: Attempt to overwrite " + name
							+ " but number of arguments is wrong.");
					System.exit(-1);
				}
				// Compare parameter types.
				for(int i = 0; i < params.size(); i++){
					if(mi.params.get(i) != params.get(i)){
						System.out.println("Error: Attempt to overwrite " + name
								+ " but parameter types are wrong.");
					}
				}
			}
		}
		checkMeth(klass.parent, name, params, type);
	}
	
	// Add a method the environment.
	public void addMethod(String klass, String name, List<String> params, String type) {
		ClassInfo k;
		ClassInfo retType;
		ClassInfo pType;
		List<ClassInfo> ps = new ArrayList<>();
		MethInfo m;
		
		// Check if the return type exists.
		retType = classTable.get(type);
		if(retType == null) {
			System.out.println("Error: Class " + retType + " does not exist.");
			System.exit(-1);
		}
		// Check if the parameter types exist.
		for (String t : params) {
			pType = classTable.get(t);
			if(pType == null) {
				System.out.println("Error: Class " + pType + " does not exist.");
				System.exit(-1);
			}
			ps.add(pType);
		}
		k = classTable.get(klass);
		// Check if the declaration is valid.
		checkMeth(k, name, ps, retType);
		m = new MethInfo(name, ps, retType);
		
		k.methods.add(m);
	}

}