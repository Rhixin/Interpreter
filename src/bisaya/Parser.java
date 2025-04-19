package bisaya;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static bisaya.TokenType.*;

class Parser {
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

//    Expr parse() {
//        try {
//            return expression();
//        } catch (ParseError error) {
//            return null;
//        }
//    }

    List<Stmt> parse(){
        consume(START, "Ang programa wala’y sinugdanan nga simbolo: SUGOD");
        List<Stmt> statements = new ArrayList<>();

        //loop until EOF or makita og KATAPUSAN
        while(!check(END) && !isAtEnd()){
            statements.add(declaration());
        }

        //check if KATAPUSAN
        //if EOF, meaning no KATAPUSAN
        if (!match(END)) {
            error(peek(), "Ang programa kinahanglan og panapos nga simbolo: KATAPUSAN");
        }

        return statements;
    }

    //GRAMMAR RULES HERE-------------------------------------------------------------------------

    /*
    program        → declaration* EOF ;
    declaration    → varDecl | statement ;
    statement      → exprStmt | printStmt | block | ifStmt | whileStmt | forStmt;

    exprStmt       → expression ";" ;
    printStmt      → "print" expression ";" ;
    block          → "{" declaration* "}" ;
    ifStmt         → "if" "(" expression ")" statement
                        ( "else" statement )? ;
    whileStmt      → "while" "(" expression ")" statement ;
    forStmt        → "for" "(" ( varDecl | exprStmt | ";" )
                     expression? ";"
                     expression? ")" statement ;


    expression     → assignment ;
    assignment     → IDENTIFIER "=" assignment | concat ;
    concat         → or ( "&" or )* ;
    or             → and ( "or" and )* ;
    and            → equality ( "and" equality )* ;
    equality       → comparison ( ( "!=" | "==" ) comparison )* ;
    comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
    term           → factor ( ( "-" | "+" ) factor )* ;
    factor         → unary ( ( "/" | "*" ) unary )* ;
    unary          → ( "!" | "-" ) unary | primary ;
    primary        → NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" | IDENTIFIER ;
;
    */

    // ADDED GRAMMAR FOR STATEMENTS
    private Stmt declaration(){
//        System.out.println("In declaration() - current token: " + peek());
        try{
            if(match(DECLARE)){
                return varDeclaration();
            }

            return statement();
        }catch(ParseError error){
            synchronize();
            return null;
        }
    }

    private Stmt varDeclaration() {
//        System.out.println("In varDeclaration() - current token: " + peek());
            // check if a data type is provided
        consumeDataType("Gilauman nga klase sa sulodanan: NUMERO, LETRA, TINUOD, TIPIK");
        Token name = consume(IDENTIFIER, "Nagdahom og pangalan sa sulodanan.");

        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }

        return new Stmt.Var(name, initializer);
    }

    private Stmt statement(){
//        System.out.println("In statement() - current token: " + peek());
        if(match(IF)) {
            return ifStatement();
        }else if(match(FOR)){
            return forStatement();
        } else if(match(WHILE)){
            return whileStatement();
        }else if(match(PRINT)){
            return printStatement();
        }else if(match(LEFT_CURLY)){
            return new Stmt.Block(block());
        }

        return expressionStatement();
    }

    private Stmt ifStatement(){
        consume(LEFT_PAREN, "Nagdahom og '(' human sa 'KUNG'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Nagdahom og ')' silbe panapos sa kondisyon sa 'IF STATEMENT'.");

        consume(BLOCK, "Nagdahom og 'PUNDOK' silbe timaan sa sinugdanan sa usa ka tapok.");
        consume(LEFT_CURLY, "Nagdahom og '{' human sa 'PUNDOK'");

        Stmt thenBranch = statement();

        consume(RIGHT_CURLY, "Nagdahom og '}' silbe panapos sa usa ka tapok.");

        Stmt elseBranch = null;
        if (match(ELSE)) {
            consume(BLOCK, "Nagdahom og 'PUNDOK' silbe timaan sa sinugdanan sa usa ka tapok.");
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt forStatement(){
        consume(LEFT_PAREN, "Nagdahom og '(' human sa 'ALANG SA'.");

        Stmt initializer;
        if(match(COMMA)){
            initializer = null; // FOR(,,) - infinite
        }else if(match(DECLARE)){
            initializer = varDeclaration();
            consume(COMMA, "Nagdahom og ',' human sa pagdeklara sa sulodanan.");
        }else {
            initializer = expressionStatement();
            consume(COMMA, "Nagdahom og ',' human sa ekspresyon.");
        }

        Expr condition = null;
        if(!check(COMMA)){ // if naa shay condition pa, meaning wa dayon comma eg. (, condition,)
            condition = expression();
        }

        //check og closing comma sa condition regardless if naa or wala, naa jud comma after dapat for completeness
        consume(COMMA, "Nagdahom og ',' human sa sinugdanan sa kondisyon.");

        Expr increment = null;
        if(!check(COMMA)){
            increment = expression();
        }

        consume(RIGHT_PAREN, "Nagdahom og ')' silbe panapos sa kondisyon sa 'FOR LOOP'.");

        Stmt body = statement();

        if (increment != null) {
            body = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(increment)));
        }

        if(condition == null) condition = new Expr.Literal(true); //loop infinitely
        body = new Stmt.While(condition, body);

        if(initializer != null){
            body = new Stmt.Block(Arrays.asList(initializer, body));
        }

        return body;
    }

    private Stmt whileStatement(){
        consume(LEFT_PAREN, "Nagdahom og '(' human sa 'SAMTANG'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Nagdahom og ')' silbe panapos sa kondisyon sa 'WHILE LOOP'.");

//        consume(LEFT_CURLY, "Expect {.");

        Stmt body = statement();

        return new Stmt.While(condition, body);
    }

    private Stmt printStatement(){
//        System.out.println("In printStatement() - current token: " + peek());
        Expr value = expression();
        return new Stmt.Print(value);
    }

    private Stmt expressionStatement(){
//        System.out.println("In expressionStatement() - current token: " + peek());
        Expr expr = expression();
        return new Stmt.Expression(expr);
    }

    private List<Stmt> block() {
        List<Stmt> innerStatements = new ArrayList<>();

        while (!check(RIGHT_CURLY) && !isAtEnd()) {
            innerStatements.add(declaration());
        }

        consume(RIGHT_CURLY, "Nagdahom og '}' silbe panapos sa usa ka tapok.");
        return innerStatements;
    }


    //TODO: LOGICAL OPERATORS (OR, AND) *** [CHECKED]
    //TODO: IMPLEMENT CONCAT

    private Expr expression(){
//        System.out.println("In expression() - current token: " + peek());
//        return or();
        return assignment();
    }

    private Expr assignment(){
        Expr expr = concatenation();

        if(match(EQUAL)){
            Token equals = previous();
            Expr value = assignment();

            if(expr instanceof Expr.Variable){
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            }

            error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr concatenation(){
        Expr expr = or();
        while (match(CONCAT)) {
            Token operator = previous();
            Expr right = or();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }


    private Expr or(){
        System.out.println("In or() - current token: " + peek());
        Expr expr = and();

        while(match(OR)){
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr and(){
//        System.out.println("In and() - current token: " + peek());
        Expr expr = equality();

        while(match(AND)){
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr equality(){
//        System.out.println("In equality() - current token: " + peek());
        Expr expr = comparison();

        while(match(NOT_EQUAL, EQUAL_EQUAL)){
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison(){
//        System.out.println("In comparison() - current token: " + peek());
        Expr expr = term();

        while(match(GREATER, GREATER_EQUAL, LESSER, LESSER_EQUAL)){
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term(){
//        System.out.println("In term() - current token: " + peek());
        Expr expr = factor();

        while(match(MINUS, PLUS, MODULO)){
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
//        System.out.println("In factor() - current token: " + peek());
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
//        System.out.println("In unary() - current token: " + peek());
        if (match(NOT, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    private Expr primary() {
//        System.out.println("In primary() - current token: " + peek());
        if (match(FALSE)) {
            return new Expr.Literal(false);
        } else if (match(TRUE)) {
            return new Expr.Literal(true);
        } else if (match(NUMBER, STRING, DOUBLE)) {
            return new Expr.Literal(previous().literal);
        } else if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }else if(match(IDENTIFIER)){
            return new Expr.Variable(previous());
        }

        throw error(peek(), "Expect expression.");
    }


    //HELPER FUNCTIONS HERE----------------------------------------------------------------------
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private boolean check(TokenType  type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    //Same like match method but it can only advance if it is the expected token type
    private Token consume(TokenType type, String message) {
        //check if the current token is a right paren
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    private Token consumeDataType(String message){
        if(match(NUMBER, DOUBLE, CHARACTER, BOOLEAN)){
            return previous();
        }

        throw error(peek(), message);
    }




    //ERROR HANDLING HEREEE---------------------------------------------------------------------------------------------------------------
    //To recover from errors
    //If parser encounters an error it might lose track in the grammar. it might keep generating errors
    //This is needed to recover and jump to a safe place
    private static class ParseError extends RuntimeException{

    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            //Same as semicolon????
            if (Objects.equals(previous().lexeme, "\n")) return;

            switch (peek().type) {
                case START:
                case END:
                case DECLARE:
                case FOR:
                case IF:
                case PRINT:
                case BLOCK:
                    return;
            }

            advance();
        }
    }

    private ParseError error(Token token, String message) {
        reportError(token, message);
        return new ParseError();
    }

    static void reportError(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
    }

}
