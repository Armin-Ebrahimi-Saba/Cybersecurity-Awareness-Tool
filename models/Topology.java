package models;

import org.bson.Document;
import org.bson.types.ObjectId;
import services.db.DBConnection;

import java.util.ArrayList;

import static com.mongodb.client.model.Filters.*;

public class Topology extends Base{

    public final static String COLLECTION = "topologies";

    public ObjectId origin_id;
    public String origin_type;
    public ObjectId target_id;
    public String target_type;

    public Topology(
            String title,
            ObjectId origin_id,
            String origin_type,
            ObjectId target_id,
            String target_type,
            String description
    ) {
        this.title = title;
        this.origin_id = origin_id;
        this.origin_type = origin_type;
        this.target_id = target_id;
        this.target_type = target_type;
        this.description = description;
    }

    public Topology(
            String title,
            ObjectId origin_id,
            String origin_type,
            ObjectId target_id,
            String target_type,
            String description,
            ObjectId _id
    ) {
        this.title = title;
        this.origin_id = origin_id;
        this.origin_type = origin_type;
        this.target_id = target_id;
        this.target_type = target_type;
        this.description = description;
        this._id = _id;
    }

    @Override
    public void save() {
        DBConnection dbConnection = getConnection(COLLECTION);

        Document document = new Document("title",this.title)
                .append("origin_id",this.origin_id)
                .append("origin_type",this.origin_type)
                .append("target_id",this.target_id)
                .append("target_type",this.target_type)
                .append("description",this.description);
        saveOrUpdate(dbConnection,document);
    }

    public static Topology findByOriginAndTarget(
            ObjectId origin_id,
            String origin_type,
            ObjectId target_id,
            String target_type
    ){
        return instance(
                getConnection(COLLECTION).find(
                        or(
                                and(
                                        eq("origin_id",origin_id),
                                        eq("origin_type",origin_type),
                                        eq("target_id",target_id),
                                        eq("target_type",target_type)
                                ),
                                and(
                                        eq("origin_id",target_id),
                                        eq("origin_type",target_type),
                                        eq("target_id",origin_id),
                                        eq("target_type",origin_type)
                                )
                        )
                )
        );
    }

    public static ArrayList<Topology> findRelationsByMainId(ObjectId origin_id, String origin_type){
        ArrayList<Document> relations = getConnection(COLLECTION).get(
                or(
                        and(
                                eq("origin_id",origin_id),
                                eq("origin_type",origin_type)
                        ),
                        and(
                                eq("target_id",origin_id),
                                eq("target_type",origin_type)
                        )
                )
        );
        ArrayList<Topology> topologies = new ArrayList<>();
        for (Document relation : relations) {
            topologies.add(instance(relation));
        }
        return topologies;
    }

    protected static Topology instance(Document document){
        return document != null ? new Topology(
                (String)document.get("title"),
                (ObjectId) document.get("origin_id"),
                (String)document.get("origin_type"),
                (ObjectId)document.get("target_id"),
                (String)document.get("target_type"),
                (String)document.get("description"),
                (ObjectId)document.get("_id")
        ) : null;
    }

    public static Topology findByMainId(ObjectId _id) {
        Document document = findByMainIdAndDbCon(
                new DBConnection(COLLECTION),
                _id
        );

        if (document == null){
            return null;
        }
        return instance(document);
    }

    public String getCollection(){
        return COLLECTION;
    }
}
