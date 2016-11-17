# Custom-UnCool-Compiler

## tnames / Tokennames

The tnames.java File contains an Enum that holds all info about a Token, except the Attribute.
Each tnames object contains:
- A string that maps the object to a certain lexeme.
- A string that indicates the type of the object (Symbol, Operator, Keyword, Type, etc.)
- A string that indicates the the full name of the object (Add, Less, Equal, Assign, While, etc.)


## Token

A Token constitutes a tnames object and an optional attribute-object, which can be of any Class that inherits Javas Object-class.


## KeywordDictionary

Slightly missleading in its name, KeywordDictionary uses all the data declared in tnames.
The key to the whole class is the getToken-method, which takes a string, and returns a Token corresponding to that string.


## LexScanner

Since KeywordDictionary returns the corresponding Token to each String, the only thing left to do, is to feed it the correct strings.
The LexScanner class handles this.
The static array separators contains all characters that can stand between Tokens. These can be Tokens themselves aswell.

