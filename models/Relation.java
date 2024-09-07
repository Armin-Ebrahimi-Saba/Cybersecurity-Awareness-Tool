package models;

import org.bson.Document;
import org.bson.types.ObjectId;
import services.db.DBConnection;

import java.util.HashMap;
import java.util.Map;

public class Relation extends Base {
    @Override
    public void save() {

    }

    @Override
    public String getCollection() {
        return null;
    }

    public void changeRelation(long clock, HashMap<String, Object> relationProperties, String operation) {
        DBConnection dbConnection = getConnection("changes");
        Document document = new Document("_id", new ObjectId());
        document.append("document_type", "relation");
        document.append("operation", operation);
        document.append("time", clock);
        for (Map.Entry<String, Object> property : relationProperties.entrySet()) {
            document.append(property.getKey(), property.getValue());
        }
        dbConnection.save(document);
    }

}
