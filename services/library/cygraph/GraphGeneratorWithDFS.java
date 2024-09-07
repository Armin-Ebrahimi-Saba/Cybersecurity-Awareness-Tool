package services.library.cygraph;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import services.db.Neo4jDbConnection;
import services.library.drowTools.GraphDraw;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * DFS Algorithm :: used transaction of neo4j
 */
public class GraphGeneratorWithDFS extends Neo4jDbConnection{

    private ArrayList<Long> seenNodes;
    private ArrayList<Long> drawnRelations;

    public GraphGeneratorWithDFS(Node parent){
        super();
        seenNodes = new ArrayList<>();
        drawnRelations = new ArrayList<>();
        double initialX = Toolkit.getDefaultToolkit().getScreenSize().getWidth()/2;
        double initialY = Toolkit.getDefaultToolkit().getScreenSize().getHeight()/2;
        try( Transaction tx = this.graphDb.beginTx() ){
            drawGraph(tx,parent, new GraphDraw("CyGraph"), initialX, initialY, null);
            tx.commit();
            shutDown();
        }
    }

    private boolean drawGraph(Transaction tx,Node parent, GraphDraw frame, double x, double y, String ancestor_id){
        if (checkNodeExists(parent.getId())){
            return false;
        }
        Map<String, Object> properties = tx.getNodeById(parent.getId()).getAllProperties();
        frame.addNode(
                String.valueOf(parent.getId()),
                properties.get("name").toString(),
                properties.get("collection").toString(),
                (int) x,
                (int) y,
                ancestor_id,
                properties.get("type").toString()
        );
        seenNodes.add(parent.getId());

        Iterator<Relationship> relationships =  tx.getNodeById(parent.getId()).getRelationships().iterator();
        ArrayList<Long> relations = new ArrayList<>();
        while (relationships.hasNext()){
            relations.add(relationships.next().getId());
        }
        int neighboursNumber = relations.size();

        services.library.drowTools.Node ancestor = frame.getNode(ancestor_id);
        double preX = x;
        double preY = y;
        if (ancestor != null){
            preX = ancestor.x;
            preY = ancestor.y;
        }

        double[][] locations = frame.getNewLocations(neighboursNumber,x,y,preX, preY);

        ArrayList<Long> skipRelation = new ArrayList<Long>();
        int index = 0;
        for (long relId : relations) {
            if (skipRelation.contains(relId)){
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

            // DFS
            boolean notRepeated = drawGraph(tx,endNode, frame, locations[index][0], locations[index][1], String.valueOf(parent.getId()));

            if (notRepeated){
                index++;
            }

            if (!checkDrawnRelationship(relId)){
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

        }
        return true;
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
