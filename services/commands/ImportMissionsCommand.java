package services.commands;

import models.Topology;
import models.missions.*;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import services.db.Neo4jDbConnection;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;

public class ImportMissionsCommand extends Neo4jDbConnection {

    public static String MISSIONS_PATH;
    public static String MISSIONS_INFORMATION_PATH;

    public ImportMissionsCommand() throws IOException {
        super();
        try( Transaction tx = this.graphDb.beginTx() ){
            fire(tx);
            tx.commit();
            shutDown();
        }
    }

    public void fire(Transaction tx) throws IOException {
        if (
                !MISSIONS_PATH.isEmpty() &&
                        !MISSIONS_PATH.isBlank() &&
                        !MISSIONS_INFORMATION_PATH.isEmpty() &&
                        !MISSIONS_INFORMATION_PATH.isBlank()
        ) {
            String line;
            String splitBy = ",";
            HashMap<String,Integer> columns = new HashMap<>();
            HashMap<String, MissionBase> nodesInfo = new HashMap<>();
            boolean flag = false;

            try {
                BufferedReader br = new BufferedReader(new FileReader(MISSIONS_PATH));

                while ((line = br.readLine()) != null) {
                    String[] information = line.split(splitBy);
                    if (!flag) {
                        for (String col : information) {
                            columns.put(col, Arrays.asList(information).indexOf(col));
                        }
                        flag = true;
                    }
                    else if (information.length == columns.size()) {
                        switch (information[columns.get("Type")]){
                            case "task" :
                                MissionTask missionTask = MissionTask.findByTitle(information[columns.get("Title")]);
                                if (missionTask == null){
                                    missionTask = new MissionTask(
                                            information[columns.get("Title")],
                                            information[columns.get("Description")],
                                            information[columns.get("nodeImpact")],
                                            information[columns.get("relativeWeight")]
                                    );
                                    missionTask.save();
                                }
                                missionTask = MissionTask.findByTitle(information[columns.get("Title")]);
                                if (missionTask != null){
                                    storeOrGetNode(missionTask._id,missionTask.title,missionTask.getCollection(),tx,null);
                                    nodesInfo.put(information[columns.get("ID")],missionTask);
                                }
                                break;
                            case "information_assets":
                                MissionInformation missionInformation = MissionInformation.findByTitle(information[columns.get("Title")]);
                                if (missionInformation == null){
                                    missionInformation = new MissionInformation(
                                            information[columns.get("Title")],
                                            information[columns.get("Description")],
                                            information[columns.get("nodeImpact")],
                                            information[columns.get("relativeWeight")]
                                    );
                                    missionInformation.save();
                                }
                                missionInformation = MissionInformation.findByTitle(information[columns.get("Title")]);
                                if (missionInformation != null){
                                    storeOrGetNode(missionInformation._id,missionInformation.title,missionInformation.getCollection(),tx,null);
                                    nodesInfo.put(information[columns.get("ID")],missionInformation);
                                }
                                break;
                            case "cyber_asset":
                                CyberAsset cyberAsset = CyberAsset.findByTitle(information[columns.get("Title")]);
                                if (cyberAsset == null){
                                    cyberAsset = new CyberAsset(
                                            information[columns.get("Title")],
                                            information[columns.get("Description")],
                                            information[columns.get("nodeImpact")],
                                            information[columns.get("relativeWeight")]
                                    );
                                    cyberAsset.save();
                                }
                                cyberAsset = CyberAsset.findByTitle(information[columns.get("Title")]);
                                if (cyberAsset != null){
                                    storeOrGetNode(cyberAsset._id,cyberAsset.title,cyberAsset.getCollection(),tx,null);
                                    nodesInfo.put(information[columns.get("ID")],cyberAsset);
                                }
                                break;
                            case "mission_objective":
                                MissionObjective missionObjective = MissionObjective.findByTitle(information[columns.get("Title")]);
                                if (missionObjective == null){
                                    missionObjective = new MissionObjective(
                                            information[columns.get("Title")],
                                            information[columns.get("Description")],
                                            information[columns.get("nodeImpact")],
                                            information[columns.get("relativeWeight")]
                                    );
                                    missionObjective.save();
                                }
                                missionObjective = MissionObjective.findByTitle(information[columns.get("Title")]);
                                if (missionObjective != null){
                                    storeOrGetNode(missionObjective._id,missionObjective.title,missionObjective.getCollection(),tx,null);
                                    nodesInfo.put(information[columns.get("ID")],missionObjective);
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }

                HashMap<String,Integer> edgesColumns = new HashMap<>();
                flag = false;

                br = new BufferedReader(new FileReader(MISSIONS_INFORMATION_PATH));
                while ((line = br.readLine()) != null) {
                    String[] information = line.split(splitBy);
                    if (!flag) {
                        for (String col : information) {
                            edgesColumns.put(col, Arrays.asList(information).indexOf(col));
                        }
                        flag = true;
                    }
                    else {
                        if (information.length == edgesColumns.size()) {
                            if (
                                    nodesInfo.containsKey(information[columns.get("FROM_ID")]) &&
                                            nodesInfo.containsKey(information[columns.get("TO_ID")])
                            ){
                                MissionBase fromMission = nodesInfo.get(information[columns.get("FROM_ID")]);
                                MissionBase toMission = nodesInfo.get(information[columns.get("TO_ID")]);

                                Node fromMissionNode = storeOrGetNode(fromMission._id,fromMission.title,fromMission.getCollection(),tx,null);
                                Node toMissionNode = storeOrGetNode(toMission._id,toMission.title,toMission.getCollection(),tx,null);

                                Topology topology = Topology.findByOriginAndTarget(
                                        fromMission._id,
                                        fromMission.getCollection(),
                                        toMission._id,
                                        toMission.getCollection()
                                );
                                if (topology == null){
                                    topology = new Topology(
                                            information[columns.get("Dependency")],
                                            fromMission._id,
                                            fromMission.getCollection(),
                                            toMission._id,
                                            toMission.getCollection(),
                                            ""
                                    );
                                    topology.save();
                                    relationship(information[columns.get("Dependency")],fromMissionNode,toMissionNode,tx);
                                }
                            }
                        }
                    }
                }
                File f= new File(MISSIONS_PATH);
                f.delete();
                f = new File(MISSIONS_INFORMATION_PATH);
                f.delete();
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
