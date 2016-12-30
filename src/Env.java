import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

class Env {
	private ClassInfo object;

	// Stores information about classes
	class ClassInfo {
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
	class VarInfo {
		String name;
		ClassInfo type;

		VarInfo(String name, ClassInfo type) {
			this.name = name;
			this.type = type;
		}
	}

	// Stores information about methods.
	class MethInfo {
		String name;
		ClassInfo type;
		List<ClassInfo> params;

		MethInfo(String name, List<ClassInfo> params, ClassInfo type) {
			this.name = name;
			this.type = type;
			this.params = params;
		}
	}

	private HashMap<String, ClassInfo> classTable;

	Env() {
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

	// Prints msg and quits the program.
	private void fail(String msg) {
		System.out.println("Error: " + msg);
		System.exit(-1);
	}

	// Fail if the type doesn't exist, or return the corresponding
	// ClassInfo object.
	private ClassInfo verifyType(String type) {
		ClassInfo ci = classTable.get(type);
		if (ci == null) {
			fail("Class " + type + " is undefined.");
		}
		return ci;
	}

	// Add a class to the environment.
	Env.ClassInfo addClass(String name, String parent) {
		ClassInfo ci;
		ClassInfo p;
		ClassInfo c = classTable.get(name);

		// Check if there is a class called name.
		if (c != null) {
			fail("Class " + name + " already exists");
		}
		if (parent != null) {
			// Check if the inherited class exists.
			p = verifyType(parent);
		} else {
			p = object; // By default, classes inherit from object.
		}
		ci = new ClassInfo(name, p);
		classTable.put(name, ci);
		return ci;
	}

	// Check if the variable name already exists.
	private void checkVar(ClassInfo klass, String name) {
		if (klass == object)
			return;

		for (VarInfo vi : klass.vars) {
			if (vi.name.equals(name)) {
				fail("Varibale " + name + " has already be defined in class "
						+ klass.name);
			}
		}
		// Check if we inherited a variable called name.
		checkVar(klass.parent, name);
	}

	// Add a variable to the environment.
	Env.VarInfo addVar(String klass, String name, String type) {
		ClassInfo k;
		ClassInfo vType;
		VarInfo v;

		// Check if the variables type exists.
		vType = verifyType(type);
		v = new VarInfo(name, vType);
		k = verifyType(klass);
		// Check if the variable is already defined.
		checkVar(k, name);
		k.vars.add(v);
		return v;
	}

	// Check if the method declaration is valid.
	private void checkMeth(ClassInfo klass, String name,
			List<ClassInfo> params, ClassInfo type, boolean inherited) {
		if (klass == object)
			return;

		// For all methods in the class ...
		for (MethInfo mi : klass.methods) {
			if (mi.name.equals(name)) {
				// We found a method with the same name.
				if (inherited == false) {
					fail("Cannot define method " + name + " twice.");
				}
				// Check the return type.
				if (mi.type != type) {
					fail("Attempt to overwrite " + name
							+ " but return type is wrong.");
				}
				// Check the number of arguments.
				if (mi.params.size() != params.size()) {
					fail("Attempt to overwrite " + name
							+ " but number of arguments is wrong.");
				}
				// Compare parameter types.
				for (int i = 0; i < params.size(); i++) {
					if (mi.params.get(i) != params.get(i)) {
						fail("Attempt to overwrite " + name
								+ " but parameter types are wrong.");
					}
				}
			}
		}
		checkMeth(klass.parent, name, params, type, true);
	}

	// Add a method the environment.
	Env.MethInfo addMethod(String klass, String name,List<String> params,
			String type) {
		ClassInfo k;
		ClassInfo retType;
		ClassInfo pType;
		List<ClassInfo> ps = new ArrayList<>();
		MethInfo m;

		// Check if the return type exists.
		retType = verifyType(type);
		// Check if the parameter types exist.
		for (String t : params) {
			pType = verifyType(t);
			ps.add(pType);
		}
		k = classTable.get(klass);
		// Check if the declaration is valid.
		checkMeth(k, name, ps, retType, false);
		m = new MethInfo(name, ps, retType);
		k.methods.add(m);
		return m;
	}

}