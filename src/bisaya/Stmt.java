package bisaya;

import java.util.List;

abstract class Stmt {
    interface Visitor<R> {
        R visitBlockStmt(Block stmt);
        R visitExpressionStmt(Expression stmt);
        R visitIfStmt(If stmt);
        R visitPrintStmt(Print stmt);
        R visitVarStmt(Var stmt);
        R visitForStmt(While stmt);
        R visitWhileStmt(While stmt);
        R visitMultiVarStmt(MultiVar stmt);

    }

    abstract <R> R accept(Visitor<R> visitor);

    // Nested Stmt classes here.-----------------------------------
    static class Block extends Stmt {
        final List<Stmt> statements;

        Block(List<Stmt> statements) {
            this.statements = statements;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }
    }

    static class Expression extends Stmt {
        final Expr expression;

        Expression(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }
    }

    static class If extends Stmt {
        final Expr condition;
        final Stmt thenBranch;
        final Stmt elseBranch;

        If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStmt(this);
        }
    }

    static class Print extends Stmt {
        final Expr expression;

        Print(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStmt(this);
        }
    }

    static class Var extends Stmt {
        final Token name;
        final Expr initializer;
        final TokenType dataType;

        Var(Token name, Expr initializer, TokenType dataType) {
            this.name = name;
            this.initializer = initializer;
            this.dataType = dataType;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarStmt(this);
        }

        @Override
        public String toString(){
            return name.lexeme;
        }
    }

    public static class MultiVar extends Stmt {
        public final TokenType dataType;
        public final List<Var> vars;

        public MultiVar(TokenType dataType, List<Var> vars) {
            this.dataType = dataType;
            this.vars = vars;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitMultiVarStmt(this);
        }
    }

    static class While extends Stmt {
        final Expr condition;
        final Stmt body;

        While(Expr condition, Stmt body) {
            this.condition = condition;
            this.body = body;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStmt(this);
        }
    }

}
