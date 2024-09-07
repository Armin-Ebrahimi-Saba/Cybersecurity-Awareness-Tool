package services.adapters;

import layouts.MistakeQuery;
import layouts.QueryInfoLayout;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import services.db.Neo4jDbConnection;
import services.library.drowTools.GraphDraw;
import services.library.drowTools.NodeHelper;

import java.awt.*;
import java.util.*;

public class MissionsHierarchyBuilder extends Neo4jDbConnection {

    private long indexNodeId = -1;
    private ArrayList<Long> node_ids = new ArrayList<>();
    private ArrayList<Long> nodes_helper = new ArrayList<>();

    private Queue<Long> queue;
    private HashMap<Long, NodeHelper> nodeHelpers;
    private ArrayList<Long> seenNodes;
    private ArrayList<Long> drawnRelations;

    private int numberOfRows = 0;
    private int numberOfColumns = 0;
    private int largestNodeDegree = 0;

    private Map<String, Integer> size_layers = new HashMap<>();
    private Map<String, ArrayList<Long>> nodeLayers = new HashMap<>();

    public MissionsHierarchyBuilder(String query, HashMap<String, Color> colors) {
        super();
        try {
            boolean flag = false;
            String temp = "";
            String q = "";

            for (int i = 0; i < query.length(); i++) {
                if (query.charAt(i) == ']') {
                    char[] chars = temp.toCharArray();
                    StringBuilder sb = new StringBuilder();
                    for(char c : chars){
                        if(Character.isDigit(c)){
                            sb.append(c);
                        }
                    }
                    temp = temp.replace(sb,"").replace("*","");
                    if (temp.isEmpty())
                        q = q + "[:LOW|HIGH|INN|ROUTES*]";
                    else {
                        boolean low_flag = false;
                        if (temp.contains("LOW") || temp.contains("HIGH")) {
                            q = q + "[:";
                            if (temp.contains("LOW")) {
                                q = q + "LOW";
                                low_flag = true;
                            }
                            if (temp.contains("HIGH")) {
                                if (low_flag)
                                    q = q + "|";
                                q = q + "HIGH";
                            }
                            if (temp.contains("INN"))
                                q = q + "|INN";
                            if (temp.contains("ROUTES"))
                                q = q + "|ROUTES";
                            q = q + "*]";
                        }
                        else {
                            new MistakeQuery();
                            return;
                        }
                    }
                    flag = false;
                    temp = "";
                    continue;
                }
                if (query.charAt(i) == '[') {
                    flag = true;
                    continue;
                }
                if (flag) {
                    temp = temp + query.charAt(i);
                }
                else {
                    q = q + query.charAt(i);
                }
            }

            query = new StringBuffer(q).replace(
                    q.lastIndexOf("RETURN"),
                    q.length(),
                    "RETURN [node in nodes(p) | id(node)]"
            ).toString();

            try(Transaction tx = this.graphDb.beginTx();
                Result result = tx.execute(query)
            ){
                while ( result.hasNext() ) {
                    Map<String,Object> row = result.next();
                    numberOfRows += row.size();
                    for ( Map.Entry<String,Object> column : row.entrySet() ) {
                        if (column.getValue() instanceof ArrayList){
                            numberOfColumns = ((ArrayList) column.getValue()).size();
                            nodes_helper = new ArrayList<>();
                            for (Object id : (ArrayList)column.getValue()) {
                                Map<String, Object> properties = tx.getNodeById((Long) id).getAllProperties();
                                ArrayList<Long> item = new ArrayList<>();
                                switch (properties.get("collection").toString()) {
                                    case "mission_information":
                                        if (nodeLayers.containsKey("mission_information")) {
                                            if (!nodeLayers.get("mission_information").contains(id))
                                                nodeLayers.get("mission_information").add((Long) id);
                                        }
                                        else {
                                            item.add((long)id);
                                            nodeLayers.put("mission_information",item);
                                        }
                                        break;
                                    case "mission_tasks":
                                        if (nodeLayers.containsKey("mission_tasks")) {
                                            if (!nodeLayers.get("mission_tasks").contains(id))
                                                nodeLayers.get("mission_tasks").add((Long) id);
                                        }
                                        else {
                                            item.add((long)id);
                                            nodeLayers.put("mission_tasks",item);
                                        }
                                        break;
                                    case "machines":
                                        if (nodeLayers.containsKey("machines")) {
                                            if (!nodeLayers.get("machines").contains(id))
                                                nodeLayers.get("machines").add((Long) id);
                                        }
                                        else {
                                            item.add((long)id);
                                            nodeLayers.put("machines",item);
                                        }
                                        break;
                                    case "subnets":
                                        if (nodeLayers.containsKey("subnets")) {
                                            if (!nodeLayers.get("subnets").contains(id))
                                                nodeLayers.get("subnets").add((Long) id);
                                        }
                                        else {
                                            item.add((long)id);
                                            nodeLayers.put("subnets",item);
                                        }
                                        break;
                                    case "firewalls":
                                        if (nodeLayers.containsKey("firewalls")) {
                                            if (!nodeLayers.get("firewalls").contains(id))
                                                nodeLayers.get("firewalls").add((Long) id);
                                        }
                                        else {
                                            item.add((long)id);
                                            nodeLayers.put("firewalls",item);
                                        }
                                        break;
                                }
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

                for (Map.Entry<String, ArrayList<Long>> entry : nodeLayers.entrySet())
                    size_layers.put(entry.getKey(),entry.getValue().size());

                if (node_ids.size() == 0){
                    throw new Exception();
                }
                queue = new ArrayDeque<>();
                nodeHelpers = new HashMap<>();
                seenNodes = new ArrayList<>();
                drawnRelations = new ArrayList<>();
                seenNodes.add(indexNodeId);
                Node parent = tx.getNodeById(indexNodeId);
                initialize(parent);

                drawGraph(tx, new GraphDraw("Missions Dependency Hierarchy", colors));

                new QueryInfoLayout(
                        numberOfRows,
                        numberOfColumns,
                        node_ids.size(),
                        drawnRelations.size(),
                        largestNodeDegree,
                        colors
                );

                tx.commit();
            }
            catch (Exception exception){
                new MistakeQuery();
            }
        }
        catch (Exception exception){
            new MistakeQuery();
        }
        shutDown();
    }

    private void drawGraph(Transaction tx, GraphDraw frame) {
        if (queue.isEmpty()) {
            return;
        }

        long parent_id = queue.poll();
        NodeHelper parentNodeHelper = nodeHelpers.get(parent_id);
        Node parent = tx.getNodeById(parent_id);
        Map<String, Object> properties = tx.getNodeById(parent.getId()).getAllProperties();

        int m;
        switch (properties.get("collection").toString()) {
            case "mission_information":
                parentNodeHelper.y = (int)(Math.random() * (250  + 1));
                parentNodeHelper.x = (int)(Math.random() * ((1000 - 600) + 1)) + 600;
                break;
            case "mission_tasks":
                parentNodeHelper.y = (int)(Math.random() * ((700 - 350) + 1)) + 350;
                parentNodeHelper.x = (int)(Math.random() * ((1000 - 600) + 1)) + 600;
                break;
            case "machines":
                parentNodeHelper.x = 400;
                m = nodeLayers.get("machines").size();
                parentNodeHelper.y = 700 -  ((size_layers.get("machines") - 0.5) / m) * 700;
                size_layers.put("machines", size_layers.get("machines") - 1);
                break;
            case "subnets":
                parentNodeHelper.x = 100;
                m = nodeLayers.get("subnets").size();
                parentNodeHelper.y = 700 -  ((size_layers.get("subnets") - 0.5) / m) * 700;
                size_layers.put("subnets", size_layers.get("subnets") - 1);
                break;
            case "firewalls":
                parentNodeHelper.x = 250;
                m = nodeLayers.get("firewalls").size();
                parentNodeHelper.y = 700 -  ((size_layers.get("firewalls") - 0.5) / m) * 700;
                size_layers.put("firewalls", size_layers.get("firewalls") - 1);
                break;
        }
        /*
        System.out.println("id: " + parent.getId());
        String x = properties.get("collection").toString();
        if (x.equals("subnets") || x.equals("machines") || x.equals("firewalls")) {

        {   System.out.println("name: " + properties.get("name").toString());
            System.out.println("collection: " + properties.get("collection").toString());
            System.out.println("p-id: " + parentNodeHelper.parent_id);
            System.out.println("///////////");
        }
        System.out.println("x: " + parentNodeHelper.x);
        System.out.println("y: " + parentNodeHelper.y);
        System.out.println("type: " + properties.get("type").toString());
        */

        frame.addNode(
                String.valueOf(parent.getId()),
                properties.get("name").toString(),
                properties.get("collection").toString(),
                (int) parentNodeHelper.x,
                (int) parentNodeHelper.y,
                String.valueOf(parentNodeHelper.parent_id),
                properties.get("type").toString()
        );

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

        if (largestNodeDegree < relations.size()){
            largestNodeDegree = relations.size();
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
                seenNodes.add(endNode.getId());
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

}

