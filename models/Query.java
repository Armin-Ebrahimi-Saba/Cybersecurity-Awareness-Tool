package models;

import org.bson.Document;
import org.bson.types.ObjectId;
import services.db.DBConnection;

import java.util.ArrayList;

import static com.mongodb.client.model.Filters.eq;

public class Query extends Base{
    public final static String COLLECTION = "queries";

    public String query;

    public Query(String query){
        this.query = query;
    }

    public Query(String query,ObjectId _id){
        this.query = query;
        this._id = _id;
    }

    @Override
    public void save() {
        DBConnection dbConnection = getConnection(COLLECTION);
        Document document = new Document("query",query);
        saveOrUpdate(dbConnection,document);
    }

    @Override
    public String getCollection() {
        return COLLECTION;
    }

    protected static Query instance(Document document){
        return document != null ? new Query(
                document.getString("query"),
                (ObjectId)document.get("_id")
        ) : null;
    }

    public static ArrayList<Query> getAllQueries(){
        ArrayList<Document> docs = getConnection(COLLECTION).getAll();
        ArrayList<Query> queries = new ArrayList<>();
        for (Document doc: docs) {
            queries.add(instance(doc));
        }
        return queries;
    }

    public static Query findByQuery(String query){
        return instance(
                getConnection(COLLECTION).find(
                        eq("query",query)
                )
        );
    }
}
