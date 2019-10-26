package persistance;

import org.bson.*;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface ChangeStreamResumer {
    void storeResumeInfo(String data, String file) throws IOException;
    String queryResumeInfo(String file) throws IOException;
    default BsonTimestamp createClusterTimestamp(String timestamp, BsonTimestamp defaultTimestamp) {
        if (timestamp != null && !timestamp.isEmpty()) {
            return new BsonTimestamp(Long.parseLong(timestamp));
        }
        return defaultTimestamp;
    }
}
