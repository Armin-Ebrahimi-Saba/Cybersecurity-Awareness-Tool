package models.missions;

import org.bson.Document;
import org.bson.types.ObjectId;
import services.db.DBConnection;
import static com.mongodb.client.model.Filters.eq;

public class CyberAsset extends MissionBase
{
    public final static String COLLECTION = "cyber_assets";

    public CyberAsset(String title, String description, String nodeImpact, String relativeWeight) {
        super(title, description, nodeImpact, relativeWeight);
    }

    public CyberAsset(String title, String description, String nodeImpact, String relativeWeight, ObjectId _id) {
        super(title, description, nodeImpact, relativeWeight, _id);
    }

    protected static CyberAsset instance(Document document){
        return document != null ? new CyberAsset(
                document.getString("title"),
                document.getString("description"),
                document.getString("nodeImpact"),
                document.getString("relativeWeight"),
                document.getObjectId("_id")
        ) : null;
    }

    @Override
    public String getCollection() {
        return COLLECTION;
    }

    public static CyberAsset findByTitle(String title){
        return instance(
                (new DBConnection(COLLECTION)).find(
                        eq("title",title)
                )
        );
    }

    public static CyberAsset findByMainId(ObjectId _id) {
        Document document = findByMainIdAndDbCon(
                new DBConnection(COLLECTION),
                _id
        );

        if (document == null){
            return null;
        }
        return instance(document);
    }

}
