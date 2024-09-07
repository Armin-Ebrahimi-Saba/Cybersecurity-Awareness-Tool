package services.commands;

import models.Topology;
import models.vulnerabilities.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import services.db.Neo4jDbConnection;
import services.middlewares.vulnerabilities.VulnerabilitiesAPI;

import java.io.IOException;


public class ImportVulnerabilitiesCommand extends Neo4jDbConnection {

    public ImportVulnerabilitiesCommand() throws IOException {
        super();
        try( Transaction tx = this.graphDb.beginTx() ){
            fire(tx);
            tx.commit();
            shutDown();
        }
    }

    private void fire(Transaction tx) throws IOException {
        int startIndex = 0;
        while (true){
            JSONObject vulnerabilitiesResult = VulnerabilitiesAPI.get(startIndex);

            if (!vulnerabilitiesResult.has("resultsPerPage") || vulnerabilitiesResult.getInt("resultsPerPage") == 0){
                break;
            }

            if (!vulnerabilitiesResult.has("result")){
                continue;
            }

            JSONObject result = (JSONObject) vulnerabilitiesResult.get("result");

            if (!result.has("CVE_Items")){
                continue;
            }

            JSONArray cve_items = (JSONArray)result.get("CVE_Items");
            for (int i = 0;i<cve_items.length();i++){
                JSONObject cve_info = (JSONObject)cve_items.get(i);

                if (!cve_info.has("cve")){
                    continue;
                }

                JSONObject cve = (JSONObject)cve_info.get("cve");

                if (!cve.has("CVE_data_meta")){
                    continue;
                }

                String cve_desc = "";
                if (cve.has("description")){
                    JSONObject cve_descriptions = cve.getJSONObject("description");
                    if (cve_descriptions.has("description_data")){
                        JSONArray description_data = cve_descriptions.getJSONArray("description_data");
                        for (int j = 0;j<description_data.length();j++){
                            JSONObject cve_desc_det = (JSONObject) description_data.get(j);
                            if (cve_desc_det.has("value")){
                                cve_desc = cve_desc_det.getString("value");
                            }
                        }
                    }
                }

                JSONObject CVE_data_meta = (JSONObject)cve.get("CVE_data_meta");

                CVE cveInst = CVE.findById(CVE_data_meta.getString("ID"));
                if (cveInst == null){
                    cveInst = new CVE(
                            CVE_data_meta.getString("ID"),
                            CVE_data_meta.getString("ID"),
                            cve_desc
                    );
                }
                else {
                    cveInst.description = cve_desc;
                }
                cveInst.save();

                cveInst = CVE.findById(CVE_data_meta.getString("ID"));
                if (cveInst == null){
                    continue;
                }
                Node cveInstNode = storeOrGetNode(cveInst._id,cveInst.title,cveInst.getCollection(),tx,CVE.TYPE);

                if (cve.has("references")){
                    JSONObject references = cve.getJSONObject("references");
                    if (references.has("reference_data")){
                        JSONArray reference_data = references.getJSONArray("reference_data");
                        for (int j = 0;j<reference_data.length();j++){
                            JSONObject refer_info = (JSONObject) reference_data.get(j);
                            if (refer_info.has("url") && refer_info.has("refsource")){
                                Reference ref = Reference.findCVEReferenceByUrlAndRefsource(
                                        refer_info.getString("url"),
                                        refer_info.getString("refsource"),
                                        cveInst._id
                                );

                                if (ref == null){
                                    //String title,String url,String refsource,String[] tags, ObjectId cve_id
                                    ref = new Reference(
                                            refer_info.has("name") ? refer_info.getString("name") : null,
                                            refer_info.has("url") ? refer_info.getString("url") : null,
                                            refer_info.has("refsource") ? refer_info.getString("refsource") : null,
                                            refer_info.has("tags") ? refer_info.get("tags") : null,
                                            cveInst._id
                                    );
                                    ref.save();
                                }
                                else {
                                    // todo :: update reference
                                }

                                ref = Reference.findCVEReferenceByUrlAndRefsource(
                                        refer_info.getString("url"),
                                        refer_info.getString("refsource"),
                                        cveInst._id
                                );

                                if (ref == null){
                                    continue;
                                }
                                Node refNode = storeOrGetNode(ref._id,ref.url,ref.getCollection(),tx,Reference.TYPE);

                                // create topology between ref and cve
                                Topology topology = Topology.findByOriginAndTarget(
                                        cveInst._id,
                                        CVE.COLLECTION,
                                        ref._id,
                                        Reference.COLLECTION
                                );

                                if (topology == null){
                                    topology = new Topology(
                                            "REFERENCE",
                                            cveInst._id,
                                            CVE.COLLECTION,
                                            ref._id,
                                            Reference.COLLECTION,
                                            null
                                    );
                                    topology.save();
                                    relationship("REFERENCE",cveInstNode,refNode,tx);
                                }
                                else {
                                    // todo :: update exist topology for reference
                                }
                            }
                        }
                    }
                }

                JSONObject problemtype = cve.getJSONObject("problemtype");
                JSONArray problemtype_data = problemtype.getJSONArray("problemtype_data");
                for (int j = 0;j<problemtype_data.length();j++){
                    JSONObject cwe = (JSONObject) problemtype_data.get(j);
                    JSONArray cwe_description = cwe.getJSONArray("description");
                    for (int k = 0;k<cwe_description.length();k++){
                        JSONObject cwe_desc = (JSONObject) cwe_description.get(k);
                        if (!cwe_desc.has("value")){
                            continue;
                        }
                        String cwe_id = cwe_desc.getString("value");
                        CWE cweInst = CWE.findCveCweByCweIdAndCveId(cwe_id,cveInst._id);
                        if (cweInst == null){
                            cweInst = new CWE(
                                    cwe_id,
                                    null,
                                    null,
                                    cveInst._id
                            );
                            cweInst.save();
                        }
                        else {
                            // todo :: update cwe
                        }

                        cweInst = CWE.findCveCweByCweIdAndCveId(cwe_id,cveInst._id);
                        if (cweInst == null){
                            continue;
                        }
                        Node cweInstNode = storeOrGetNode(cweInst._id,cweInst.id,cweInst.getCollection(),tx,CWE.TYPE); // todo

                        // create topology between cwe and cve
                        Topology topology = Topology.findByOriginAndTarget(
                                cveInst._id,
                                CVE.COLLECTION,
                                cweInst._id,
                                CWE.COLLECTION
                        );

                        if (topology == null){
                            topology = new Topology(
                                    "CWE",
                                    cveInst._id,
                                    CVE.COLLECTION,
                                    cweInst._id,
                                    CWE.COLLECTION,
                                    null
                            );
                            topology.save();
                            relationship("CWE",cveInstNode,cweInstNode,tx);
                        }
                        else {
                            // todo :: update exist topology
                        }
                    }
                }

                if (cve_info.has("configurations")){
                    JSONObject configurations = cve_info.getJSONObject("configurations");
                    if (configurations.has("nodes")){
                        JSONArray cpes_nodes = configurations.getJSONArray("nodes");
                        for (int j = 0;j<cpes_nodes.length();j++){
                            JSONObject cpes_info = (JSONObject) cpes_nodes.get(j);
                            if (!cpes_info.has("cpe_match")){
                                continue;
                            }
                            JSONArray cpes_matches = cpes_info.getJSONArray("cpe_match");
                            for (int k = 0;k<cpes_matches.length();k++){
                                JSONObject cpe = (JSONObject) cpes_matches.get(k);
                                if (!cpe.has("cpe23Uri")){
                                    continue;
                                }
                                String cpe23Uri = cpe.getString("cpe23Uri");
                                // save or update CPE and relation by CVE
                                CPE cpeInst = CPE.findCveCpeByCpe23UriAndCveId(cpe23Uri,cveInst._id);
                                if (cpeInst == null){
                                    cpeInst = new CPE(
                                            null,
                                            cpe23Uri,
                                            cpe.has("vulnerable") && cpe.getBoolean("vulnerable"),
                                            cveInst._id
                                    );
                                    cpeInst.save();
                                }
                                else {
                                    // todo update cpe
                                }

                                cpeInst = CPE.findCveCpeByCpe23UriAndCveId(cpe23Uri,cveInst._id);

                                if (cpeInst == null){
                                    continue;
                                }
                                Node cpeInstNode = storeOrGetNode(cpeInst._id,cpeInst.cpe23Uri,cpeInst.getCollection(),tx,CPE.TYPE); // todo

                                // create topology between cpe and cve
                                Topology topology = Topology.findByOriginAndTarget(
                                        cveInst._id,
                                        CVE.COLLECTION,
                                        cpeInst._id,
                                        CPE.COLLECTION
                                );

                                if (topology == null){
                                    topology = new Topology(
                                            "SOFTWARE",
                                            cveInst._id,
                                            CVE.COLLECTION,
                                            cpeInst._id,
                                            CPE.COLLECTION,
                                            null
                                    );
                                    topology.save();
                                    relationship("SOFTWARE",cveInstNode,cpeInstNode,tx);
                                }
                                else {
                                    // todo :: update exist topology
                                }

                            }
                        }
                    }
                }


                if (cve_info.has("impact")){
                    JSONObject impacts = cve_info.getJSONObject("impact");
                    if (impacts.has("baseMetricV3")){
                        String severityValue = "";
                        JSONObject baseMetricV3 = impacts.getJSONObject("baseMetricV3");
                        if (baseMetricV3.has("cvssV3")){
                            JSONObject cvssV3 = baseMetricV3.getJSONObject("cvssV3");
                            severityValue = cvssV3.has("baseSeverity") ? cvssV3.getString("baseSeverity") : "";
                            CVSS cvss3 = CVSS.findCveCvssByTitleAndCveId("cvssV3",cveInst._id);
                            if (cvss3 == null){
                                cvss3 = new CVSS(
                                        "cvssV3",
                                        cvssV3.has("version") ? cvssV3.getString("version") : null,
                                        cvssV3.has("vectorString") ? cvssV3.getString("vectorString") : null,
                                        cvssV3.has("confidentialityImpact") ? cvssV3.getString("confidentialityImpact") : null,
                                        cvssV3.has("integrityImpact") ? cvssV3.getString("integrityImpact") : null,
                                        cvssV3.has("availabilityImpact") ? cvssV3.getString("availabilityImpact") : null,
                                        cvssV3.has("baseScore") ? cvssV3.getDouble("baseScore") : 0,
                                        cveInst._id
                                );
                                cvss3.save();
                            }
                            else {
                                // todo update cvssV3
                            }
                        }
                        CVSS cvss3 = CVSS.findCveCvssByTitleAndCveId("cvssV3",cveInst._id);
                        if (cvss3 != null){
                            Node cvss3Node = storeOrGetNode(cvss3._id,cvss3.title,cvss3.getCollection(),tx,CVSS.TYPE);

                            // create topology between cvss and cve
                            Topology topology = Topology.findByOriginAndTarget(
                                    cveInst._id,
                                    CVE.COLLECTION,
                                    cvss3._id,
                                    CVSS.COLLECTION
                            );

                            if (topology == null){
                                topology = new Topology(
                                        "CVSS",
                                        cveInst._id,
                                        CVE.COLLECTION,
                                        cvss3._id,
                                        CVSS.COLLECTION,
                                        null
                                );
                                topology.save();
                                relationship("CVSS",cveInstNode,cvss3Node,tx);
                            }
                            else {
                                // todo :: update exist topology
                            }

                            Metric metric = Metric.findCvssMetricByTitleAndCveId("baseMetricV3",cveInst._id);
                            if (metric == null){
                                metric = new Metric(
                                        "baseMetricV3",
                                        baseMetricV3.has("exploitabilityScore") ? baseMetricV3.getDouble("exploitabilityScore") : 0,
                                        baseMetricV3.has("impactScore") ? baseMetricV3.getDouble("impactScore") : 0,
                                        cveInst._id
                                );
                                metric.save();
                            }
                            else {
                                // todo update metric
                            }

                            metric = Metric.findCvssMetricByTitleAndCveId("baseMetricV3",cveInst._id);
                            if (metric != null){
                                Node metricNode = storeOrGetNode(metric._id,metric.title,metric.getCollection(),tx,Metric.TYPE);

                                // create topology between cvss and metric
                                topology = Topology.findByOriginAndTarget(
                                        cvss3._id,
                                        CVSS.COLLECTION,
                                        metric._id,
                                        Metric.COLLECTION
                                );

                                if (topology == null){
                                    topology = new Topology(
                                            "METRIC",
                                            cvss3._id,
                                            CVSS.COLLECTION,
                                            metric._id,
                                            Metric.COLLECTION,
                                            null
                                    );
                                    topology.save();
                                    relationship("METRIC",cvss3Node,metricNode,tx);
                                }
                                else {
                                    // todo :: update exist topology
                                }
                            }

                            Severity severity = Severity.findByTitleAndCveId("cvssV3",cveInst._id);
                            if (severity == null){
                                severity = new Severity(
                                        "cvssV3",
                                        severityValue,
                                        cveInst._id
                                );
                                severity.save();
                            }
                            else {
                                // todo update severity
                            }

                            severity = Severity.findByTitleAndCveId("cvssV3",cveInst._id);
                            if (severity != null){
                                Node severityNode = storeOrGetNode(severity._id,severity.severity,severity.getCollection(),tx,Severity.TYPE);

                                // create topology between cvss and severity
                                topology = Topology.findByOriginAndTarget(
                                        cvss3._id,
                                        CVSS.COLLECTION,
                                        severity._id,
                                        Severity.COLLECTION
                                );

                                if (topology == null){
                                    topology = new Topology(
                                            "SEVERITY",
                                            cvss3._id,
                                            CVSS.COLLECTION,
                                            severity._id,
                                            Severity.COLLECTION,
                                            null
                                    );
                                    topology.save();
                                    relationship("SEVERITY",cvss3Node,severityNode,tx);
                                }
                                else {
                                    // todo :: update exist topology
                                }
                            }
                        }
                    }
                    if (impacts.has("baseMetricV2")){
                        JSONObject baseMetricV2 = impacts.getJSONObject("baseMetricV2");
                        if (baseMetricV2.has("cvssV2")){
                            JSONObject cvssV2 = baseMetricV2.getJSONObject("cvssV2");
                            CVSS cvss2 = CVSS.findCveCvssByTitleAndCveId("cvssV2",cveInst._id);
                            if (cvss2 == null){
                                cvss2 = new CVSS(
                                        "cvssV2",
                                        cvssV2.has("version") ? cvssV2.getString("version") : null,
                                        cvssV2.has("vectorString") ? cvssV2.getString("vectorString") : null,
                                        cvssV2.has("confidentialityImpact") ? cvssV2.getString("confidentialityImpact") : null,
                                        cvssV2.has("integrityImpact") ? cvssV2.getString("integrityImpact") : null,
                                        cvssV2.has("availabilityImpact") ? cvssV2.getString("availabilityImpact") : null,
                                        cvssV2.has("baseScore") ? cvssV2.getDouble("baseScore") : 0,
                                        cveInst._id
                                );
                                cvss2.save();
                            }
                            else {
                                // todo update cvssV2
                            }
                        }
                        CVSS cvss2 = CVSS.findCveCvssByTitleAndCveId("cvssV2",cveInst._id);
                        if (cvss2 != null){
                            Node cvss2Node = storeOrGetNode(cvss2._id,cvss2.title,cvss2.getCollection(),tx,CVSS.TYPE);

                            // create topology between cvss and cve
                            Topology topology = Topology.findByOriginAndTarget(
                                    cveInst._id,
                                    CVE.COLLECTION,
                                    cvss2._id,
                                    CVSS.COLLECTION
                            );

                            if (topology == null){
                                topology = new Topology(
                                        "CVSS",
                                        cveInst._id,
                                        CVE.COLLECTION,
                                        cvss2._id,
                                        CVSS.COLLECTION,
                                        null
                                );
                                topology.save();
                                relationship("CVSS",cveInstNode,cvss2Node,tx);
                            }
                            else {
                                // todo :: update exist topology
                            }

                            Metric metric = Metric.findCvssMetricByTitleAndCveId("baseMetricV2",cveInst._id);
                            if (metric == null){
                                metric = new Metric(
                                        "baseMetricV2",
                                        baseMetricV2.has("exploitabilityScore") ? baseMetricV2.getDouble("exploitabilityScore") : 0,
                                        baseMetricV2.has("impactScore") ? baseMetricV2.getDouble("impactScore") : 0,
                                        cveInst._id
                                );
                                metric.save();
                            }
                            else {
                                // todo update metric
                            }

                            metric = Metric.findCvssMetricByTitleAndCveId("baseMetricV2",cveInst._id);
                            if (metric != null){
                                Node metricNode = storeOrGetNode(metric._id,metric.title,metric.getCollection(),tx,Metric.TYPE);

                                // create topology between cvss and metric
                                topology = Topology.findByOriginAndTarget(
                                        cvss2._id,
                                        CVSS.COLLECTION,
                                        metric._id,
                                        Metric.COLLECTION
                                );

                                if (topology == null){
                                    topology = new Topology(
                                            "METRIC",
                                            cvss2._id,
                                            CVSS.COLLECTION,
                                            metric._id,
                                            Metric.COLLECTION,
                                            null
                                    );
                                    topology.save();
                                    relationship("METRIC",cvss2Node,metricNode,tx);
                                }
                                else {
                                    // todo :: update exist topology
                                }

                                Severity severity = Severity.findByTitleAndCveId("cvssV2",cveInst._id);
                                if (severity == null){
                                    severity = new Severity(
                                            "cvssV2",
                                            baseMetricV2.has("severity") ? baseMetricV2.getString("severity") : "",
                                            cveInst._id
                                    );
                                    severity.save();
                                }
                                else {
                                    // todo update severity
                                }

                                severity = Severity.findByTitleAndCveId("cvssV2",cveInst._id);
                                if (severity != null){
                                    Node severityNode = storeOrGetNode(severity._id,severity.severity,severity.getCollection(),tx,Severity.TYPE);

                                    // create topology between metric and severity
                                    topology = Topology.findByOriginAndTarget(
                                            metric._id,
                                            Metric.COLLECTION,
                                            severity._id,
                                            Severity.COLLECTION
                                    );

                                    if (topology == null){
                                        topology = new Topology(
                                                "SEVERITY",
                                                metric._id,
                                                Metric.COLLECTION,
                                                severity._id,
                                                Severity.COLLECTION,
                                                null
                                        );
                                        topology.save();
                                        relationship("SEVERITY",metricNode,severityNode,tx);
                                    }
                                    else {
                                        // todo :: update exist topology
                                    }
                                }
                            }
                        }
                    }
                }
            }

            startIndex += vulnerabilitiesResult.getInt("resultsPerPage");
            System.out.println(startIndex);
        }
    }
}
