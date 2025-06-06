package bisaya;


/*

Expression

    Assign
        Token name, Expression value
        x = 1

    Unary
        Token operator, Expression right
        -x

    Binary
        Expression left, Token operator, Expression right
        x + y

    Grouping
        Expression expr
        (x + y + z)

    Literal
        Token value
        123, "hello", true

    Logical
        Expression left, Token operator, Expression right
        x && y

    Variable
        Token name
        x

*/

abstract class Expr {

    interface Visitor<R> {
        R visitAssignExpr(Assign expr);
        R visitUnaryExpr(Unary expr);
        R visitBinaryExpr(Binary expr);
        R visitGroupingExpr(Grouping expr);
        R visitLiteralExpr(Literal expr);
        R visitLogicalExpr(Logical expr);
        R visitVariableExpr(Variable expr);
        R visitPostfixExpr(Postfix expr);
    }

    abstract <R> R accept(Visitor<R> visitor);

    // Nested Expr classes here-----------------------------------
    static class Assign extends Expr {
        final Token name;
        final Expr value;

        Assign(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }

        @Override
        public String toString(){
            return String.format("Token: %s, Value: %s", name.lexeme, value);
        }
    }

    static class Postfix extends Expr {
        final Token operator;
        final Expr expression;

        Postfix(Expr expression, Token operator) {
            this.expression = expression;
            this.operator = operator;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitPostfixExpr(this);
        }
    }

    static class Unary extends Expr {
        final Token operator;
        final Expr right;
        Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }

    }

    static class Binary extends Expr {
        final Expr left;
        final Token operator;
        final Expr right;

        Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }

    static class Grouping extends Expr {
        final Expr expression;

        Grouping(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }
    }

    static class Literal extends Expr {
        final Object value;

        Literal(Object value) {
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }

    static class Logical extends Expr {
        final Expr left;
        final Token operator;
        final Expr right;

        Logical(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogicalExpr(this);
        }
    }

    static class Variable extends Expr {
        final Token name;

        Variable(Token name) {
            this.name = name;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableExpr(this);
        }
    }


}
