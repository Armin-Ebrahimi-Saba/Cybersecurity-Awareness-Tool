package services.adapters;

import models.*;
import models.missions.*;
import models.vulnerabilities.*;
import org.bson.types.ObjectId;

public class ModelAdapter {
    public static Base getNode(String id,String collection,String type){
        ObjectId mainId = new ObjectId(id);
        switch (collection){
            case Vulnerability.COLLECTION: {
                switch (type){
                    case CPE.TYPE : return CPE.findByMainId(mainId);
                    case CVE.TYPE: return CVE.findByMainId(mainId);
                    case CVSS.TYPE: return CVSS.findByMainId(mainId);
                    case CWE.TYPE: return CWE.findByMainId(mainId);
                    case Metric.TYPE: return Metric.findByMainId(mainId);
                    case Reference.TYPE: return Reference.findByMainId(mainId);
                    case Severity.TYPE: return Severity.findByMainId(mainId);
                    default: throw new IllegalStateException("Unexpected value: " + type);
                }
            }
            case AttackPattern.COLLECTION: return AttackPattern.findByMainId(mainId);
            case Firewall.COLLECTION: return Firewall.findByMainId(mainId);
            case Machine.COLLECTION: return Machine.findByMainId(mainId);
            case Snort.COLLECTION: return Snort.findByMainId(mainId);
            case Subnet.COLLECTION: return Subnet.findByMainId(mainId);
            case Topology.COLLECTION: return Topology.findByMainId(mainId);
            case MissionInformation.COLLECTION: return MissionInformation.findByMainId(mainId);
            case MissionTask.COLLECTION: return MissionTask.findByMainId(mainId);
            case CyberAsset.COLLECTION: return CyberAsset.findByMainId(mainId);
            case MissionObjective.COLLECTION: return MissionObjective.findByMainId(mainId);
            default: throw new IllegalStateException("Unexpected value: " + collection);
        }
    }
}
