package persistance;

import java.io.IOException;

public interface DocumentPersister {
    void persistDocument(String content) throws IOException;
}
