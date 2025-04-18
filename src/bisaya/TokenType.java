package bisaya;

enum TokenType {
    //Single character tokens--------------------------------------------------------
    //(, ), [, ], {, }
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, LEFT_CURLY, RIGHT_CURLY,
    //+ , /, *, %, =
    PLUS, SLASH, STAR, MODULO, EQUAL,
    //,
    COMMA,
    //$, &
    NEW_LINE, CONCAT,

    // One or two character tokens OR misleading tokens.-----------------------------
    //>, >=
    GREATER, GREATER_EQUAL,
    //<, <=, <>, ==
    LESSER, LESSER_EQUAL, NOT_EQUAL, EQUAL_EQUAL,
    //-, --
    MINUS,

    // Literals.--------------------------------------------------------------------
    IDENTIFIER, NUMBER, DOUBLE, CHARACTER, BOOLEAN,

    // Keywords.---------------------------------------------------------------------
    START, END, DECLARE, PRINT, BLOCK, FOR, SCAN, AND, OR, TRUE, NOT, FALSE, IF, ELSE_IF, ELSE,


    //??------------------------------------------------------------------------------
    STRING,

    EOF

    //TODO:
//    public enum TokenType {
//        NUMBER("number"),
//        DOUBLE("double"),
//        STRING("string"),
//        BOOLEAN("boolean");
//
//        private final String displayName;
//
//        TokenType(String displayName) {
//            this.displayName = displayName;
//        }
//
//        @Override
//        public String toString() {
//            return displayName;
//        }
//    }

    }