package bisaya;

enum TokenType {
    //Single character tokens--------------------------------------------------------
    //(, ), [, ]
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    //+ , /, *, %, =
    PLUS, SLASH, STAR, MODULO, EQUAL,
    //, .
    COMMA, DOT,
    //$, &
    NEXT_LINE, CONCAT,
    //or
    O,

    // One or two character tokens OR misleading tokens.-----------------------------
    //<, <=
    GREATER, GREATER_EQUAL,
    //<, <=, <>
    LESS, LESS_EQUAL, NOT_EQUAL,
    //-, --
    MINUS, COMMENT,
    //if, else if, else
    KUNG, KUNG_DILI, KUNG_WALA,

    // Literals.--------------------------------------------------------------------
    // variable name, number, double, string??, character, boolean
    IDENTIFIER, NUMERO, TIPIK, STRING, LETRA, TINUOD,

    // Keywords.---------------------------------------------------------------------
    //and, True -> "OO", !, False -> "DILI"
    UG, OO, DILI_OPERATOR, DILI_LITERAL,
    //start, end, var, print, block statement, for, uninitialized variable
    SUGOD, KATAPUSAN, MUGNA, IPAKITA, PUNDOK, ALANG_SA, DAWAT,

    EOF
}