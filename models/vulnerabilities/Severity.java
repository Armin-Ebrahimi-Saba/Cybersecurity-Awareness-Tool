package models.vulnerabilities;

import org.bson.Document;
import org.bson.types.ObjectId;
import services.db.DBConnection;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class Severity extends SubVulnerability{

    public static final String TYPE = "Severity";

    public String severity;

    public Severity(String title,String severity,ObjectId cve_id){
        this.type = TYPE;
        this.severity = severity;
        this.title = title; // cvssv2 or cvssv3
        this.description = null;
        this.cve_id = cve_id;
    }

    public Severity(String title,String severity, ObjectId _id,ObjectId cve_id){
        this.type = TYPE;
        this.severity = severity;
        this.title = title; // cvssv2 or cvssv3
        this.description = null;
        this._id = _id;
        this.cve_id = cve_id;
    }

    @Override
    public void save() {
        saveOrUpdate(
                getConnection(COLLECTION),
                new Document("type",this.type)
                        .append("title",this.title)
                        .append("description",this.description)
                        .append("severity",this.severity)
                        .append("cve_id",this.cve_id)
        );
    }

    protected static Severity instance(Document document){
        return document != null ? new Severity(
                (String)document.get("title"),
                (String)document.get("severity"),
                (ObjectId)document.get("_id"),
                (ObjectId)document.get("cve_id")
        ) : null;
    }

    public static Severity findByTitleAndCveId(String title,ObjectId cve_id){
        return instance(
                (new DBConnection(COLLECTION)).find(
                        and(
                                eq("cve_id",cve_id),
                                eq("type",TYPE),
                                eq("title",title)
                        )
                )
        );
    }

}
