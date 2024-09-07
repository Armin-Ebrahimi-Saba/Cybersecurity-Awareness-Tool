package models;

import org.bson.Document;
import org.bson.types.ObjectId;
import services.db.DBConnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.mongodb.client.model.Filters.eq;

abstract public class Base {

    public ObjectId _id;
    public String title;
    public String description = null;
    public String type = "";

    protected static DBConnection getConnection(String collection){
        return new DBConnection(collection);
    }

    abstract public void save();

    abstract public String getCollection();

    public static Document findByMainIdAndDbCon(DBConnection dbConnection,Object _id){
        return dbConnection.find(eq("_id",_id));
    }

    public static Document findByIdAndDbCon(DBConnection dbConnection,String id){
        return dbConnection.find(eq("id",id));
    }

    public static ArrayList<Document> getAll(DBConnection dbConnection){
        return dbConnection.getAll();
    }

    public void saveOrUpdate(DBConnection dbConnection,Document document){
        if (this._id == null){
            dbConnection.save(document);
        }
        else {
            dbConnection.update(this._id,document);
        }
    }

    public void addNode(long clock, HashMap <String, Object> relation) {
        DBConnection dbConnection = getConnection("changes");
        Document document = new Document("_id", new ObjectId());
        document.append("document_type", "node");
        document.append("operation", "add");
        document.append("relation", relation);
        for (Map.Entry<String, Object> property : getNodeProproperties(clock).entrySet()) {
            document.append(property.getKey(), property.getValue());
        }
        dbConnection.save(document);
    }

    public void deleteNode(long clock, ArrayList<HashMap <String, Object>> relations) {
        DBConnection dbConnection = getConnection("changes");
        Document document = new Document("_id", new ObjectId());
        document.append("document_type", "node");
        document.append("operation", "delete");
        document.append("relations", relations);
        for (Map.Entry<String, Object> property : getNodeProproperties(clock).entrySet()) {
            document.append(property.getKey(), property.getValue());
        }
        dbConnection.save(document);
    }

    public HashMap<String,Object> getNodeProproperties(long clock) {
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("identifier", _id);
        properties.put("name", title);
        properties.put("collection", getCollection());
        properties.put("type", type);
        properties.put("time", clock);
        return properties;
    }

}
