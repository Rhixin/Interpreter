package bisaya;

import java.util.List;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void>{
    private Environment environment = new Environment();

    void interpret(List<Stmt> statements){
        try{
//            Object value = evaluate(expression);
//            System.out.println(stringify(value));
            for(Stmt statement : statements){
                execute(statement);
            }
        } catch (RuntimeError error){
            Bisaya.runtimeError(error);
        }
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment; // save current environment
        try {
            this.environment = environment;

            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous; // restore the environment
        }
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type){
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(toDouble(right));
            case NOT:
                return !isTruthy(right);
        }

        //Unreachable
        return null;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {


        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return toDouble(left) - toDouble(right);
            case PLUS:
                //TODO: Implement concat when either one of the operand is a string
                if (left instanceof Number && right instanceof Number) {
                    return toDouble(left) + toDouble(right);
                }

                if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }

                //CONCAT STRING AND NUMBER
                if(left instanceof String && right instanceof Number){
                    return left + right.toString();
                } else if (left instanceof Number && right instanceof String){
                    return left.toString() + right;
                }

                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
            case SLASH:
                //TODO: Implement division by zero
                checkNumberOperands(expr.operator, left, right);
                return toDouble(left) / toDouble(right);
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return toDouble(left) * toDouble(right);
            case NOT_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return toDouble(left) > toDouble(right);
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return toDouble(left) >= toDouble(right);
            case LESSER:
                checkNumberOperands(expr.operator, left, right);
                return toDouble(left) < toDouble(right);
            case LESSER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return toDouble(left) <= toDouble(right);
        }

        //Unreachable
        return null;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);

        if(expr.operator.type == TokenType.OR) {
            //no need to evaluate the right
            if(isTruthy(left)) return left;
        }else if(expr.operator.type == TokenType.AND){
            if(!isTruthy(left)) return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }

    //Statement overrides ------------------------------------------------------------------------------
    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }


    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
        public Void visitBlockStmt(Stmt.Block stmt) {
            executeBlock(stmt.statements, new Environment(environment));
            return null;
        }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        return null;
    }

    @Override
    public Void visitForStmt(Stmt.While stmt) {
        return null;
    }


    //HELPER FUNCTIONS----------------------------------------------------------------------------------
    private boolean isTruthy(Object object){
        //false and null are falsey
        //everything else is truthy
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean)object;

        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;

        return a.equals(b);
    }

    private void checkNumberOperand(Token operator, Object operand){
        if(operand instanceof Number){
            return;
        }

        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Number && right instanceof Number) return;


        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private String stringify(Object object) {
        if (object == null) return "null";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }

    private Double toDouble(Object number){
        return ((Number) number).doubleValue();
    }
}
