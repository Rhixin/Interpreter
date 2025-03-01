package bisaya;

import bisaya.Utils.SourceCodeReader;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        SourceCodeReader reader = SourceCodeReader.getInstance();
        String source = "";
        Scanner scScanner = null;

        try{
            source = reader.readSourceCode("src/bisaya/Utils/sample.txt");
        }catch (IOException e){
            e.printStackTrace();
        }

        scScanner = new Scanner(source);
        //scScanner.scanTokens();
        //scScanner.printTokens();

        Expr expression1 = new Expr.Binary(
                new Expr.Binary(
                        new Expr.Literal(10),
                        new Token(TokenType.PLUS, "+", null, 1),
                        new Expr.Literal(20)
                ),
                new Token(TokenType.STAR, "*", null, 1),
                new Expr.Unary(
                        new Token(TokenType.MINUS, "-", null, 1),
                        new Expr.Literal(5)
                )
        );

        System.out.println(new AstPrinter().print(expression1));


    }
}
