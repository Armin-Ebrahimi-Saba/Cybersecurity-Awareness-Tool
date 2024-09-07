package services.db;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import static com.mongodb.client.model.Sorts.descending;
import java.util.ArrayList;

import static com.mongodb.client.model.Filters.eq;

public class DBConnection {

    protected String dbname = "cygraph";
    protected MongoCollection<Document> collection;

    protected MongoDatabase database;
    protected MongoClient mongo;

    public DBConnection(String collectionName) {
        // Creating a Mongo client
        int port = 27017;
        String host = "127.0.0.1";

        this.mongo = new MongoClient(host, port);

        this.database = mongo.getDatabase(this.dbname);

        if (!this.collectionExists(collectionName)){
            this.database.createCollection(collectionName);
        }
        this.collection = database.getCollection(collectionName);
    }

    public void save(Document data){
        this.collection.insertOne(data);
        this.mongo.close();
    }

    public void update(ObjectId _id, Document data){
        this.collection.updateOne(eq("_id",_id),(new BasicDBObject()).append("$set",data));
        this.mongo.close();
    }

    public Document find(Bson object){
        Document document = this.collection
                .find(object)
                .first();
        this.mongo.close();
        return document;
    }

    public ArrayList<Document> get(Bson object){
        ArrayList<Document> documents = new ArrayList<>();
        for (Document doc : this.collection.find(object)) {
            documents.add(doc);
        }
        this.mongo.close();
        return documents;
    }

    public ArrayList<Document> getAll(){
        ArrayList<Document> documents = new ArrayList<>();
        for (Document doc : this.collection.find().sort(descending("_id"))) {
            documents.add(doc);
        }
        this.mongo.close();
        return documents;
    }

    private boolean collectionExists(String collectionName) {
        MongoIterable<String> collectionNames = this.database.listCollectionNames();
        for (final String name : collectionNames) {
            if (name.equalsIgnoreCase(collectionName)) {
                return true;
            }
        }
        return false;
    }
}
