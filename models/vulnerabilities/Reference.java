package models.vulnerabilities;

import org.bson.Document;
import org.bson.types.ObjectId;
import services.db.DBConnection;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class Reference extends SubVulnerability{

    public static final String TYPE = "Reference";

    public String url;
    public String refsource;
    public Object tags;

    public Reference(String title,String url,String refsource,Object tags,ObjectId cve_id){
        this.type = TYPE;
        this.title = title;
        this.url = url;
        this.refsource = refsource;
        this.tags = tags;
        this.description = null;
        this.cve_id = cve_id;
    }

    public Reference(String title, String url, String refsource, Object tags, ObjectId _id,ObjectId cve_id){
        this.type = TYPE;
        this.title = title;
        this.url = url;
        this.description = null;
        this.refsource = refsource;
        this.tags = tags;
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
                        .append("url",this.url)
                        .append("refsource",this.refsource)
                        .append("tags",this.tags)
                        .append("cve_id",this.cve_id)
        );
    }

    protected static Reference instance(Document document){
        return document != null ? new Reference(
                (String)document.get("title"),
                (String) document.get("url"),
                (String) document.get("refsource"),
                document.get("tags"),
                (ObjectId)document.get("_id"),
                (ObjectId)document.get("cve_id")
        ) : null;
    }

    public static Reference findCVEReferenceByUrlAndRefsource(String url,String refsource,ObjectId cve_id){
        return instance(
                (new DBConnection(COLLECTION)).find(
                        and(
                                eq("cve_id",cve_id),
                                eq("type",TYPE),
                                eq("url",url),
                                eq("refsource",refsource)
                        )
                )
        );
    }

}
