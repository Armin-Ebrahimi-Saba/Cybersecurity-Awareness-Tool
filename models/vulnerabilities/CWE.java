package models.vulnerabilities;

import org.bson.Document;
import org.bson.types.ObjectId;
import services.db.DBConnection;
import java.util.ArrayList;

import static com.mongodb.client.model.Filters.*;

public class CWE extends SubVulnerability{

    public static final String TYPE = "CWE";

    public String id;

    public CWE(String id,String title,String description,ObjectId cve_id){
        this.type = TYPE;
        this.id = id;
        this.title = title;
        this.description = description;
        this.cve_id = cve_id;
    }

    public CWE(String id, String title, String description, ObjectId _id,ObjectId cve_id){
        this.type = TYPE;
        this.id = id;
        this.title = title;
        this.description = description;
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
                        .append("id",this.id)
                        .append("cve_id",this.cve_id)
        );
    }

    protected static CWE instance(Document document){
        return document != null ? new CWE(
                (String)document.get("id"),
                (String)document.get("title"),
                (String) document.get("description"),
                (ObjectId)document.get("_id"),
                (ObjectId)document.get("cve_id")
        ) : null;
    }

    public static CWE findCveCweByCweIdAndCveId(String id,ObjectId cve_id){
        return instance(
                (new DBConnection(COLLECTION)).find(
                        and(
                                eq("id",id),
                                eq("type",TYPE),
                                eq("cve_id",cve_id)
                        )
                )
        );
    }

    public static ArrayList<CWE> getAllById(String id){
        ArrayList<Document> relations = getConnection(COLLECTION).get(
                and(
                        eq("id",id),
                        eq("type",TYPE)
                )
        );

        ArrayList<CWE> cwes = new ArrayList<>();
        for (Document relation : relations) {
            cwes.add(instance(relation));
        }

        return cwes;
    }

}
