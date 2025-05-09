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

            System.out.println("\n[No Error]");
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
    private boolean typeCompatible(TokenType expected, TokenType actual) {
        if (expected == actual) return true;

        // Allow implicit widening: NUMBER → DOUBLE
        return expected == TokenType.DOUBLE && actual == TokenType.NUMBER;
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
//                if (left instanceof Number && right instanceof Number) {
//                    return toDouble(left) + toDouble(right);
//                }

                //both integer sha
                if (left instanceof Integer && right instanceof Integer) {
                    return ((Integer) left) + ((Integer) right); // Returns Integer
                }

                //both sha strings
                if(left instanceof Double || right instanceof Double){
                    return toDouble(left) + toDouble(right); // Returns Double
                }

                //both strings
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
                if (toDouble(right) == 0) {
                    throw new RuntimeError(expr.operator, "Cannot divide by zero.");
                }
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
            case MODULO:
                checkNumberOperands(expr.operator, left, right);
                if (toDouble(right) == 0) {
                    throw new RuntimeError(expr.operator, "Cannot modulo by zero.");
                }
                return toDouble(left) % toDouble(right);
            case CONCAT:
                return stringify(left) + stringify(right);
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
        System.out.print(stringify(value));
        return null;
    }

    @Override
    public Void visitInputStmt(Stmt.Input stmt) {
        System.out.print("Ibutang ang mga bili para sa: ");
        for (int i = 0; i < stmt.names.size(); i++) {
            System.out.print(stmt.names.get(i).lexeme);
            if (i < stmt.names.size() - 1) System.out.print(", ");
        }

        System.out.print("\n>> ");

        //para di mo conflict sa atoang Scanner nga lexer
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        String input = scanner.nextLine();
        String[] parts = input.split(",");

        if (parts.length != stmt.names.size()) {
            throw new RuntimeError(stmt.names.get(0), "Gilauman ang " + stmt.names.size() + " ka bili, pero nakuha ang " + parts.length + ".");
        }
//
//        for(int i = 0;  i < stmt.names.size(); i++){
//            System.out.println(stmt.names.get(i));
//        }

        for (int i = 0; i < stmt.names.size(); i++) {
            Token name = stmt.names.get(i);

            String raw = parts[i].trim();
            TokenType expectedType = environment.getType(name.lexeme);

            Object value = parseInput(raw, expectedType, name);

            System.out.println("THE VALUE IS: " + value);


            environment.assign(name, value);

//            System.out.println("Name: " + name);
//            System.out.println("Value: " + environment.get(name));
        }

        return null;
    }


    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
//        Object value = null;
//        if (stmt.initializer != null) {
//            value = evaluate(stmt.initializer);
//        }
//
//        environment.define(stmt.name, value);
        return null;
    }
    @Override
    public Void visitMultiVarStmt(Stmt.MultiVar stmt) {
        for (Stmt.Var var : stmt.vars) {
            Object value = null;
            if (var.initializer != null) {
                value = evaluate(var.initializer);
            }

            environment.define(var.name, stmt.dataType, value);
        }
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
//            System.out.println("Executing then branch...");
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
//            System.out.println("Executing else branch...");
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitForStmt(Stmt.While stmt) {
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
        }
        return null;
    }

    @Override
    public Object visitPostfixExpr(Expr.Postfix expr) {
        if (!(expr.expression instanceof Expr.Variable)) {
            throw new RuntimeError(expr.operator, "Ang pagdungag og usa sa kantidad mamahimo ra sa sa usa ka sulodanan.");
        }

        Expr.Variable variable = (Expr.Variable) expr.expression;
        Object value = environment.get(variable.name);

        if (!(value instanceof Number)) {
            throw new RuntimeError(expr.operator, "Pwede ra ma-increment ang mga numero.");
        }

        double current = toDouble(value);
        double updated = expr.operator.type == TokenType.INCREMENT ? current + 1 : current - 1;

        // Assign the updated value
        environment.assign(variable.name, updated);

        // Return the original value (postfix behavior)
        return current;
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

        //TODO
        //special case when comparing 1 == 1.0 (evals to false)
        //or 0.0 == 0 (evals to false)

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

        System.out.println(object);

        if (object instanceof Boolean) {
            return (Boolean) object ? "OO" : "DILI";
        }

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

    private Object parseInput(String raw, TokenType expectedType, Token name) {

        try {
            switch (expectedType) {
                case NUMBER:
                    return Integer.parseInt(raw);
                case DOUBLE:
                    return Double.parseDouble(raw);
                case BOOLEAN:
                    if(raw.equals("\"OO\"")){
                        return true;
                    }else{
                        return false;
                    }
                case CHARACTER:
                    if (raw.length() != 1) throw new RuntimeException();
                    return raw.charAt(0);
                case STRING:
                    return raw;
                default:
                    throw new RuntimeError(name, "Di mailhan nga klase sa datos.");
            }
        } catch (Exception e) {
            throw new RuntimeError(name, "Di matagaan og bili ang '" + raw + "' isip " + expectedType + ".");
        }
    }

}
