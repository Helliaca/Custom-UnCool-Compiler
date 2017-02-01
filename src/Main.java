import java.io.IOException;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) throws IOException {
		String filename = "";
		if (args.length == 1) {
			filename = args[0];
		} else if (args.length > 1) {
			System.out.println("Error: Too many Arguments.");
			System.exit(-1);
		} else {
			System.out.println("Enter file name: ");
			Scanner in = new Scanner(System.in);
			filename = in.next();
			in.close();
		}

		
		Node root = new Parser(new LexScanner(filename)).parse();
		System.out.println(root.toString());
		tacGenerator gen = new tacGenerator(root);
		/*
		for(pnames p : pnames.values()) p.fixparts();
		names[] x = pnames.EXPR.Follow(new names[] {tnames.ID, tnames.BRACKETOPEN, pnames.ARGS, tnames.BRACKETCLOSE});
		for (names j : x) System.out.println(j);
		*/
	}

}
