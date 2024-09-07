package models.vulnerabilities;

import org.bson.Document;
import org.bson.types.ObjectId;
import services.db.DBConnection;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class Metric extends SubVulnerability{

    public static final String TYPE = "Metric";

    public double exploitabilityScore;
    public double impactScore;

    public Metric(String title,double exploitabilityScore,double impactScore,ObjectId cve_id){
        this.type = TYPE;
        this.title = title;// baseMetricV2 or baseMetricV3
        this.description = null;
        this.exploitabilityScore = exploitabilityScore;
        this.impactScore = impactScore;
        this.cve_id = cve_id;
    }

    public Metric(String title, double exploitabilityScore, double impactScore, ObjectId _id,ObjectId cve_id){
        this.type = TYPE;
        this.exploitabilityScore = exploitabilityScore;
        this.impactScore = impactScore;
        this.title = title;// baseMetricV2 or baseMetricV3
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
                        .append("exploitabilityScore",this.exploitabilityScore)
                        .append("impactScore",this.impactScore)
                        .append("cve_id",this.cve_id)
        );
    }

    protected static Metric instance(Document document){
        return document != null ? new Metric(
                (String)document.get("title"),
                (double) document.get("exploitabilityScore"),
                (double) document.get("impactScore"),
                (ObjectId)document.get("_id"),
                (ObjectId)document.get("cve_id")
        ) : null;
    }

    public static Metric findCvssMetricByTitleAndCveId(String title,ObjectId cve_id){
        return instance(
                (new DBConnection(COLLECTION)).find(
                        and(
                                eq("title",title),
                                eq("type",TYPE),
                                eq("cve_id",cve_id)
                        )
                )
        );
    }

}
