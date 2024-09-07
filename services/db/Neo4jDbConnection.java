package services.db;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.util.*;

import models.Base;
import models.Relation;
import org.bson.types.ObjectId;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.*;
import services.adapters.GetNodeLabel;

import static org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;

public class Neo4jDbConnection {
    private static final Path databaseDirectory = Path.of( "target/neo4j-CyGraph-db" );

    // tag::vars[]
    protected GraphDatabaseService graphDb;
    protected DatabaseManagementService managementService;
    // end::vars[]

    // tag::createReltype[]
    private enum RelTypes implements RelationshipType {
        KNOWS
    }
    // end::createReltype[]

    public Neo4jDbConnection(){
        // tag::startDb[]
        this.managementService = new DatabaseManagementServiceBuilder( databaseDirectory ).build();
        this.graphDb = this.managementService.database( DEFAULT_DATABASE_NAME );
        registerShutdownHook( this.managementService );
        // end::startDb[]
    }

    // tag::shutdownHook[]
    private static void registerShutdownHook( final DatabaseManagementService managementService ) {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                managementService.shutdown();
            }
        });
    }
    // end::shutdownHook[]

    public void shutDown() {
        // tag::shutdownServer[]
        managementService.shutdown();
        // end::shutdownServer[]
    }

    public Node findNodeById(long id){
        Node node;
        if (id == -1)
            return null;
        try ( Transaction tx = this.graphDb.beginTx() )
        {
            node =  tx.getNodeById(id);
            tx.commit();
//            shutDown();
        }
        return node;
    }

    public long getNodeIdBySearchOnIdentifier(String identifier){
        long res = -1;
        try ( Transaction tx = this.graphDb.beginTx();
              Result result = tx.execute( "MATCH (n {identifier: '"+identifier+"'}) RETURN ID(n)" ))
        {
            while ( result.hasNext() )
            {
                Map<String,Object> row = result.next();
                for ( Map.Entry<String,Object> column : row.entrySet() )
                {
                    res = (long) column.getValue();
                }
            }
            tx.commit();
//            shutDown();
        }
        return res;
    }

    public Node getNodeBySearchOnIdentifier(String identifier) {
        return findNodeById(getNodeIdBySearchOnIdentifier(identifier));
    }

    public Map<String, Object> getNodeProperties(Node node){
        Map<String, Object> props;
        try ( Transaction tx = this.graphDb.beginTx() )
        {
            props = tx.getNodeById(node.getId()).getAllProperties();
            tx.commit();
            shutDown();
        }
        return props;
    }

    public ArrayList<Long> getNodeRelationships(Node node){
        Iterator<Relationship> relationships;
        ArrayList<Long> result = new ArrayList<>();
        try ( Transaction tx = this.graphDb.beginTx() )
        {
            relationships =  tx.getNodeById(node.getId()).getRelationships().iterator();
            while (relationships.hasNext()){
                result.add(relationships.next().getId());
            }
            tx.commit();
            shutDown();
        }
        return result;
    }

    public Node getRelationshipEndNode(long relationshipId){
        Node endNode;
        try ( Transaction tx = this.graphDb.beginTx() )
        {
            endNode =  tx.getRelationshipById(relationshipId).getEndNode();
            tx.commit();
            shutDown();
        }
        return endNode;
    }

    public Map<String, Object> getRelationshipProperties(long relationshipId){
        Map<String, Object> props;

        try ( Transaction tx = this.graphDb.beginTx() )
        {
            props = tx.getRelationshipById(relationshipId).getAllProperties();
            tx.commit();
            shutDown();
        }
        return props;
    }

    protected Node getNodeById(long id,Transaction tx){
        return  tx.getNodeById(id);
    }

    public void removeNodeAndRelations(Node node) {

        try ( Transaction tx = this.graphDb.beginTx() ) {
            // tag::removingData[]
            // let's remove the data
            node = tx.getNodeById( node.getId() );
            for (Relationship rel : node.getRelationships()) {
                rel.delete();
            }
            node.delete();
            // end::removingData[]
            tx.commit();
//            shutDown();
        }
    }

    public void removeNodeAndRelationsAndRegister(Base new_node) {
        Node node = findNodeById(getNodeIdBySearchOnIdentifier(String.valueOf(new_node._id)));
        if (node != null) {

            long clock = Clock.systemUTC().millis();
            try (Transaction tx = this.graphDb.beginTx()) {

                node = tx.getNodeById( node.getId() );
                ArrayList <HashMap <String, Object>> relations = new ArrayList<>();
                for (Relationship rel : node.getRelationships()) {

                    HashMap<String, Object> properties = new HashMap<>();
                    properties.put("relationName", rel.getAllProperties().get("name"));
                    properties.put("startNode", rel.getStartNode().getAllProperties().get("identifier"));
                    properties.put("startNodeName", rel.getStartNode().getAllProperties().get("name"));
                    properties.put("endNode", rel.getEndNode().getAllProperties().get("identifier"));
                    properties.put("endNodeName", rel.getEndNode().getAllProperties().get("name"));
                    rel.delete();
                    relations.add(properties);
                }
                node.delete();
                new_node.deleteNode(clock, relations);
                // end::removingData[]
                tx.commit();
//            shutDown();
            }
        }

    }

    public void removeRelationship(String startNode_id, String endNode_id, String relationName) {

        try ( Transaction tx = this.graphDb.beginTx() ) {
            Node startNode = findNodeById(getNodeIdBySearchOnIdentifier(startNode_id));
            Node endNode = findNodeById(getNodeIdBySearchOnIdentifier(endNode_id));
            startNode = tx.getNodeById(startNode.getId());
            endNode = tx.getNodeById(endNode.getId());
            for (Relationship rel : startNode.getRelationships(RelationshipType.withName(relationName.toUpperCase()))) {
                if (rel.getAllProperties().get("name").equals(relationName) && rel.getEndNode().equals(endNode))
                    rel.delete();
            }
            tx.commit();
//            shutDown();
        }
    }

    public void removeRelationshipAndRegister(Node startNode, Node endNode, String relationName) {

        try ( Transaction tx = this.graphDb.beginTx() ) {
            startNode = tx.getNodeById( startNode.getId() );
            long clock = Clock.systemUTC().millis();
            for (Relationship rel : startNode.getRelationships(RelationshipType.withName(relationName.toUpperCase()))) {

                if (rel.getAllProperties().get("name").equals(relationName) && rel.getEndNode().equals(endNode)) {
                    HashMap<String, Object> properties = new HashMap<>();
                    properties.put("relationName", rel.getAllProperties().get("name"));
                    properties.put("startNode", rel.getStartNode().getAllProperties().get("identifier"));
                    properties.put("startNodeName", rel.getStartNode().getAllProperties().get("name"));
                    properties.put("endNode", rel.getEndNode().getAllProperties().get("identifier"));
                    properties.put("endNodeName", rel.getEndNode().getAllProperties().get("name"));
                    rel.delete();
                    (new Relation()).changeRelation(clock, properties, "delete");
                }
            }
            tx.commit();
//            shutDown();
        }
    }

    protected static Relationship relationship(String name,Node node1,Node node2,Transaction tx) throws IOException {
        HashMap<String,String> properties = new HashMap<>();
        properties.put("name",name);

        node1 = tx.getNodeById(node1.getId());
        node2 = tx.getNodeById(node2.getId());
        Relationship relationship = node1.createRelationshipTo(node2,RelationshipType.withName(name.toUpperCase()));
        for (Map.Entry<String, String> property : properties.entrySet()) {
            relationship.setProperty(property.getKey(),property.getValue());
        }
        return relationship;
    }

    public Relationship relating(Node node1,Node node2,HashMap<String,String> properties) throws IOException{
        Relationship relationship;
        // tag::transaction[]
        try ( Transaction tx = this.graphDb.beginTx() )
        {
            node1 = tx.getNodeById(node1.getId());
            node2 = tx.getNodeById(node2.getId());
            relationship = node1.createRelationshipTo(node2,RelationshipType.withName(properties.get("name").toUpperCase()));
            for (Map.Entry<String, String> property : properties.entrySet()) {
                relationship.setProperty(property.getKey(),property.getValue());
            }
            tx.commit();
            shutDown();
        }
        return relationship;
    }

    public Relationship relating(String startNode_id, String endNode_id, String name) throws IOException{
        Relationship relationship;
        // tag::transaction[]
        try ( Transaction tx = this.graphDb.beginTx() )
        {
            Node node1 = findNodeById(getNodeIdBySearchOnIdentifier(startNode_id));
            Node node2 = findNodeById(getNodeIdBySearchOnIdentifier(endNode_id));
            node1 = tx.getNodeById(node1.getId());
            node2 = tx.getNodeById(node2.getId());
            relationship = node1.createRelationshipTo(node2,RelationshipType.withName(name.toUpperCase()));
            relationship.setProperty("name",name);
            tx.commit();
//            shutDown();
        }
        return relationship;
    }

    public Relationship relatingAndRegister(Node node1,Node node2,HashMap<String,String> properties) throws IOException{
        Relationship relationship;
        long clock = Clock.systemUTC().millis();
        // tag::transaction[]
        try ( Transaction tx = this.graphDb.beginTx() )
        {
            node1 = tx.getNodeById(node1.getId());
            node2 = tx.getNodeById(node2.getId());
            relationship = node1.createRelationshipTo(node2,RelationshipType.withName(properties.get("name").toUpperCase()));
            for (Map.Entry<String, String> property : properties.entrySet()) {
                relationship.setProperty(property.getKey(),property.getValue());
            }
            HashMap<String, Object> relationProperties = new HashMap<>();
            relationProperties.put("relationName", relationship.getAllProperties().get("name"));
            relationProperties.put("startNode", relationship.getStartNode().getAllProperties().get("identifier"));
            relationProperties.put("startNodeName", relationship.getStartNode().getAllProperties().get("name"));
            relationProperties.put("endNode", relationship.getEndNode().getAllProperties().get("identifier"));
            relationProperties.put("endNodeName", relationship.getEndNode().getAllProperties().get("name"));
            (new Relation()).changeRelation(clock, relationProperties,"add");
            tx.commit();
//            shutDown();
        }
        return relationship;
    }

    public Node storeNode(HashMap<String,String> properties) throws IOException{
        Node node = null;
        // tag::transaction[]
        try ( Transaction tx = this.graphDb.beginTx() ) {
            // end::transaction[]
            // tag::addData[]
            node = tx.createNode();

            for (Map.Entry<String, String> property : properties.entrySet()) {
                node.setProperty(property.getKey(),property.getValue());
            }
            // tag::transaction[]
            tx.commit();
            shutDown();
        }
        return node;
    }

    public Node storeOrGetNode(ObjectId id, String name, String collection, String type) {

        Node node = findNodeById(getNodeIdBySearchOnIdentifier(String.valueOf(id)));
        // tag::transaction[]
        try ( Transaction tx = this.graphDb.beginTx() ) {
            // end::transaction[]
            // tag::addData[]
            if (node != null)
                return node;
            else {

                node = tx.createNode();

                node.setProperty("identifier",String.valueOf(id));
                node.setProperty("name",name);
                node.setProperty("collection",collection);
                node.setProperty("type",type);

                tx.commit();
//                shutDown();
                return node;
            }
            // tag::transaction[]
//            tx.commit();
        }
    }

    public Node storeOrGetNode(ObjectId id, String name, String collection,Transaction tx, String type) {
        long originNodeId = -1;
        Result result = tx.execute( "MATCH (n {identifier: '"+id.toString()+"'}) RETURN ID(n)" );
        while ( result.hasNext() )
        {
            Map<String,Object> row = result.next();
            for ( Map.Entry<String,Object> column : row.entrySet() )
            {
                originNodeId = (long) column.getValue();
            }
        }

        System.out.println(originNodeId);
        if (originNodeId == -1){
            type = type == null ? "" : type;
            HashMap<String,String> properties = new HashMap<>();
            properties.put("identifier",id.toString());
            properties.put("name",name);
            properties.put("collection",collection);
            properties.put("type",type);
            Node node;
            if (!type.isEmpty() && !type.isBlank()){
                node = tx.createNode(
                        Label.label(GetNodeLabel.getLabel(collection)),
                        Label.label(type.toUpperCase())
                );
            }
            else {
                node = tx.createNode(
                        Label.label(GetNodeLabel.getLabel(collection))
                );
            }
            for (Map.Entry<String, String> property : properties.entrySet()) {
                node.setProperty(property.getKey(),property.getValue());
            }
            return node;
        }
        else {
            return tx.getNodeById(originNodeId);
        }
    }

    public Node storeNodeAndRegister(Base new_node, String startNodeName, Node node1, HashMap<String,String> relationProperties) throws IOException{
//        Node node = getNode(String.valueOf(new_node._id));
        Node node = findNodeById(getNodeIdBySearchOnIdentifier(String.valueOf(new_node._id)));
        // tag::transaction[]
        try ( Transaction tx = this.graphDb.beginTx() ) {
            // end::transaction[]
            // tag::addData[]
            if (node != null)
                return node;
            else {
                long clock = Clock.systemUTC().millis();
                HashMap<String, Object> properties = new_node.getNodeProproperties(clock);
                node = tx.createNode();
                for (Map.Entry<String, Object> property : properties.entrySet()) {
                    node.setProperty(property.getKey(), String.valueOf(property.getValue()));
                }
                Node startNode;
                Node endNode;
                if (startNodeName.equals(new_node.title)) {
                    startNode = tx.getNodeById(node.getId());
                    endNode = tx.getNodeById(node1.getId());
                }
                else {
                    startNode = tx.getNodeById(node1.getId());
                    endNode = tx.getNodeById(node.getId());
                }
                Relationship relationship = startNode.createRelationshipTo(endNode, RelationshipType.withName(relationProperties.get("name").toUpperCase()));
                for (Map.Entry<String, String> relationProperty : relationProperties.entrySet()) {
                    relationship.setProperty(relationProperty.getKey(), relationProperty.getValue());
                }

                HashMap<String, Object> relation = new HashMap<>();
                relation.put("relationName", relationship.getAllProperties().get("name"));
                relation.put("startNode", relationship.getStartNode().getAllProperties().get("identifier"));
                relation.put("startNodeName", relationship.getStartNode().getAllProperties().get("name"));
                relation.put("endNode", relationship.getEndNode().getAllProperties().get("identifier"));
                relation.put("endNodeName", relationship.getEndNode().getAllProperties().get("name"));
                new_node.addNode(clock,relation);

                tx.commit();
//                shutDown();
                return node;
            }
            // tag::transaction[]
//            tx.commit();
        }
    }

}