package bisaya;

import java.util.ArrayList;
import java.util.List;

class Scanner {
    //source is the raw source code
    private final String source;

    //our scanner must generate list of tokens
    private final List<Token> tokens = new ArrayList<>();

    //helpers in scanning
    private int start = 0;
    private int current = 0;
    private int line = 1;


    Scanner(String source){
        this.source = source;
    }
}
