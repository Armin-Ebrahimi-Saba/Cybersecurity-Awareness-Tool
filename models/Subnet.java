package models;

import org.bson.Document;
import org.bson.types.ObjectId;
import services.db.DBConnection;

public class Subnet extends Node{

    public final static String COLLECTION = "subnets";

    public String id;

    public Subnet(String id, String title) {
        this.id = id;
        this.title = title;
        this.description = "";
    }

    public Subnet(String id, String title, String description, ObjectId _id) {
        this.id = id;
        this.title = title;
        this.description = description;
        this._id = _id;
    }

    @Override
    public void save() {
        DBConnection dbConnection = getConnection(COLLECTION);
        Document document = new Document("id",this.id)
                .append("title",this.title)
                .append("description",this.description);
        saveOrUpdate(dbConnection,document);
    }

    protected static Subnet instance(Document document){
        return document != null ? new Subnet(
                (String)document.get("id"),
                (String)document.get("title"),
                (String)document.get("description"),
                (ObjectId)document.get("_id")
        ) : null;
    }

    public static Subnet findById(String id){
        return instance(
                findByIdAndDbCon(
                        new DBConnection(COLLECTION),
                        id
                )
        );
    }

    public static Subnet findByMainId(ObjectId _id) {
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
