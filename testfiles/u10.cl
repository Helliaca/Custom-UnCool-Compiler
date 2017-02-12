class Main inherits IO {
    prim(n:Int):Bool{
        let i: Int <- 2 in
        { while not n/i*i = n loop
            i <- i+1
          pool;
        i=n;
        }
    };
    main () : Object {
        let n: Int in
        {
        out_string("Primzahltest fuer Zahl (>1):");
        n <- in_int();
        out_nl();
        if prim(n) then
            out_string("Primzahl")
        else
            out_string("zusammengesetzte Zahl")
        fi;
        }
    };
};