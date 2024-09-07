package models.vulnerabilities;

import org.bson.Document;
import org.bson.types.ObjectId;
import services.db.DBConnection;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class CPE extends SubVulnerability{
    public static final String TYPE = "CPE";

    public String cpe23Uri;
    public boolean vulnerable;

    public CPE(String title,String cpe23Uri,boolean vulnerable,ObjectId cve_id){
        this.type = TYPE;
        this.title = title;
        this.cpe23Uri = cpe23Uri;
        this.vulnerable = vulnerable;
        this.description = null;
        this.cve_id = cve_id;
    }

    public CPE(String title, String cpe23Uri, boolean vulnerable, ObjectId _id,ObjectId cve_id){
        this.type = TYPE;
        this.title = title;
        this.cpe23Uri = cpe23Uri;
        this.vulnerable = vulnerable;
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
                        .append("cpe23Uri",this.cpe23Uri)
                        .append("vulnerable",this.vulnerable)
                        .append("cve_id",this.cve_id)
        );
    }

    protected static CPE instance(Document document){
        return document != null ? new CPE(
                (String)document.get("title"),
                (String) document.get("cpe23Uri"),
                (boolean) document.get("vulnerable"),
                (ObjectId)document.get("_id"),
                (ObjectId)document.get("cve_id")
        ) : null;
    }

    public static CPE findCveCpeByCpe23UriAndCveId(String cpe23Uri,ObjectId cve_id){
        return instance(
                (new DBConnection(COLLECTION)).find(
                        and(
                                eq("cve_id",cve_id),
                                eq("type",TYPE),
                                eq("cpe23Uri",cpe23Uri)
                        )
                )
        );
    }
}
