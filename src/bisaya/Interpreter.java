package bisaya;

import com.sun.jdi.DoubleValue;

public class Interpreter implements Expr.Visitor<Object>{

    void interpret(Expr expression){
        try{
            Object value = evaluate(expression);
            System.out.println(stringify(value));
        } catch (RuntimeError error){
            Bisaya.runtimeError(error);
        }
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        return null;
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
        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
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
