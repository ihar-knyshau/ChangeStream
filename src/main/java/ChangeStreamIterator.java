import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import mongodb.MongoDBPool;
import mongodb.MongoDBPoolService;
import org.bson.BsonTimestamp;
import org.bson.Document;
import org.bson.json.JsonWriterSettings;
import other.PropertiesReader;
import persistance.ChangeStreamResumer;
import persistance.DocumentPersister;
import persistance.FileChangeStreamResumer;
import persistance.FileDocumentPersister;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class ChangeStreamIterator {
    private static final int BATCH_SIZE = 5;
    private static final int MAX_AWAIT_TIME = 1;
    private final String CLUSTER_TIME_FILE_PATH;
    private ChangeStreamResumer changeStreamResumer;
    private DocumentPersister documentPersister;
    private MongoDBPool mongoDBPool;
    private BsonTimestamp clusterTime;

    private ChangeStreamIterator(MongoDBPool mongoDBPool, ChangeStreamResumer changeStreamResumer,
                                 DocumentPersister documentPersister, Properties properties) {
        this.mongoDBPool = mongoDBPool;
        this.changeStreamResumer = changeStreamResumer;
        this.documentPersister = documentPersister;
        this.CLUSTER_TIME_FILE_PATH = properties.getProperty("cluster_time_file_path");
    }

    private MongoCursor<Document> getChangeStream() throws IOException {
        MongoClient mongoClient = mongoDBPool.getMongoClient();

        String strClusterTime = changeStreamResumer.queryResumeInfo(CLUSTER_TIME_FILE_PATH).trim();
        Document document = mongoClient.getDatabase("admin").runCommand(new Document("buildInfo", 1));
        BsonTimestamp defaultTimestamp = document.get("$clusterTime", Document.class)
                .get("clusterTime", BsonTimestamp.class);
        clusterTime = changeStreamResumer.createClusterTimestamp(strClusterTime, defaultTimestamp);

        return mongoClient.watch()
                .maxAwaitTime(MAX_AWAIT_TIME, TimeUnit.SECONDS)
                .batchSize(BATCH_SIZE)
                .startAtOperationTime(clusterTime)
                .withDocumentClass(Document.class)
                .iterator();

    }

    private void iterateChangeStream() throws IOException {
        MongoCursor<Document> changeStream = getChangeStream();
        while (!Thread.interrupted()) {
            Document next = changeStream.next();
            if (next != null) {
                String jsonDoc = next.toJson(JsonWriterSettings.builder().indent(true).build());
                documentPersister.persistDocument(jsonDoc);
                clusterTime = next.get("clusterTime", BsonTimestamp.class);
                if (clusterTime != null) {
                    changeStreamResumer.storeResumeInfo(String.valueOf(clusterTime.getValue() + 1),
                            CLUSTER_TIME_FILE_PATH);
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        PropertiesReader propertiesReader = new PropertiesReader();
        Properties properties = propertiesReader.readProperties("src/main/resources/config.properties");
        MongoDBPool mongoDBPoolService = new MongoDBPoolService(properties);
        ChangeStreamResumer fileChangeStreamResumer = new FileChangeStreamResumer();
        DocumentPersister fileDocumentPersister = new FileDocumentPersister(properties);
        ChangeStreamIterator changeStreamIterator = new ChangeStreamIterator(mongoDBPoolService,
                fileChangeStreamResumer, fileDocumentPersister, properties);
        changeStreamIterator.iterateChangeStream();
    }
}
