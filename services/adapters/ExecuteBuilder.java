package services.adapters;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import services.db.Neo4jDbConnection;
import services.library.drowTools.NodeHelper;

import java.util.*;

public class ExecuteBuilder extends Neo4jDbConnection{

    private long indexNodeId = -1;
    private ArrayList<Long> node_ids = new ArrayList<>();
    private ArrayList<Long> nodes_helper = new ArrayList<>();

    private Queue<Long> queue;
    private HashMap<Long, NodeHelper> nodeHelpers;
    private ArrayList<Long> seenNodes;
    private ArrayList<Long> drawnRelations;

    private ArrayList<node> nodes;
    private ArrayList<edge> edges;

    public ArrayList<node> getNodes() {
        return nodes;
    }

    public ArrayList<edge> getEdges() {
        return edges;
    }

    public void ExecuteBuilder(String q){

        nodes = new ArrayList<>();
        edges = new ArrayList<>();

        String query = new StringBuffer(q).replace(
                q.lastIndexOf("RETURN"),
                q.length(),
                "RETURN [node in nodes(p) | id(node)]"
        ).toString();

        Transaction tx = this.graphDb.beginTx();
        Result result = tx.execute(query);

        while ( result.hasNext() ) {
            Map<String,Object> row = result.next();
            for ( Map.Entry<String,Object> column : row.entrySet() ) {
                if (column.getValue() instanceof ArrayList){
                    nodes_helper = new ArrayList<>();
                    for (Object id : (ArrayList)column.getValue()) {
                        indexNodeId = (long)id;
                        nodes_helper.add((long)id);
                    }
                    if (nodes_helper.contains(indexNodeId)){
                        for (long id : nodes_helper) {
                            if (!node_ids.contains(id)){
                                node_ids.add(id);
                            }
                        }
                    }
                }
            }
        }

        queue = new ArrayDeque<>();
        nodeHelpers = new HashMap<>();
        seenNodes = new ArrayList<>();
        drawnRelations = new ArrayList<>();
        seenNodes.add(indexNodeId);
        Node parent = tx.getNodeById(indexNodeId);
        initialize(parent);

        drawGraph(tx);

//        tx.commit();
    }

    private void drawGraph(Transaction tx) {
        if (queue.isEmpty()) {
            return;
        }

        long parent_id = queue.poll();
        Node parent = tx.getNodeById(parent_id);
        Map<String, Object> properties = tx.getNodeById(parent.getId()).getAllProperties();

        nodes.add(new node(properties.get("identifier").toString()));

        Iterator<Relationship> relationships =  tx.getNodeById(parent.getId()).getRelationships().iterator();
        ArrayList<Long> relations = new ArrayList<>();
        while (relationships.hasNext()){
            Relationship relationship = relationships.next();
            Node endNode = relationship.getEndNode();
            if (endNode.getId() == parent.getId()){
                endNode =  relationship.getStartNode();
            }
            if (node_ids.contains(endNode.getId())){
                relations.add(relationship.getId());
            }
        }


        ArrayList<Long> skipRelation = new ArrayList<Long>();
        int index = 0;
        for (long relId : relations) {
            if (skipRelation.contains(relId)){
                index++;
                continue;
            }
            // get end node
            Node endNode =  tx.getRelationshipById(relId).getEndNode();
            int relationship_kind = 0;
            if (endNode.getId() == parent.getId()){
                endNode =  tx.getRelationshipById(relId).getStartNode();
                relationship_kind = 1;
            }
            for (long relationId : relations){
                if (relationId != relId){
                    Node twiceEndNode = tx.getRelationshipById(relationId).getEndNode();
                    if (twiceEndNode.getId() == parent.getId()){
                        twiceEndNode = tx.getRelationshipById(relationId).getStartNode();
                    }
                    if (twiceEndNode.getId() == endNode.getId()){
                        relationship_kind = 2;
                        skipRelation.add(relationId);
                        break;
                    }
                }
            }

            if (!checkNodeExists(endNode.getId())){
                seenNodes.add(endNode.getId());
                // push node in queue
                queue.add(endNode.getId());
            }
            else if (!checkDrawnRelationship(relId)){
                // relating nodes
                drawnRelations.add(relId);
                Map<String, Object> relProperties = tx.getRelationshipById(relId).getAllProperties();
                edges.add(new edge(parent.getAllProperties().get("identifier").toString(),
                        endNode.getAllProperties().get("identifier").toString(), relProperties.get("name").toString()));
                if (relationship_kind == 2)
                    edges.add(new edge(endNode.getAllProperties().get("identifier").toString(),
                            parent.getAllProperties().get("identifier").toString(), relProperties.get("name").toString()));
            }
            index++;
        }
        drawGraph(tx);
    }

    private void initialize(Node parent){
        double initialX = 500;
        double initialY = 350;
        nodeHelpers.put(parent.getId(),new NodeHelper(initialX,initialY,null));
        queue.add(parent.getId());
    }

    private boolean checkNodeExists(long id){
        for (long node_id : seenNodes) {
            if (node_id == id) {
                return true;
            }
        }
        return false;
    }

    private boolean checkDrawnRelationship(long id){
        for (long rel_id : drawnRelations) {
            if (rel_id == id) {
                return true;
            }
        }
        return false;
    }

    public class node {
        String identifier;

        public node(String identifier) {
            this.identifier = identifier;
        }
    }

    public class edge {
        String startNodeId;
        String endNodeId;
        String relationName;

        public edge(String startNodeId, String endNodeId, String relationName) {
            this.startNodeId = startNodeId;
            this.endNodeId = endNodeId;
            this.relationName = relationName;
        }
    }
}