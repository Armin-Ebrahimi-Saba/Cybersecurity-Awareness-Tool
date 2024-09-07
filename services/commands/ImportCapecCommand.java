package services.commands;

import models.AttackPattern;
import models.Topology;
import models.vulnerabilities.CWE;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import services.db.Neo4jDbConnection;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ImportCapecCommand extends Neo4jDbConnection {

    private static final String CAPEC  = "src/main/java/services/library/capec/capec-mechanism-of-attack.csv";

    public ImportCapecCommand() throws IOException {
        super();
        try( Transaction tx = this.graphDb.beginTx() ){
            fire(tx);
            tx.commit();
            shutDown();
        }
    }

    private void fire(Transaction tx) throws IOException {
        String line;
        String splitBy = ",";
        HashMap<String,Integer> columns = new HashMap<>();
        HashMap<String, ArrayList> relations = new HashMap<>();
        HashMap<String, ArrayList> cwe_relations = new HashMap<>();
        boolean flag = false;

        try {
            BufferedReader br = new BufferedReader(new FileReader(CAPEC));

            while ((line = br.readLine()) != null) {
                String[] information = line.split(splitBy);
                if (!flag){
                    for (String col : information) {
                        columns.put(col, Arrays.asList(information).indexOf(col));
                    }
                    flag = true;
                }
                else if (information.length == columns.size()){
                    AttackPattern attackPattern = AttackPattern.findById(information[columns.get("ID")]);
                    if (attackPattern == null){
                        attackPattern = new AttackPattern(
                                information[columns.get("ID")],
                                information[columns.get("Name")],
                                information[columns.get("Description")],
                                information[columns.get("Likelihood Of Attack")],
                                information[columns.get("Typical Severity")],
                                information[columns.get("Taxonomy Mappings")],
                                information[columns.get("Status")]
                        );
                        attackPattern.save();
                    }
                    else {
                        // todo :: update it if required!
                    }
                    relations.put(
                            information[columns.get("ID")],
                            this.getRelations(information[columns.get("Related Attack Patterns")])
                    );
                    cwe_relations.put(
                            information[columns.get("ID")],
                            this.getCWEs(information[columns.get("Related Weaknesses")])
                    );
                }
            }
            for (Map.Entry<String, ArrayList> related : relations.entrySet()) {
                String origin_id = related.getKey();
                for (Object target_id : related.getValue()) {
                    AttackPattern origin = AttackPattern.findById(origin_id);
                    if (origin != null && origin.id != null){
                        Node originNode = storeOrGetNode(origin._id,origin.title,origin.getCollection(),tx,null);
                        AttackPattern target = AttackPattern.findById((String) target_id);
                        if (target != null && target.id != null){
                            Node targetNode = storeOrGetNode(target._id,target.title,target.getCollection(),tx,null);
                            Topology topology = Topology.findByOriginAndTarget(
                                    origin._id,
                                    AttackPattern.COLLECTION,
                                    target._id,
                                    AttackPattern.COLLECTION
                            );
                            if (topology == null){
                                topology = new Topology(
                                        "PREPARES",
                                        origin._id,
                                        AttackPattern.COLLECTION,
                                        target._id,
                                        AttackPattern.COLLECTION,
                                        null
                                );
                                topology.save();
                                relationship("PREPARES",targetNode,originNode,tx);
                            }
                            else {
                                // todo :: update exist topology
                            }
                        }
                    }
                }
            }
            for (Map.Entry<String, ArrayList> related : cwe_relations.entrySet()) {
                String origin_id = related.getKey();
                for (Object target_id : related.getValue()) {
                    AttackPattern origin = AttackPattern.findById(origin_id);
                    if (origin != null && origin.id != null){
                        Node originNode = storeOrGetNode(origin._id,origin.title,origin.getCollection(),tx,null);
                        ArrayList<CWE> targets = CWE.getAllById((String) target_id);
                        for (CWE target: targets) {
                            if (target != null && target.id != null){
                                Node targetNode = storeOrGetNode(target._id,target.title,target.getCollection(),tx,null);
                                Topology topology = Topology.findByOriginAndTarget(
                                        origin._id,
                                        AttackPattern.COLLECTION,
                                        target._id,
                                        CWE.COLLECTION
                                );
                                if (topology == null){
                                    topology = new Topology(
                                            "AGAINST",
                                            origin._id,
                                            AttackPattern.COLLECTION,
                                            target._id,
                                            CWE.COLLECTION,
                                            null
                                    );
                                    topology.save();
                                    relationship("PREPARES",originNode,targetNode,tx);
                                }
                                else {
                                    // todo :: update exist topology
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<String> getRelations(String relations){
        ArrayList<String> result = new ArrayList<>();
        String[] relSps = relations.split("::");
        for (String relSp : relSps) {
            if (relSp.contains("ParentOf")){
                continue;
            }
            String[] related = relSp.split("CAPEC ID:");
            if (related.length != 1){
                result.add(related[1]);
            }
        }
        return result;
    }

    private ArrayList<String> getCWEs(String relations){
        ArrayList<String> result = new ArrayList<>();
        String[] relSps = relations.split("::");
        for (String relSp : relSps) {
            if (!relSp.equalsIgnoreCase("") && !relSp.equalsIgnoreCase("\"")){
                result.add("CWE-"+relSp);
            }
        }
        return result;
    }
}
