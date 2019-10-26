package mongodb;

import com.mongodb.client.MongoClient;

public interface MongoDBPool {
    MongoClient getMongoClient();
}
