package bisaya;

import bisaya.Utils.SourceCodeReader;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
//        Expr expression = new Expr.Binary(
//                new Expr.Unary(new Token(TokenType.MINUS, "-", null, 1), new Expr.Literal(123)),
//                new Token(TokenType.STAR, "*", null, 1),
//                new Expr.Grouping(new Expr.Literal(45.67)));
//
//        System.out.println(new AstPrinter().print(expression));

        SourceCodeReader reader = SourceCodeReader.getInstance();
        String source = "";
        Scanner scScanner = null;

        try{
            source = reader.readSourceCode("src/bisaya/Utils/sample.txt");
        }catch (IOException e){
            e.printStackTrace();
        }

        scScanner = new Scanner(source);
        scScanner.scanTokens();
        scScanner.printTokens();
    }
}
