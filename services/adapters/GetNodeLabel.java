package services.adapters;

import models.*;
import models.vulnerabilities.Vulnerability;

public class GetNodeLabel {
    public static String getLabel(String collection){
        switch (collection){
            case Vulnerability.COLLECTION : return "Vulnerability".toUpperCase();
            case Topology.COLLECTION : return "Topology".toUpperCase();
            case Snort.COLLECTION: return "Alert".toUpperCase();
            default : return collection.toUpperCase().substring(0,collection.length()-1);
        }
    }
}
