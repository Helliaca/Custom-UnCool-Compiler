import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum pnames implements names {
	//---- INLCUDES ALL PRODUCTIONS FOR THE UNCOOL-GRAMMAR ----

	
	//Temporary placeholder-productions that will be replaced in fixparts()
	PH_EXPR(new names[][] {{}}),
	PH_ARGS(new names[][] {{}}),
	PH_MATH(new names[][] {{}}),
	PH_EXTEXPR(new names[][] {{}}),
	PH_T(new names[][] {{}}),
	PH_FORMAL_(new names[][] {{}}),
	PH_IDDEF(new names[][] {{}}),
	PH_PROGRAM(new names[][] {{}}),
	
	
	//Productions in the UnCool-Grammar, in order. tnames are terminals, pnames are productions.
	//Example: The one below is: 
	// F -> ID | CONSTANT | ( MATH )
	F(new names[][] {
			{tnames.ID},
			{tnames.CONSTANT},
			{tnames.BRACKETOPEN, pnames.PH_MATH, tnames.BRACKETCLOSE}
	}),
	T(new names[][] {
			{pnames.F, tnames.MUL, pnames.PH_T},
			{pnames.F, tnames.DIV, pnames.PH_T},
			{tnames.COMPLEMENT, pnames.F},
			{pnames.F}
	}),
	MATH(new names[][] {
			{pnames.T, tnames.ADD, pnames.PH_MATH},
			{pnames.T, tnames.SUB, pnames.PH_MATH},
			{pnames.T}
	}),
	BOOLEAN_(new names[][] {
			{tnames.LE, pnames.MATH},
			{tnames.LT, pnames.MATH},
			{tnames.EQ, pnames.MATH},
			{tnames.EPSILON}
	}),
	ARGS_(new names[][] {
			{tnames.COMMA, pnames.PH_ARGS},
			{tnames.EPSILON}
	}),
	ARGS(new names[][] {
			{pnames.MATH, pnames.ARGS_},
			{tnames.ID, pnames.ARGS_},
			{tnames.CONSTANT, pnames.ARGS_},
			{tnames.EPSILON}
	}),
	EXTEXPR(new names[][] {
			{pnames.MATH, pnames.BOOLEAN_},
			{pnames.PH_EXTEXPR, tnames.EQ, pnames.PH_EXTEXPR},
			{tnames.NOT, pnames.PH_EXTEXPR},
			{tnames.TRUE},
			{tnames.FALSE}
	}),
	EXPR(new names[][] {
			{tnames.ID, tnames.ASSIGN, pnames.PH_EXPR},
			{tnames.ID, tnames.DOT, tnames.ID, tnames.BRACKETOPEN, pnames.ARGS, tnames.BRACKETCLOSE},
			{tnames.ID, tnames.BRACKETOPEN, pnames.ARGS, tnames.BRACKETCLOSE},
			{tnames.IF, pnames.PH_EXPR, tnames.THEN, pnames.PH_EXPR, tnames.ELSE, pnames.PH_EXPR, tnames.FI},
			{tnames.WHILE, pnames.PH_EXPR, tnames.LOOP, pnames.PH_EXPR, tnames.LOOP},
			{tnames.NEW, tnames.TYPEID},
			{tnames.BRACKETOPEN, pnames.PH_EXPR, tnames.BRACKETCLOSE},
			{tnames.ID},
			{pnames.EXTEXPR}
	}),
	FORMAL_(new names[][] {
			{tnames.COMMA, tnames.ID, tnames.COLON, tnames.TYPEID, pnames.PH_FORMAL_},
			{tnames.EPSILON}
	}),
	FORMAL(new names[][] {
			{tnames.ID, tnames.COLON, tnames.TYPEID, pnames.FORMAL_},
			{tnames.EPSILON}
	}),
	EXPR_(new names[][] {
			{tnames.ASSIGN, pnames.EXPR},
			{tnames.EPSILON}
	}),
	FEAT(new names[][] {
			{tnames.BRACKETOPEN, pnames.FORMAL, tnames.BRACKETCLOSE, tnames.COLON, tnames.TYPEID, tnames.BRACEOPEN, pnames.EXPR, tnames.BRACECLOSE, tnames.SEMI, pnames.PH_IDDEF},
			{tnames.COLON, tnames.TYPEID, pnames.EXPR_, tnames.SEMI, pnames.PH_IDDEF}
	}),
	IDDEF(new names[][] {
			{tnames.ID, pnames.FEAT},
			{tnames.EPSILON}
	}),
	IHERITANCE(new names[][] {
			{tnames.INHERITS, tnames.TYPEID},
			{tnames.EPSILON}
	}),
	CLASSDEF(new names[][] {
			{tnames.CLASS, tnames.TYPEID, pnames.IHERITANCE, tnames.BRACEOPEN, pnames.IDDEF, tnames.BRACECLOSE}
	}),
	CLASSDEF_(new names[][] {
			{pnames.PH_PROGRAM},
			{tnames.EPSILON}
	}),
	PROGRAM(new names[][] {
			{pnames.CLASSDEF, tnames.SEMI, pnames.CLASSDEF_},
	});
	
	
	private final String FullName;
	private names[][] parts;
	
	
	//Returns a List of tnames (terminals) that can appear as the first terminal in a production.
	@Override
	public tnames[] First(names[] prod) {
		ArrayList<pnames> cs = new ArrayList<pnames>();
		return First(prod, cs);
	}
	
	//Same as above, but with a List of items that dont need to be called recursively
	public tnames[] First(names[] prod, ArrayList<pnames> callstack) {
		ArrayList<tnames> ret = new ArrayList<tnames>();
		for(int i = 0; i<prod.length; i++) {
			if(prod[i].isTnames() && prod[i]==tnames.EPSILON) {ret.add((tnames) prod[i]);} //If we found epsilon, add it and continue
			else if(prod[i].isTnames()) {ret.add((tnames) prod[i]); break;} //If we found a terminal, add it and return
			else if(callstack.contains((pnames) prod[i])) break;
			else if (!callstack.contains((pnames) prod[i])) {	//If not, add First(x), where x are the productions we just found, as long as First(x) hasn't been called already
				pnames c = (pnames) prod[i];
				callstack.add(c);
				for(names[] p : c.parts) Collections.addAll(ret, First(p, callstack));
				if(!ArrayHelp.arrayContains(ret.toArray(), tnames.EPSILON)) break;
			}
		}
		return ret.toArray(new tnames[ret.size()]);
	}
	
	//Same as above, but takes just the Symbol as parameter instead of entire production
	public tnames[] First(names prod) {
		if(prod.isTnames()) return new tnames[] {(tnames)prod};
		ArrayList<tnames> ret = new ArrayList<tnames>();
		for(names[] p : ((pnames)prod).parts) Collections.addAll(ret, First(p));
		return ret.toArray(new tnames[ret.size()]);
	}
	
	//Returns set of terminals that can appear as the second symbol of a production
	public tnames[] Follow(names prod) {
		if(prod.isTnames()) return new tnames[] {tnames.EOF};
		ArrayList<tnames> ret = new ArrayList<tnames>();
		for(pnames pns : pnames.values()) {
			for (names[] ps : pns.parts) {
				for(int i=0; i<ps.length; i++) {
					int u = 1;
					while(ps[i]==prod && u+i < ps.length && ArrayHelp.arrayContains(First(ps[u+i]), tnames.EPSILON)) {
						Collections.addAll(ret,  First(ps[u+i]));
						u++;
					}
				}
			}
		}
		for(names[] p : ((pnames)prod).parts) Collections.addAll(ret, Follow(p));
		return ret.toArray(new tnames[ret.size()]);
	}
	
	//Same as above, but takes entire production as parameter
	public tnames[] Follow(names[] prod) {
		if(prod.length<2) return new tnames[] {tnames.EOF};
		ArrayList<tnames> ret = new ArrayList<tnames>();
		ret.add(tnames.EOF);
		int i = 0;
		do {
			i++;
			Collections.addAll(ret, First(prod[i]));
		} while(i<prod.length-1 && ArrayHelp.arrayContains(First(prod[i]), tnames.EPSILON));
		if(!prod[0].isTnames()) {
			Collections.addAll(ret, Follow(prod[0]));
		}
		return ret.toArray(new tnames[ret.size()]);
	}
	
	//For a terminal "first", return the first possible production that can start with 'first' or are empty.
	@Override
	public names[] getProduction(tnames first, tnames follow) {
		for(names[] p : parts) { //For each production from the current Production-symbol, check if 'first' is in First(p)
			if(ArrayHelp.arrayContains(First(p), first) && ArrayHelp.arrayContains(Follow(p), follow)) return p;
		}
		for(names[] p : parts) { //Special case for productions like X -> ID
			if(ArrayHelp.arrayContains(First(p), first) && p.length==1) return p;
		}
		for(names[] p : parts) { //If not, check if Epsilon is in any First(...)
			if(ArrayHelp.arrayContains(First(p), tnames.EPSILON)) return new names[] {tnames.EPSILON};
		}
		return null; //Didn't find it.
	}
	
	
	private pnames(String name, names[][] parts) {
		this.FullName = name;
		this.parts = parts;
	}
	
	
	private pnames(names[][] parts) {
		this.FullName = this.toString();
		this.parts = parts;
	}
	
	
	//Replace placeholder-productions by the correct ones
	public void fixparts() {
		for(names[] p : this.parts) {
			for(int i=0; i<p.length; i++) {
				if(p[i]==pnames.PH_FORMAL_) p[i]=pnames.FORMAL_;
				else if(p[i]==pnames.PH_IDDEF) p[i]=pnames.IDDEF;
				else if(p[i]==pnames.PH_PROGRAM) p[i]=pnames.PROGRAM;
				else if(p[i]==pnames.PH_MATH) p[i]=pnames.MATH;
				else if(p[i]==pnames.PH_T) p[i]=pnames.T;
				else if(p[i]==pnames.PH_ARGS) p[i]=pnames.ARGS;
				else if(p[i]==pnames.PH_EXTEXPR) p[i]=pnames.EXTEXPR;
				else if(p[i]==pnames.PH_EXPR) p[i]=pnames.EXPR;
			}
		}
	}

	@Override
	public boolean isTnames() {
		return false;
	}
}
