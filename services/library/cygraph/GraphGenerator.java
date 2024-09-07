package services.library.cygraph;

import models.*;
import org.bson.types.ObjectId;
import org.neo4j.graphdb.Node;
import services.library.drowTools.GraphDraw;
import services.library.neo4j.Neo4jActivities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GraphGenerator {

    public HashMap<ObjectId, HashMap<ObjectId,String>> graph = new HashMap<>();
    public HashMap<ObjectId,String> parents = new HashMap<>();
    public HashMap<ObjectId,HashMap<ObjectId,String>> topologies_info = new HashMap<>();

    private ArrayList<Long> seenNodes = new ArrayList<>();

    /**
     * DFS Algorithm :: Depth First Search
     */
    public void generate(ObjectId parent_id,String parentCollectionName){

        graph.put(parent_id,new HashMap<>());
        topologies_info.put(parent_id,new HashMap<>());
        parents.put(parent_id,parentCollectionName);

        ArrayList<Topology> remain_relations = new ArrayList<>();
        ArrayList<Topology> relations = Topology.findRelationsByMainId(
                parent_id,
                parentCollectionName
        );

        if (!relations.isEmpty()){
            for (Topology relation : relations) {
                if
                (
                        (relation.origin_id.equals(parent_id)  && graph.containsKey(relation.target_id)) ||
                                (relation.target_id.equals(parent_id) && graph.containsKey(relation.origin_id))
                )
                {
                    continue;
                }
                remain_relations.add(relation);
            }
            if (!remain_relations.isEmpty()){
                for (Topology relation : remain_relations) {
                    HashMap<ObjectId,String> topology_info = topologies_info.get(parent_id);
                    HashMap<ObjectId,String> children = graph.get(parent_id);

                    if (relation.origin_id.equals(parent_id)){
                        children.put(relation.target_id,relation.target_type);
                        graph.put(parent_id,children);
                        topology_info.put(relation.target_id,relation.title);
                        topologies_info.put(parent_id,topology_info);
                        generate(relation.target_id,relation.target_type);
                    }
                    else {
                        children.put(relation.origin_id,relation.origin_type);
                        graph.put(parent_id,children);
                        topology_info.put(relation.origin_id,relation.title);
                        topologies_info.put(parent_id,topology_info);
                        generate(relation.origin_id,relation.origin_type);
                    }
                }
            }
        }
    }

    /**
     * DFS Algorithm :: used Neo4jActivities (BAD IDEA)
     */
    public boolean generateFromNeo4j(Node parent, GraphDraw frame, double x, double y, double preX, double preY){
        if (checkNodeExists(parent.getId())){
            return false;
        }
        Map<String, Object> properties = Neo4jActivities.getNodeProperties(parent);
        frame.addNode(
                String.valueOf(parent.getId()),
                properties.get("name").toString(),
                properties.get("collection").toString(),
                (int) x,
                (int) y,
                null,
                properties.get("type").toString()
        );
        seenNodes.add(parent.getId());

        ArrayList<Long> relations = Neo4jActivities.getNodeRelationships(parent);
        int neighboursNumber = relations.size();
        double[][] locations = frame.getNewLocations(neighboursNumber,x,y,preX,preY);

        int index = 0;
        for (long relId : relations) {
            // get end node
            Node endNode = Neo4jActivities.getRelationshipEndNode(relId);

            // DFS
            boolean notRepeated = generateFromNeo4j(endNode, frame, locations[index][0], locations[index][1], x, y);

            if (notRepeated){
                Map<String, Object> relProperties = Neo4jActivities.getRelationshipProperties(relId);
                // relating nodes
                frame.addEdge(
                        relProperties.get("name").toString(),
                        String.valueOf(parent.getId()),
                        String.valueOf(endNode.getId()),
                        0
                );
                index++;
            }
        }
        return true;
    }

    private boolean checkNodeExists(long id){
        for (long node_id : seenNodes) {
            if (node_id == id)
                return true;
        }
        return false;
    }
}
