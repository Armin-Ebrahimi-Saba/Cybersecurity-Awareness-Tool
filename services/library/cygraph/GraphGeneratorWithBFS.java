package services.library.cygraph;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import services.db.Neo4jDbConnection;
import services.library.drowTools.GraphDraw;
import services.library.drowTools.NodeHelper;

import java.awt.*;
import java.util.*;

/**
 * BFS Algorithm :: used transaction of neo4j
 */
public class GraphGeneratorWithBFS extends Neo4jDbConnection {

    private Queue<Long> queue;
    private HashMap<Long, NodeHelper> nodeHelpers;
    private ArrayList<Long> seenNodes;
    private ArrayList<Long> drawnRelations;

    public GraphGeneratorWithBFS(Node parent){
        super();
        queue = new ArrayDeque<>();
        nodeHelpers = new HashMap<>();
        seenNodes = new ArrayList<>();
        drawnRelations = new ArrayList<>();
        initialize(parent);
        try( Transaction tx = this.graphDb.beginTx() ){
            drawGraph(tx, new GraphDraw("CyGraph"));
            tx.commit();
            shutDown();
        }
    }

    private void drawGraph(Transaction tx, GraphDraw frame){
        if (queue.isEmpty()){
            return;
        }

        long parent_id = queue.poll();
        NodeHelper parentNodeHelper = nodeHelpers.get(parent_id);
        Node parent = tx.getNodeById(parent_id);
        Map<String, Object> properties = tx.getNodeById(parent.getId()).getAllProperties();

        frame.addNode(
                String.valueOf(parent.getId()),
                properties.get("name").toString(),
                properties.get("collection").toString(),
                (int) parentNodeHelper.x,
                (int) parentNodeHelper.y,
                String.valueOf(parentNodeHelper.parent_id),
                properties.get("type").toString()
        );

        seenNodes.add(parent.getId());

        Iterator<Relationship> relationships =  tx.getNodeById(parent.getId()).getRelationships().iterator();
        ArrayList<Long> relations = new ArrayList<>();
        while (relationships.hasNext()){
            relations.add(relationships.next().getId());
        }

        int neighboursNumber = relations.size();

        double preX = parentNodeHelper.x;
        double preY = parentNodeHelper.y;
        if (parentNodeHelper.parent_id != null){
            NodeHelper ancestor = nodeHelpers.get(parentNodeHelper.parent_id);
            preX = ancestor.x;
            preY = ancestor.y;
        }

        double[][] locations = frame.getNewLocations(neighboursNumber,parentNodeHelper.x,parentNodeHelper.y,preX, preY);

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
                // push node in queue
                nodeHelpers.put(endNode.getId(),new NodeHelper(locations[index][0], locations[index][1],parent_id));
                queue.add(endNode.getId());
            }
            else if (!checkDrawnRelationship(relId)){
                Map<String, Object> relProperties = tx.getRelationshipById(relId).getAllProperties();
                // relating nodes
                frame.addEdge(
                        relProperties.get("name").toString(),
                        String.valueOf(parent.getId()),
                        String.valueOf(endNode.getId()),
                        relationship_kind
                );
                drawnRelations.add(relId);
            }
            index++;
        }
        drawGraph(tx,frame);
    }

    private void initialize(Node parent){
        double initialX = Toolkit.getDefaultToolkit().getScreenSize().getWidth()/2;
        double initialY = Toolkit.getDefaultToolkit().getScreenSize().getHeight()/2;
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
}
