package models;

import org.bson.Document;
import org.bson.types.ObjectId;
import services.db.DBConnection;

public class AttackPattern extends Node{

    public final static String COLLECTION = "attack_patterns";

    public String id;
    public String status;
    public String likelihoodAttack;
    public String severity;
    public String taxonomyMappings;


    public AttackPattern(String id, String title, String description,String likelihoodAttack,String severity, String taxonomyMappings, String status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.likelihoodAttack = likelihoodAttack;
        this.severity = severity;
        this.taxonomyMappings = taxonomyMappings;
        this.status = status;
    }

    public AttackPattern(String id, String title, String description,String likelihoodAttack,String severity, String taxonomyMappings, String status, ObjectId _id) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.likelihoodAttack = likelihoodAttack;
        this.severity = severity;
        this.taxonomyMappings = taxonomyMappings;
        this.status = status;
        this._id = _id;
    }

    @Override
    public void save() {
        DBConnection dbConnection = getConnection(COLLECTION);
        Document document = new Document("id",this.id)
                .append("title",this.title)
                .append("description",this.description)
                .append("likelihoodAttack",this.likelihoodAttack)
                .append("severity",this.severity)
                .append("taxonomyMappings",this.taxonomyMappings)
                .append("status",this.status);
        saveOrUpdate(dbConnection,document);
    }

    protected static AttackPattern instance(Document document){
        return document != null ? new AttackPattern(
                (String)document.get("id"),
                (String)document.get("title"),
                (String)document.get("description"),
                document.getString("likelihoodAttack"),
                document.getString("severity"),
                document.getString("taxonomyMappings"),
                document.getString("status"),
                (ObjectId)document.get("_id")
        ) : null;
    }

    public static AttackPattern findById(String id){
        return instance(
                findByIdAndDbCon(
                        new DBConnection(COLLECTION),
                        id
                )
        );
    }

    public static AttackPattern findByMainId(ObjectId _id) {
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
