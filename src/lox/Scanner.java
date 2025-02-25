package lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static lox.TokenType.*;

class Scanner {
    //source is the raw source code
    private final String source;

    //our scanner must generate list of tokens
    private final List<Token> tokens = new ArrayList<>();

    //Store reserved keywords here
    private static final Map<String, TokenType> keywords;
    static {
        keywords = new HashMap<>();
        keywords.put("and",    AND);
        keywords.put("class",  CLASS);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("fun",    FUN);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
    }

    //helpers in scanning
    private int start = 0;
    private int current = 0;
    private int line = 1;


    Scanner(String source){
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }

        //Appends one final “end of file” token
        //this is important ensuring the parses knows the end of the input
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            //Special cases like (!=, ==, >=, <=)
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            //Dvision is a spceial case because '/' might also be '//' which is a comment
            case '/':
                if (match('/')) {
                    // A comment goes until the end of the line.
                    while (peek() != '\n' && !isAtEnd()){
                        advance();
                    }
                } else {
                    addToken(SLASH);
                }
                break;
            //Ignoring whitespace
            case ' ':
            case '\r':
            case '\t':
                break;
            //Proceed to the next line
            case '\n':
                line++;
                break;
            //String literal
            case '"': string(); break;
            //Scanning other symbols like @#^
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    //Check first through this function if the scanned text is a reserved word or an identifier
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character.");
                }
                break;
        }
    }

    //LITERALS HERE!!!!-------------------------------------------------------------------------

    //For string literal
    private void string(){
        while(peek() != '"' && !isAtEnd()){
            if(peek() == '\n'){
                line++;
            }

            advance();
        }

        if(isAtEnd()){
            Lox.error(line, "Unterminated string");
            return;
        }

        //For the closing ""
        advance();

        //Trimming the string excluding the surrounding quotes
        String value =  source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    //For number literal
    private void number(){
        while(isDigit(peek())){
            advance();
        }

        if(peek() == '.' && isDigit(peekNext()) ){
            //Consume now the '.'. Safe to move forward
            advance();

            //Consume remaing decimal digits
            while(isDigit(peek())){
                advance();
            }
        }

        //SUGGESTION: Implement double parsing yourself but it is time consuming
        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    //For identifiers like (myVariable, averagevariable) inshort variable names or function names
    private void identifier() {
        while (isAlphaNumeric(peek())){
            advance();
        }

        String text = source.substring(start, current);

        //Check from the reserved keywords if the scanned text is found there
        TokenType type = keywords.get(text);
        if (type == null) {
            type = IDENTIFIER;
        }

        addToken(type);
    }


    //HELPER FUNCTIONS HERE!!!!------------------------------------------------------------------
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    //To know when to stop scanning
    private boolean isAtEnd() {
        return current >= source.length();
    }

    //Proceed to next character
    private char advance() {
        return source.charAt(current++);
    }

    //To check for special tokens like !=, ==, >=, <=
    //Scanner might read them as single character which is not ideal
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    //Return current character
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    //Return the next chracter (current + 1)
    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    //Adding a token to the list (2 ways. For example from the expression var x = 10;
    //1 var does not have an actual value
    private void addToken(TokenType type) {
        addToken(type, null);
    }
    //2 the token '10' has an actual value which is 10
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }


}
