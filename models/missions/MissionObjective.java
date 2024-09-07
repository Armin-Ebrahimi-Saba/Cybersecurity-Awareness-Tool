package models.missions;

import org.bson.Document;
import org.bson.types.ObjectId;
import services.db.DBConnection;

import static com.mongodb.client.model.Filters.eq;

public class MissionObjective extends MissionBase
{
    public final static String COLLECTION = "mission_objectives";

    public MissionObjective(String title, String description, String nodeImpact, String relativeWeight) {
        super(title, description, nodeImpact, relativeWeight);
    }

    public MissionObjective(String title, String description, String nodeImpact, String relativeWeight, ObjectId _id) {
        super(title, description, nodeImpact, relativeWeight, _id);
    }

    protected static MissionObjective instance(Document document){
        return document != null ? new MissionObjective(
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

    public static MissionObjective findByTitle(String title){
        return instance(
                (new DBConnection(COLLECTION)).find(
                        eq("title",title)
                )
        );
    }

    public static MissionObjective findByMainId(ObjectId _id) {
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
