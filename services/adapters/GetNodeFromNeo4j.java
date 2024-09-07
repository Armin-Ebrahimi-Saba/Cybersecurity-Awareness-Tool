package services.adapters;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import services.db.Neo4jDbConnection;

public class GetNodeFromNeo4j extends Neo4jDbConnection {

    public String identifier;
    public String collection;
    public String type;

    public GetNodeFromNeo4j(long id) {
        super();
        try( Transaction tx = this.graphDb.beginTx() ){
            Node node = this.getNodeById(id,tx);
            this.identifier = String.valueOf(node.getProperty("identifier"));
            this.collection = String.valueOf(node.getProperty("collection"));
            this.type = String.valueOf(node.getProperty("type"));
            tx.commit();
            shutDown();
        }
    }
}
