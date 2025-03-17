package bisaya;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Bisaya {
    private static final Interpreter interpreter = new Interpreter();
    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            //if more than 1 argument error message is printed
            System.out.println("Usage: Bisaya [script]");
            System.exit(64);
        } else if (args.length == 1) {
            //runs one single file
            runFile(args[0]);
        } else {
            //interavtive prompt where user can type commands a time
            runPrompt();
        }
    }


    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        // Indicate an error in the exit code.
        if (hadError) System.exit(65);
        if (hadRuntimeError) System.exit(70);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
            hadError = false;
        }
    }

    private static void run(String source) {

        //Don't execute a code with known errors
        if(hadError) {
            System.exit(65);
        }

        //TOKENIZATION
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        Expr expression = parser.parse();

        // Stop if there was a syntax error.
        if (hadError) return;

        //PARSING
        //print expression before parsing. just to check lang
        System.out.println(new AstPrinter().print(expression));
        interpreter.interpret(expression);
    }

    //Suggestion:
    //Create interface ErrorReporter to achieve different implementations of error reporting
    //Example: ConsoleErrorReporter, FileErrorReporter (way gamit ba ang mo console log ug errors????)
    //Or Create separate class for the ErrorHandler
    static void error(int line, String message){
        report(line, "", message);
    }

    private static void report(int line, String where, String message){
        System.err.println("[line " + line + "] Error " + where + ": " + message);
        hadError = true;
    }

    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() + "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }
}
