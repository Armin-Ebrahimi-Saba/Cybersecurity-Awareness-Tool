package services.library.cygraph;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.neo4j.graphdb.Node;
import services.db.Neo4jDbConnection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;

public class GraphChange {

    private TreeMap<Long, ArrayList<Document>> changes;

    public GraphChange(TreeMap<Long, ArrayList<Document>> changes) {
        this.changes = changes;
    }

    public void applyChanges(ArrayList<Long> subarray) {

        Neo4jDbConnection connection = new Neo4jDbConnection();
        for(long time: subarray) {
            ArrayList<Document> temp = new ArrayList();
            ArrayList<Long> clocks = new ArrayList();
            for (Document document : changes.get(time))
                clocks.add((Long) document.get("time"));
            Collections.sort(clocks);
            for (int i = clocks.size() - 1; i >= 0; i--) {
                for (Document document : changes.get(time)) {
                    if (document.get("time").equals(clocks.get(i)))
                        temp.add(document);
                }
            }

            for(Document document: temp) {
                if (document.get("document_type").equals("node")) {
                    if (document.get("operation").equals("add")) {
                        Node node = connection.storeOrGetNode((ObjectId) document.get("identifier"), (String) document.get("name"),
                                (String) document.get("collection"), (String) document.get("type"));
                        connection.removeNodeAndRelations(node);
                    }
                    if (document.get("operation").equals("delete")) {
                        connection.storeOrGetNode((ObjectId) document.get("identifier"), (String) document.get("name"),
                                (String) document.get("collection"), (String) document.get("type"));
                        for (Document relation: (ArrayList <Document>) document.get("relations")) {
                            try {
                                connection.relating((String) relation.get("startNode"), (String) relation.get("endNode"), (String) relation.get("relationName"));
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
                if (document.get("document_type").equals("relation")) {
                    if (document.get("operation").equals("add")) {
                        connection.removeRelationship((String) document.get("startNode"), (String) document.get("endNode"), (String) document.get("relationName"));
                    }
                    if (document.get("operation").equals("delete")) {
                        try {
                            connection.relating((String) document.get("startNode"), (String) document.get("endNode"), (String) document.get("relationName"));
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
        connection.shutDown();
    }

    public void undoChanges(ArrayList<Long> arraysub) {

        Neo4jDbConnection dbConnection = new Neo4jDbConnection();
        for(long time: arraysub) {

            ArrayList<Document> temp = new ArrayList();
            ArrayList<Long> clocks = new ArrayList();
            for (Document document : changes.get(time))
                clocks.add((Long) document.get("time"));
            Collections.sort(clocks);
            for (int i = 0; i < clocks.size(); i++) {
                for (Document document : changes.get(time)) {
                    if (document.get("time").equals(clocks.get(i)))
                        temp.add(document);
                }
            }
            for(Document document: temp) {
                if (document.get("document_type").equals("node")) {
                    if (document.get("operation").equals("add")) {
                        dbConnection.storeOrGetNode((ObjectId) document.get("identifier"), (String) document.get("name"),
                                (String) document.get("collection"), (String) document.get("type"));
                        Document relation = (Document) document.get("relation");
                        try {
                            dbConnection.relating((String) relation.get("startNode"), (String) relation.get("endNode"), (String) relation.get("relationName"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (document.get("operation").equals("delete")) {
                        Node node = dbConnection.storeOrGetNode((ObjectId) document.get("identifier"), (String) document.get("name"),
                                (String) document.get("collection"), (String) document.get("type"));
                        for (Document relation: (ArrayList <Document>) document.get("relations")) {
                            dbConnection.removeRelationship((String) relation.get("startNode"), (String) relation.get("endNode"), (String) relation.get("relationName"));
                        }
                        dbConnection.removeNodeAndRelations(node);
                    }
                }
                if (document.get("document_type").equals("relation")) {
                    if (document.get("operation").equals("add")) {
                        try {
                            dbConnection.relating((String) document.get("startNode"), (String) document.get("endNode"), (String) document.get("relationName"));
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                    if (document.get("operation").equals("delete")) {
                        dbConnection.removeRelationship((String) document.get("startNode"), (String) document.get("endNode"), (String) document.get("relationName"));
                    }
                }
            }
        }
        dbConnection.shutDown();
    }

}
