# Custom-UnCool-Compiler

## Tnames / Tokennames

The Tnames.java file contains an Enum that holds all information about a token, except its attribute.
Each Tnames object contains:
- A string that maps the object to a certain lexeme.
- A string that indicates the type of the object (Symbol, Operator, Keyword, Type, etc.)
- A string that indicates the the full name of the object (Add, Less, Equal, Assign, While, etc.)


## Token

A token constitutes a Tnames object and an optional attribute-object, which can be of any class that inherits Javas Object-class.


## KeywordDictionary

Slightly misleading in its name, KeywordDictionary uses all the data declared in Tnames.
The key to the whole class is the getToken-method, which takes a string, and returns a Token corresponding to that string.


## LexScanner

Since KeywordDictionary returns the corresponding Token to each String, the only thing left to do, is to feed it the correct strings.
The LexScanner class handles this.
The rough routine is:
- Scan text into 'saved' until we hit a separator-character.
- Get the corresponding token.
- Handle special cases such as '<', '<-' and '<='.
The static array separators contains all characters that can stand between Tokens. These can be Tokens themselves as well.

