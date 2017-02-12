class Main inherits IO {
    main() : Object {
        let a : Int <- 1,
            b : Int <- 0,
            c : Int,
            i : Int <- 1,
            n : Int
        in
        {
        out_string("Wie viele?");
        n <- in_int();
        while i <= n loop
            { out_int(a);
            c <- a;
            a <- a+b;
            b <- c;
            i <- i+1;
            }
        pool;
        }
    };
}