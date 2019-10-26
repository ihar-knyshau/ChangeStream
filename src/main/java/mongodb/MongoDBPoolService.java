package mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.internal.Base64;

import java.util.Properties;

public class MongoDBPoolService implements MongoDBPool {
    private final MongoClient mongoClient;

    public MongoDBPoolService(Properties properties) {
        String SRV_MASK = properties.getProperty("srv_mask");
        String username = System.getenv("mongo_username");
        String password = System.getenv("mongo_password");
        final String SRV = String.format(SRV_MASK, username, password);
        this.mongoClient = MongoClients.create(SRV);
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }
}
