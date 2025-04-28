package bisaya;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    final Environment enclosing;
    private final Map<String, Object> values = new HashMap<>();
    private final Map<String, TokenType> types = new HashMap<>(); // tracks types

    public Environment(){
        enclosing = null;
    }

    public Environment(Environment enclosing){
        this.enclosing = enclosing;
    }

    void define(Token name, TokenType dataType, Object value) {
        TokenType givenType = getTokenTypeFromValue(value);
//        if(givenType != dataType && value != null){
//            throw new RuntimeError(name, String.format("Ang gihatag nga bili '%s' sa sulodanan nga '%s' wala magtugma sa iyang klase sa datos '%s'.", value, name.lexeme, dataType));
//        }

        values.put(name.lexeme, value);
//        TokenType type = getTokenTypeFromValue(value)
        types.put(name.lexeme, dataType);
    }

    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }

        //crawl up sa scope
        if(enclosing != null) return enclosing.get(name);

        throw new RuntimeError(name, "Undefined variable FROM GET '" + name.lexeme + "'.");
    }

    TokenType getType(String name) {
        if (types.containsKey(name)) return types.get(name);
        if (enclosing != null) return enclosing.getType(name);
        return null;
    }

    void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            TokenType expectedType = types.get(name.lexeme);
            TokenType givenNewType = getTokenTypeFromValue(value);

//            if(!typeCompatible(expectedType, givenNewType)){
//                throw new RuntimeError(name, String.format("Type mismatch: Cannot assign %s to %s", expectedType.toString(), givenNewType.toString()));
//            }

            if(expectedType != givenNewType){
                if(expectedType == TokenType.NUMBER && givenNewType == TokenType.DOUBLE){
                    value = ((Double) value).intValue();
                }else{
                    throw new RuntimeError(name, String.format("Ang gihatag nga bili '%s' sa sulodanan nga '%s' wala magtugma sa iyang klase sa datos '%s'.", value, name.lexeme, expectedType));
                }
            }

            values.put(name.lexeme, value);
            return;
        }

        //crawl up sa nesting
        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name, "Undefined variable FROM ASSIGN'" + name.lexeme + "'.");
    }

    private boolean typeCompatible(TokenType expected, TokenType actual) {
        if (expected == actual) return true;

        // Allow implicit widening: NUMBER → DOUBLE
        return expected == TokenType.DOUBLE && actual == TokenType.NUMBER ||
                expected == TokenType.NUMBER && actual == TokenType.DOUBLE;
    }

    private TokenType getTokenTypeFromValue(Object value) {
        if (value instanceof Double) return TokenType.DOUBLE;
        if (value instanceof Integer) return TokenType.NUMBER;
        if (value instanceof Boolean) return TokenType.BOOLEAN;
        if (value instanceof String) return TokenType.STRING;
        if (value instanceof Character) return TokenType.CHARACTER;
        return null;
    }


}
