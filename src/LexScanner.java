import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class LexScanner implements Lexer {

	private char[] buffer;      // Buffer that stores current chuck of source-text
	                            // being read.
	private int pos;            // Current position in the buffer being read
	private int bytes;          // Amount of Bytes in the buffer
	private int startline;		// Stores line on which the current lexeme starts
	private int line;           // Stores current line-number that is being read
	private boolean eof;        // Stores whether if we have reached the end of the
	                            // file or not
	private KeywordDictionary kwd;      // Dictionary that contains all tokens with
	                                    // their corresponding lexemes
	private BufferedReader br;          // Used to read lines from file
	final int BufSize = 4096;           // Size of buffer
	private String saved;               // Stores currently read lexeme
	private char c;                     // Stores character right after 'saved'

	// Contains all characters that indicate that a lexeme *might* have
	// come to an end
	static char[] separators = { '\r', '\0', '\n', '\t', '\f', ' ', '+', '-',
			'*', '/', '=', '<', ':', ';', '.', ',', '(', '[', '{', '}', ']',
			')', '"' };
	// Contains all characters in between lexemes that can be safely ignored
	static char[] ignore = { ' ', '\t', '\n', '\r', '\f' };
	// Contains all Tokens that don't need to be returned by the
	// nextToken-method
	static Tnames[] doNotReturn = { Tnames.COMMENT };

	public LexScanner(String file) {
		kwd = new KeywordDictionary();
		saved = "";
		pos = 0;
		bytes = 0;
		line = startline = 1;
		buffer = new char[BufSize];

		// create a new reader
		try {
			br = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			System.out.println("Can't find file: " + file);
			System.exit(-1);
		}
		// fill the buffer
		readChunk();
		c = next();
	}

	@Override
	public Token nextToken() {

		// Skip all whitespace etc.
		while (arrayContains(ignore, c)) 
			c = next();

		if (eof)
			return new Token(Tnames.EOF);
		startline = line;
		// Once we have found a relevant character, scan until we hit a
		// separator-character
		skipUntil(separators);
		Token t = kwd.getToken(saved); // Let's see what we found

		// --- SPECIAL CASES: ---
		{
			// If we found a minus-char, check if it is a comment
			if (t.name == Tnames.SUB && c == '-') {
				skipUntil(new char[] { '\n' }); // If so, skip and discard the
												// entire line
				t = new Token(Tnames.COMMENT);
				c = next();
			}
			// If we found a quotation-mark, get the string literal
			else if (t.name == Tnames.QUOTATIONMARK) {
				if (c == '"') {
					t = kwd.getToken("\"\"");
				} // Case "" requires special treatment due to the nature of
					// skipUntil
				else {
					if (!skipUntil(new char[] { '"' })) // Look for the next quotation-mark
						error(saved); // If there is none, complain
					c = saveAndNext(c);
					t = kwd.getToken(saved);
				}
			}
			// If we found a '<', it could be <-, <= or <
			else if (t.name == Tnames.LT && kwd.getToken(saved + c).name != Tnames.ERROR) {
				c = saveAndNext(c);
				t = kwd.getToken(saved);
			}
		}

		saved = ""; // Reset saved
		if (arrayContains(doNotReturn, t.name))
			t = nextToken(); // Get the next Token if we ought to ignore this one
		if(t.name == Tnames.ERROR)
			error(t.attr.toString());
		return t;

	}

	// Will move c further in the buffer until a character in chars or \0 is
	// found.
	private boolean skipUntil(char[] chars) {
		if (eof)
			return false; // Couldn't find it :(
		if (arrayContains(chars, c)) { // Check if we are looking for c itself
			c = saveAndNext(c);
			return true;
		} else { // Move on
			while (!arrayContains(chars, c)) {
				if (eof)
					return false;
				c = saveAndNext(c);
			}
		}
		return true;
	}
	
	//Prints error message and quits.
	private void error(String s) {
		System.out.println("Error at line " + line + ": " + s);
		System.exit(-1);
	}

	// Read the next part of the input file into the buffer.
	private void readChunk() {
		try {
			bytes = br.read(buffer, 0, BufSize);
			if (bytes < 0)
				eof = true;
		} catch (IOException e) {
			System.out.println(e);
			System.exit(-1);
		}
	}

	// Save the parameter to 'saved' and return next character
	private char saveAndNext(char x) {
		save(x);
		return next();
	}

	// Append parameter to saved
	private void save(char x) {
		saved += x;
	}

	// Returns the current character and advances the input stream.
	private char next() {
		if (eof) {
			return '\0';
		}

		char c = buffer[pos++];

		if (pos == bytes) {
			// We've read all chars in the buffer. Get the next chunk.
			readChunk();
			pos = 0;
		}
		if (c == '\n')
			newLine();
		return c;
	}

	@Override
	public int linenumber() {
		return startline;
	}

	private void newLine() {
		line++;
	}

	private <A> boolean arrayContains(A[] arr, A obj) {
		for (A a : arr)
			if (a == obj)
				return true;
		return false;
	}

	private boolean arrayContains(char[] arr, char obj) {
		for (char a : arr)
			if (a == obj)
				return true;
		return false;
	}

}
