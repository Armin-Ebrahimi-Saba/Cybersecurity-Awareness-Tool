package models;

import org.bson.Document;
import org.bson.types.ObjectId;
import services.db.DBConnection;

public class Machine extends Node{

    public final static String COLLECTION = "machines";

    public String id;
    public String source_ip;
    public String source_port;
    public String destination_ip;
    public String destination_port;
    public String protocol;
    public String stage;

    public Machine(
            String id,
            String title,
            String description,
            String source_ip,
            String source_port,
            String destination_ip,
            String destination_port,
            String protocol,
            String stage
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.source_ip = source_ip;
        this.source_port = source_port;
        this.destination_ip = destination_ip;
        this.destination_port = destination_port;
        this.protocol = protocol;
        this.stage = stage;
    }

    public Machine(
            String id,
            String title,
            String description,
            String source_ip,
            String source_port,
            String destination_ip,
            String destination_port,
            String protocol,
            String stage,
            ObjectId _id
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.source_ip = source_ip;
        this.source_port = source_port;
        this.destination_ip = destination_ip;
        this.destination_port = destination_port;
        this.protocol = protocol;
        this.stage = stage;
        this._id = _id;
    }


    @Override
    public void save() {
        DBConnection dbConnection = getConnection(COLLECTION);
        Document document = new Document("id",this.id)
                .append("title",this.title)
                .append("description",this.description)
                .append("source_ip",this.source_ip)
                .append("source_port",this.source_port)
                .append("destination_ip",this.destination_ip)
                .append("destination_port",this.destination_port)
                .append("protocol",this.protocol)
                .append("stage",this.stage);
        saveOrUpdate(dbConnection,document);
    }

    public static Machine findById(String id){
        return instance(
                findByIdAndDbCon(
                        new DBConnection(COLLECTION),
                        id
                )
        );
    }

    protected static Machine instance(Document document){
        return document != null ? new Machine(
                (String)document.get("id"),
                (String)document.get("title"),
                (String)document.get("description"),
                (String)document.get("source_ip"),
                (String)document.get("source_port"),
                (String)document.get("destination_ip"),
                (String)document.get("destination_port"),
                (String)document.get("protocol"),
                (String)document.get("stage"),
                (ObjectId)document.get("_id")
        ) : null;
    }

    public static Machine findByMainId(ObjectId _id) {
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
