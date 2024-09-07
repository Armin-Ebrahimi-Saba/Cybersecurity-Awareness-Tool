package models.missions;

import models.Node;
import org.bson.Document;
import org.bson.types.ObjectId;
import services.db.DBConnection;

abstract public class MissionBase extends Node {

    public String nodeImpact;
    public String relativeWeight;

    public MissionBase(
            String title,
            String description,
            String nodeImpact,
            String relativeWeight
    )
    {
        this.title = title;
        this.description = description;
        this.nodeImpact = nodeImpact;
        this.relativeWeight = relativeWeight;
    }

    public MissionBase(
            String title,
            String description,
            String nodeImpact,
            String relativeWeight,
            ObjectId _id
    )
    {
        this.title = title;
        this.description = description;
        this.nodeImpact = nodeImpact;
        this.relativeWeight = relativeWeight;
        this._id = _id;
    }

    @Override
    public void save() {
        DBConnection dbConnection = getConnection(this.getCollection());
        saveOrUpdate(
                dbConnection,
                new Document("title",title)
                        .append("description",description)
                        .append("nodeImpact",nodeImpact)
                        .append("relativeWeight",relativeWeight)
        );
    }
}
