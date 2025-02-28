package bisaya;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static bisaya.TokenType.*;

class Scanner {
    //source is the raw source code
    private final String source;

    //our scanner must generate list of tokens
    private final List<Token> tokens = new ArrayList<>();

    //Store reserved keywords here
    private static final Map<String, TokenType> keywords;
    static {
        keywords = new HashMap<>();

        keywords.put("SUGOD",  START);
        keywords.put("KATAPUSAN",  END);

        keywords.put("UG",    AND);
        keywords.put("O",     OR);
        keywords.put("BALI",  NOT);

        keywords.put("KUNG",     IF);
        keywords.put("KUNG WALA",   ELSE_IF);
        keywords.put("KUNG DILI",    ELSE);

        keywords.put("ALANG SA",    FOR);

        keywords.put("OO",   TRUE);
        keywords.put("DILI",  FALSE);

        keywords.put("MUGNA",    DECLARE);
        keywords.put("IPAKITA",  PRINT);

        keywords.put("NUMERO",  NUMBER);
        keywords.put("LETRA",  CHARACTER);
        keywords.put("TINUOD",  BOOLEAN);
        keywords.put("TIPIK",  DOUBLE);

        keywords.put("PUNDOK",  BLOCK);
        keywords.put("DAWAT",  SCAN);
    }

    //helpers in scanning
    private int start = 0;
    private int current = 0;
    private int line = 1;

    Scanner(String source){
        this.source = source;
    }


    //SCANNER MAIN FUNCTIONS HERE-------------------------------------------------------------------
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

        switch (c){
            //SINGLE CHARACTERS
            //(, ), [, ], {, }
            //+ , /, *, %, =
            //,
            //$, &
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_CURLY); break;
            case '}': addToken(RIGHT_CURLY); break;
            case '[': addToken(LEFT_BRACE); break;
            case ']': addToken(RIGHT_BRACE); break;
            case '+': addToken(PLUS); break;
            case '*': addToken(STAR); break;
            case '/': addToken(SLASH); break;
            case '%': addToken(MODULO); break;
            case ',': addToken(COMMA); break;
            case '$': addToken(NEW_LINE); break;
            case '&': addToken(CONCAT); break;

            //ONE OR MORE CHARACTERS
            //=, ==
            //>, >=
            //<, <=, <>
            //-, --
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            case '<':
                addToken(match('=') ? LESSER_EQUAL : match('>') ? NOT_EQUAL : LESSER);
                break;
            case '-':
                if(match('-')){
                    while(peek() != '\n' && !isAtEnd()){
                        advance();
                    }

                } else {
                    addToken(MINUS);
                }
                break;

            //WHITE SPACE AND NEW LINE
            case ' ':
                break;
            case '\n':
                line++;
                break;

            //CHARACTER LITERAL
            case '\'':
                character();
                break;

            //STRING, TRUE, FALSE LITERAL
            case '"':
                string();
                break;

            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    //Check first through this function if the scanned text is a reserved word or an identifier
                    //TODO: SOME RESERVED WORDS ARE SEPARATED BY SPACE. EX. "KUNG WALA"
                    identifier();
                } else {
                    Bisaya.error(line, "Unexpected character.");
                }
                break;
        }

    }

    private void addToken(TokenType type){
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal){
        String lexeme = source.substring(start, current);
        tokens.add(new Token(type, lexeme,literal,line));
    }





    //LITERALS FUNCTIONS HERE-----------------------------------------------------------------
    private void character(){
        if(peekNext() != '\''){
            Bisaya.error(line, "Unterminated character");
        } else {
            char value = peek();
            advance();
            advance();
            addToken(CHARACTER, value);
        }
    }

    private void string(){
        while(peek() != '"' && !isAtEnd()){
            if(peek() == '\n'){
                line++;
            }

            advance();
        }

        if(isAtEnd()){
            Bisaya.error(line, "Unterminated string");
            return;
        }

        //For the closing ""
        advance();

        //Trimming the string excluding the surrounding quotes
        String value = source.substring(start + 1, current - 1);

        //3 possible tokentypes only for words enclosed by ""
        //STRING, TRUE, FALSE
        TokenType type = STRING;
        if(value.equals("OO")){
            type = TRUE;
        } else if (value.equals("DILI")){
            type = FALSE;
        }

        addToken(type, value);
    }

    private void number() {
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

    //For identifiers (myVariable, averagevariable) and reserved words (DILI, OO, SAMTANG)
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




    //HELPER FUNCTIONS HERE---------------------------------------------------------------------
    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        return source.charAt(current++);
    }

    private boolean match(char expected){
        if(isAtEnd() || source.charAt(current) != expected){
            return false;
        }

        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
}
