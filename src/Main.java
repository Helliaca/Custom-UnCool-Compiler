import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;


public class Main {
	
	public static void main(String[] args) throws IOException {
		String filename="";
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
		  
		LexScanner ls = new LexScanner(filename);
		
		Token t=null;
		int line = 0;
		do {
			t = ls.nextToken();
			if(ls.linenumber()>line) {
				line = ls.linenumber();
				System.out.println();
			}
			System.out.print(t.toString() + " ");
		} while(t.name!=tnames.EOF);
			

	}

}
