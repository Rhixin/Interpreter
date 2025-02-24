package interpreter;

class Token {
    //Specifies the category of the token (e.g., IDENTIFIER, NUMBER, PLUS, EOF). Implemented as ENUM
    final TokenType type;
    //Stores the exact characters from the source code that form the token.
    final String lexeme;
    //Holds the actual value of the token (for numbers, strings, etc.), or null if not applicable.
    final Object literal;
    //Tracks the line number where the token appears in the source code.
    final int line;


    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public String toString(){
        return "Type: " + type + "\nLexeme: " + lexeme + "\nLiteral: " + literal;
    }
}
