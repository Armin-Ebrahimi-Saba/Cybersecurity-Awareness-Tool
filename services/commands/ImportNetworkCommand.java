package services.commands;

import models.AttackPattern;
import models.Machine;
import models.Topology;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import services.db.Neo4jDbConnection;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

public class ImportNetworkCommand extends Neo4jDbConnection {

    private static final String NETWORK  = "src/main/java/services/library/network/network.csv";
    private final HashMap<String,String> capec_relations = new HashMap<>(){{
        put("Normal", "");
        put("Network Scan", "224-169");
        put("Backdoor", "438-444-447");
        put("Web Vulnerability Scan", "224");
        put("Directory Bruteforce", "");
        put("BENIGN", "");
        put("Account Discovery", "118-116-560");
        put("Account Bruteforce", "112-49");
        put("SQL Injection", "66");
        put("Malware Download", "185");
        put("CSRF", "62");
        put("Command Injection", "248");
        put("Data Exfiltration", "118-116");
    }};

    public ImportNetworkCommand() throws IOException {
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
        boolean flag = false;

        try {
            BufferedReader br = new BufferedReader(new FileReader(NETWORK));

            while ((line = br.readLine()) != null) {
                String[] information = line.split(splitBy);
                if (!flag) {
                    for (String col : information) {
                        columns.put(col, Arrays.asList(information).indexOf(col));
                    }
                    flag = true;
                } else if (information.length == columns.size()) {
                    Machine machine = Machine.findById(information[columns.get("ID")]);
                    if (machine == null){
                        machine = new Machine(
                                information[columns.get("ID")],
                                information[columns.get("ID")],
                                "",
                                information[columns.get("Source_IP")],
                                information[columns.get("Source_Port")],
                                information[columns.get("Destination_IP")],
                                information[columns.get("Destination_Port")],
                                information[columns.get("Protocol")],
                                information[columns.get("Stage")]
                        );
                        machine.save();
                    }
                    machine = Machine.findById(information[columns.get("ID")]);
                    if (machine != null){
                        Node machineNode = storeOrGetNode(machine._id,machine.title,machine.getCollection(),tx,null);
                        if (capec_relations.containsKey(information[columns.get("CAPEC")])){
                            String[] capec_ids = capec_relations.get(information[columns.get("CAPEC")]).split("-");
                            for (String capec_id : capec_ids) {
                                AttackPattern attackPattern = AttackPattern.findById(capec_id);
                                if (attackPattern != null){
                                    Node capec = storeOrGetNode(attackPattern._id,attackPattern.title,attackPattern.getCollection(),tx,null);
                                    Topology topology = Topology.findByOriginAndTarget(
                                            attackPattern._id,
                                            AttackPattern.COLLECTION,
                                            machine._id,
                                            Machine.COLLECTION
                                    );
                                    if (topology == null){
                                        topology = new Topology(
                                                "VICTIM",
                                                attackPattern._id,
                                                AttackPattern.COLLECTION,
                                                machine._id,
                                                Machine.COLLECTION,
                                                null
                                        );
                                        topology.save();
                                        relationship("VICTIM",capec,machineNode,tx);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }
}
