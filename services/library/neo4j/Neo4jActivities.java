package services.library.neo4j;

import org.bson.types.ObjectId;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import services.db.Neo4jDbConnection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Neo4jActivities {

    public static Node store(ObjectId id,String name, String collection, String type) throws IOException {
        type = type == null ? "" : type;
        HashMap<String,String> properties = new HashMap<>();
        properties.put("identifier",id.toString());
        properties.put("name",name);
        properties.put("collection",collection);
        properties.put("type",type);
        return (new Neo4jDbConnection()).storeNode(properties);
    }

    public static Relationship relationship(String name,Node node1,Node node2) throws IOException {
        HashMap<String,String> properties = new HashMap<>();
        properties.put("name",name);
        return (new Neo4jDbConnection()).relating(node1,node2,properties);
    }

    public static void destroy(Node node){
        (new Neo4jDbConnection()).removeNodeAndRelations(node);
    }

    public static long findIdByIdentifier(ObjectId id){
        return (new Neo4jDbConnection()).getNodeIdBySearchOnIdentifier(id.toString());
    }

    public static Node getById(long id){
        return (new Neo4jDbConnection()).findNodeById(id);
    }

    public static Node storeOrGetNode(ObjectId id, String name, String collection, String type) throws IOException {
        long originNodeId = Neo4jActivities.findIdByIdentifier(id);
        if (originNodeId == -1){
            return Neo4jActivities.store(id,name,collection,type);
        }
        else {
            return Neo4jActivities.getById(originNodeId);
        }
    }

    public static Map<String, Object> getNodeProperties(Node node){
        return (new Neo4jDbConnection()).getNodeProperties(node);
    }

    public static ArrayList<Long> getNodeRelationships(Node node){
        return (new Neo4jDbConnection()).getNodeRelationships(node);
    }

    public static Node getRelationshipEndNode(long relationshipId){
        return (new Neo4jDbConnection()).getRelationshipEndNode(relationshipId);
    }

    public static Map<String, Object> getRelationshipProperties(long relationshipId){
        return (new Neo4jDbConnection()).getRelationshipProperties(relationshipId);
    }
}
