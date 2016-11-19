class Fac {
   computeFac(num : Int) : Int {
	if num < 1 then 1 
	else  num * (computeFac(num-1)) fi
	};
};
class Main inherits IO {
  main() : Object {
    { out_string("Enter a number ");
	  out_int((new Fac).computeFac(in_int()));
	  out_nl();
    }
  };
};
