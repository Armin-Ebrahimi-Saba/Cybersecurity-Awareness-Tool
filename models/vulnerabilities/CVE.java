package models.vulnerabilities;

import org.bson.Document;
import org.bson.types.ObjectId;
import services.db.DBConnection;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class CVE extends Vulnerability{

    public static final String TYPE = "CVE";

    public String id;

    public CVE(String id,String title,String description){
        this.type = TYPE;
        this.id = id;
        this.title = title;
        this.description = description;
    }

    public CVE(String id, String title, String description, ObjectId _id){
        this.type = TYPE;
        this.id = id;
        this.title = title;
        this.description = description;
        this._id = _id;
    }

    @Override
    public void save() {
        saveOrUpdate(
                getConnection(COLLECTION),
                new Document("type",this.type)
                        .append("title",this.title)
                        .append("description",this.description)
                        .append("id",this.id)
        );
    }

    protected static CVE instance(Document document){
        return document != null ? new CVE(
                (String)document.get("id"),
                (String)document.get("title"),
                (String) document.get("description"),
                (ObjectId)document.get("_id")
        ) : null;
    }

    public static CVE findById(String id){
        return instance(
                (new DBConnection(COLLECTION)).find(
                        and(
                                eq("id",id),
                                eq("type",TYPE)
                        )
                )
        );
    }

}
