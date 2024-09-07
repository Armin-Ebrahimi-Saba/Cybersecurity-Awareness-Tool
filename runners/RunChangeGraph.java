package runners;


import models.Firewall;
import models.Machine;
import models.Subnet;
import org.neo4j.graphdb.Node;
import services.db.Neo4jDbConnection;

import java.io.IOException;
import java.util.HashMap;

public class RunChangeGraph extends Neo4jDbConnection {
    public void main(String[] args) {
        try {

            Firewall external = Firewall.findById("externalFirewall");
            Node externalFirewall = getNodeBySearchOnIdentifier(String.valueOf(external._id));

            Subnet dmz = Subnet.findById("DMZ");
            Node dmzSubnet = getNodeBySearchOnIdentifier(String.valueOf(dmz._id));

            removeRelationshipAndRegister(externalFirewall,dmzSubnet,"ROUTES");

            Firewall internal = Firewall.findById("internalFirewall");
            Node internalFirewall = getNodeBySearchOnIdentifier(String.valueOf(internal._id));

            HashMap<String, String> f1 = new HashMap<>();
            f1.put("name", "ROUTES");

            relatingAndRegister(internalFirewall,dmzSubnet,f1);

            Subnet subnetInternet = Subnet.findById("Internet");
            Node internet = getNodeBySearchOnIdentifier(String.valueOf(subnetInternet._id));

            HashMap<String, String> f2 = new HashMap<>();
            f2.put("name", "INN");

            Machine machine1 = Machine.findById("8.0.6.4-8.6.0.1-0-0-0");
            Node firstNode = storeNodeAndRegister(machine1,subnetInternet.title,internet,f2);

            relatingAndRegister(firstNode,internet,f2);

            Machine machine2 = Machine.findById("192.168.3.10-239.2.11.71-53569-8662-17");
            Node secondNode = storeNodeAndRegister(machine2,machine2.title,internet,f2);

            relatingAndRegister(internet,secondNode,f2);

            removeNodeAndRelationsAndRegister(dmz);

            Machine machine3 = Machine.findById("LocalDnsServer");
            Node local = getNodeBySearchOnIdentifier(String.valueOf(machine3._id));

            dmzSubnet = storeNodeAndRegister(dmz,external.title,externalFirewall,f1);

            relatingAndRegister(dmzSubnet,local,f2);
            relatingAndRegister(local,dmzSubnet,f2);


        } catch (IOException e) {
            e.printStackTrace();
        }
        shutDown();
    }
}
