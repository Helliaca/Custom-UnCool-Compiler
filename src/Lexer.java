

/**
 * Die Implementierungen dieser Klasse sollen in der Lage sein, ein in einer Datei
 * gegebenes Quellprogramm in Token zu zerlegen.
 * 
 * @author Nadja Scharf
 *
 */
public interface Lexer {
	
	/**
	 * Die (abstrakte - das haengt von Ihrem Design ab) Klasse Token 
	 * muss noch definiert werden.
	 * Bei Aufruf der Methode soll der Lexer 
	 * das naechste Token zurueckgeben. Alle dafuer konsumierten Zeichen
	 * muessen danach nicht laenger bekannt sein.
	 * 
	 * @return naechstes Token im Tokenstrom
	 */
	public Token nextToken();
	
	/**
	 * Um spaeter hilfreiche Fehlermeldungen ausgeben zu koennen, 
	 * soll der Lexer bei Aufruf dieser Methode die Zeile im Quellproramm
	 * ausgeben, in der das zuletzt zurueckgegebene Lexem gefunden wurde.
	 * 
	 * @return aktuelle Zeilennummer, in der sich der Lexer befindet
	 */
	public int linenumber();
}
