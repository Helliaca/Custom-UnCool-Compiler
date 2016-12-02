import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum pnames implements names {
	//---- INLCUDES ALL PRODUCTIONS FOR THE UNCOOL-GRAMMAR ----

	
	//Temporary placeholder-productions that will be replaced in fixparts()
	PH_FORMAL_(new names[][] {{}}),
	PH_IDDEF(new names[][] {{}}),
	PH_PROGRAM(new names[][] {{}}),
	
	
	//Productions in the UnCool-Grammar, in order. tnames are terminals, pnames are productions.
	FORMAL_(new names[][] {
			{tnames.COMMA, tnames.ID, tnames.COLON, tnames.TYPEID, pnames.PH_FORMAL_},
			{tnames.EPSILON}
	}),
	FORMAL(new names[][] {
			{tnames.ID, tnames.COLON, tnames.TYPEID, pnames.FORMAL_},
			{tnames.EPSILON}
	}),
	EXPR_(new names[][] {
			{tnames.ASSIGN, tnames.EXPR},
			{tnames.EPSILON}
	}),
	FEAT(new names[][] {
			{tnames.BRACKETOPEN, pnames.FORMAL, tnames.BRACKETCLOSE, tnames.COLON, tnames.TYPEID, tnames.BRACEOPEN, tnames.EXPR, tnames.BRACECLOSE, tnames.SEMI, pnames.PH_IDDEF},
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
		List<tnames> ret = new ArrayList<tnames>();
		for(int i = 0; i<prod.length; i++) {
			if(prod[i].isTnames()) {ret.add((tnames) prod[i]); break;} //If we found a terminal, add it and return
			else {	//If not, add First(x), where x are the productions we just found
				pnames c = (pnames) prod[i];
				for(names[] p : c.parts) Collections.addAll(ret, First(p));
			}
		}
		return ret.toArray(new tnames[ret.size()]);
	}
	
	//For a terminal "first", return the first possible production that can start with 'first' or are empty.
	@Override
	public names[] getProduction(names first) { 
		for(names[] p : parts) { //For each production from the current Production-symbol, check if 'first' is in First(p)
			if(ArrayHelp.arrayContains(First(p), first)) return p;
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
			}
		}
	}
}
