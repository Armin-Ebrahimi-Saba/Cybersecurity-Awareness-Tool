package services.commands;

import models.AttackPattern;
import models.Snort;
import models.Topology;
import models.vulnerabilities.CVE;
import models.vulnerabilities.CWE;
import models.vulnerabilities.Reference;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import services.db.Neo4jDbConnection;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Scanner;

public class ImportSnortsCommand extends Neo4jDbConnection {

    private static final File SNORTS  = new File("src/main/java/services/library/snorts");
    private ArrayList<String> snortFiles = new ArrayList<>();

    public ImportSnortsCommand() throws IOException {
        super();
        readSnortPaths();
        try( Transaction tx = this.graphDb.beginTx() ){
            fire(tx);
            tx.commit();
            shutDown();
        }
    }

    private void fire(Transaction tx) throws IOException {
        for (String filePath : snortFiles) {
            HashMap<String,String> snortInfo = getSnortInfo(filePath);
            if (!snortInfo.containsKey("snort_id")){
                continue;
            }
            Snort snort = Snort.findById(snortInfo.get("snort_id"));
            if (snort == null){
                snort = new Snort(snortInfo.get("snort_id"));
                snort.save();
            }
            snort = Snort.findById(snortInfo.get("snort_id"));
            if (snort != null && snort._id != null){
                Node snortNode = storeOrGetNode(snort._id,snort.title,snort.getCollection(),tx,null);

                if (snortInfo.containsKey("cve")){
                    CVE cveInst = CVE.findById(snortInfo.get("cve"));
                    if (cveInst != null && cveInst._id != null){
                        Node cveInstNode = storeOrGetNode(cveInst._id,cveInst.title,cveInst.getCollection(),tx,null);

                        Topology topology = Topology.findByOriginAndTarget(
                                snort._id,
                                Snort.COLLECTION,
                                cveInst._id,
                                CVE.COLLECTION
                        );
                        if (topology == null){
                            topology = new Topology(
                                    "SN_CVE",
                                    snort._id,
                                    Snort.COLLECTION,
                                    cveInst._id,
                                    CVE.COLLECTION,
                                    null
                            );
                            topology.save();
                            relationship("SN_CVE",snortNode,cveInstNode,tx);
                        }
                    }
                }
                if (snortInfo.containsKey("cwe")){
                    ArrayList<CWE> cwes = CWE.getAllById(snortInfo.get("cwe"));
                    for (CWE cweInst : cwes) {
                        if (cweInst != null && cweInst._id != null){
                            Node cweNode = storeOrGetNode(cweInst._id,cweInst.id,cweInst.getCollection(),tx,null);

                            Topology topology = Topology.findByOriginAndTarget(
                                    snort._id,
                                    Snort.COLLECTION,
                                    cweInst._id,
                                    CWE.COLLECTION
                            );
                            if (topology == null){
                                topology = new Topology(
                                        "SN_CWE",
                                        snort._id,
                                        Snort.COLLECTION,
                                        cweInst._id,
                                        CWE.COLLECTION,
                                        null
                                );
                                topology.save();
                                relationship("SN_CWE",snortNode,cweNode,tx);
                            }
                        }
                    }
                }
                if (snortInfo.containsKey("capec")){
                    AttackPattern attackPattern = AttackPattern.findById(snortInfo.get("capec"));
                    if (attackPattern != null && attackPattern._id != null){
                        Node attackPatternNode = storeOrGetNode(attackPattern._id,attackPattern.title,attackPattern.getCollection(),tx,null);

                        Topology topology = Topology.findByOriginAndTarget(
                                snort._id,
                                Snort.COLLECTION,
                                attackPattern._id,
                                AttackPattern.COLLECTION
                        );
                        if (topology == null){
                            topology = new Topology(
                                    "DETECTION",
                                    snort._id,
                                    Snort.COLLECTION,
                                    attackPattern._id,
                                    AttackPattern.COLLECTION,
                                    null
                            );
                            topology.save();
                            relationship("DETECTION",snortNode,attackPatternNode,tx);
                        }
                    }
                }
            }
        }
    }

    private void readSnortPaths() {
        for (File fileEntry : Objects.requireNonNull(SNORTS.listFiles())) {
            snortFiles.add(fileEntry.getPath());
        }
    }

    private HashMap<String,String> getSnortInfo(String filePath) {
        HashMap<String,String> snortInfo = new HashMap<>();
        File file = new File(filePath);
        try {
            Scanner scan = new Scanner(file);
            while (scan.hasNextLine()) {
                String scannedline = scan.nextLine();
                if ((scannedline.contains("Sid") || scannedline.contains("sid")) && scan.hasNextLine()){
                    String snort_id = cleaningWord(scan.nextLine());
                    if (!snort_id.equalsIgnoreCase("")){
                        snortInfo.put("snort_id",snort_id);
                    }
                }
                if (scannedline.contains("CVE-") && scannedline.contains("NIST")){
                    int start = scannedline.indexOf("CVE-");
                    int end = getEndOfCVEIndex(scannedline,start);
                    String cve = cleaningWord(scannedline.substring(start, end));
                    if (!cve.equalsIgnoreCase("")){
                        snortInfo.put("cve",cve);
                    }
                }
                if (scannedline.contains("Common Weakness Enumeration:") && scan.hasNextLine()){
                    String cwe = cleaningWord(getIdFromUrl(scan.nextLine()));
                    if (!cwe.equalsIgnoreCase("")){
                        snortInfo.put("cwe","CWE-"+cwe);
                    }
                }
                if (scannedline.contains("Common Attack Pattern Enumeration and Classification:") && scan.hasNextLine()){
                    String capec = cleaningWord(getIdFromUrl(scan.nextLine()));
                    if (!capec.equalsIgnoreCase("")){
                        snortInfo.put("capec",capec);
                    }
                }
            }
            scan.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return snortInfo;
    }

    private String cleaningWord(String word){
        return word.replace("\n","")
                .replace("\r","")
                .replace("\\","")
                .replace("\t","")
                .replace("'","")
                .replace(" ","")
                .replace(";","");
    }

    private int getEndOfCVEIndex(String line,int start_index){
        start_index += 9;
        while (true){
            try {
                Integer.valueOf(line.charAt(start_index));
            }catch (Exception e){
                return start_index;
            }
            start_index++;
        }
    }

    private String getIdFromUrl(String url){
        int start = url.lastIndexOf("/")+1;
        int end = url.lastIndexOf(".html");
        return url.substring(start,end);
    }
}
