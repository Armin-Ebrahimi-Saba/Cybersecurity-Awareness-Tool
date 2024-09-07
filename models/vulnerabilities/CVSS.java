package models.vulnerabilities;

import org.bson.Document;
import org.bson.types.ObjectId;
import services.db.DBConnection;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class CVSS extends SubVulnerability{

    public static final String TYPE = "CVSS";

    public String version;
    public String vectorString;
    public String confidentialityImpact;
    public String integrityImpact;
    public String availabilityImpact;
    public double baseScore;

    public CVSS
            (
                    String title,
                    String version,
                    String vectorString,
                    String confidentialityImpact,
                    String integrityImpact,
                    String availabilityImpact,
                    double baseScore,
                    ObjectId cve_id
            )
    {
        this.type = TYPE;
        this.title = title; // cvssV2 or cvssV3
        this.version = version;
        this.confidentialityImpact = confidentialityImpact;
        this.integrityImpact = integrityImpact;
        this.availabilityImpact = availabilityImpact;
        this.baseScore = baseScore;
        this.description = null;
        this.cve_id = cve_id;
        this.vectorString = vectorString;
    }

    public CVSS
            (
                    String title,
                    String version,
                    String vectorString,
                    String confidentialityImpact,
                    String integrityImpact,
                    String availabilityImpact,
                    double baseScore,
                    ObjectId _id,
                    ObjectId cve_id
            )
    {
        this.type = TYPE;
        this.title = title; // cvssV2 or cvssV3
        this.version = version;
        this.confidentialityImpact = confidentialityImpact;
        this.integrityImpact = integrityImpact;
        this.availabilityImpact = availabilityImpact;
        this.baseScore = baseScore;
        this._id = _id;
        this.description = null;
        this.cve_id = cve_id;
        this.vectorString = vectorString;
    }

    @Override
    public void save() {
        saveOrUpdate(
                getConnection(COLLECTION),
                new Document("type",this.type)
                        .append("title",this.title)
                        .append("description",this.description)
                        .append("version",this.version)
                        .append("confidentialityImpact",this.confidentialityImpact)
                        .append("integrityImpact",this.integrityImpact)
                        .append("availabilityImpact",this.availabilityImpact)
                        .append("baseScore",this.baseScore)
                        .append("cve_id",this.cve_id)
                        .append("vectorString",this.vectorString)
        );
    }

    protected static CVSS instance(Document document){
        return document != null ? new CVSS(
                (String)document.get("title"),
                (String) document.get("version"),
                (String) document.get("vectorString"),
                (String) document.get("confidentialityImpact"),
                (String) document.get("integrityImpact"),
                (String) document.get("availabilityImpact"),
                (double) document.get("baseScore"),
                (ObjectId)document.get("_id"),
                (ObjectId)document.get("cve_id")
        ) : null;
    }

    public static CVSS findCveCvssByTitleAndCveId(String title,ObjectId cve_id){
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
