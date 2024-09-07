package models;

import org.bson.Document;
import org.bson.types.ObjectId;
import services.db.DBConnection;

import java.util.ArrayList;

public class Snort extends Node{

    public final static String COLLECTION = "snorts";

    public String id;

    public Snort(String id){
        this.id = id;
        this.title = "snort_"+id;
        this.description = null;
    }

    public Snort(String id, String title, String description, ObjectId _id){
        this.id = id;
        this.title = title;
        this.description = description;
        this._id = _id;
    }

    protected static Snort instance(Document document){
        return document != null ? new Snort(
                (String)document.get("id"),
                (String)document.get("title"),
                (String)document.get("description"),
                (ObjectId)document.get("_id")
        ) : null;
    }

    public static Snort findById(String id){
        return instance(
                findByIdAndDbCon(
                        new DBConnection(COLLECTION),
                        id
                )
        );
    }

    @Override
    public void save() {
        DBConnection dbConnection = getConnection(COLLECTION);
        Document document = new Document("id",this.id)
                .append("title",this.title)
                .append("description",this.description);
        saveOrUpdate(dbConnection,document);
    }

    public static Snort findByMainId(ObjectId _id) {
        Document document = findByMainIdAndDbCon(
                new DBConnection(COLLECTION),
                _id
        );

        if (document == null){
            return null;
        }
        return instance(document);
    }

    @Override
    public String getCollection() {
        return COLLECTION;
    }

    public static ArrayList<Snort> getAllSnorts(){
        ArrayList<Document> docs = getAll(new DBConnection(COLLECTION));
        ArrayList<Snort> snorts = new ArrayList<>();
        for (Document doc: docs) {
            snorts.add(instance(doc));
        }
        return snorts;
    }
}
