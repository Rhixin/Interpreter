package bisaya.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SourceCodeReader {
    private SourceCodeReader(){}

    private static class Holder {
        private static final SourceCodeReader INSTANCE = new SourceCodeReader();
    }

    public static SourceCodeReader getInstance(){
        return Holder.INSTANCE;
    }

    public String readSourceCode(String path) throws IOException {
        Path filePath = Path.of(path);
        if (Files.exists(filePath)) {
            System.out.println("File exists: " + filePath.toAbsolutePath());
        } else {
            System.out.println("File not found: " + filePath.toAbsolutePath());
        }

        String source = Files.readString(filePath); //para basa entire strings
        return source;
    }
}
