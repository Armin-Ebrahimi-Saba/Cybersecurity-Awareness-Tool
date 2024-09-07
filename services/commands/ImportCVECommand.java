package services.commands;

import models.vulnerabilities.CVE;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

public class ImportCVECommand {

    private static final String CVE_FILE  = "src/main/java/services/library/vulnerabilities/cve.csv";


    public void fire() {
        String line;
        String splitBy = ",";
        HashMap<String,Integer> columns = new HashMap<>();
        boolean flag = false;
        try {
            BufferedReader br = new BufferedReader(new FileReader(CVE_FILE));

            while ((line = br.readLine()) != null) {
                String[] information = line.split(splitBy);
                if (!flag){
                    for (String col : information) {
                        columns.put(col, Arrays.asList(information).indexOf(col));
                    }
                    flag = true;
                }
                else if (information.length == columns.size()){
                    CVE cve = CVE.findById(information[columns.get("Name")]);
                    if (cve == null){
                        cve = new CVE(
                                information[columns.get("Name")],
                                information[columns.get("Name")],
                                information[columns.get("Description")]
                        );
                    }
                    else {
                        cve.description = information[columns.get("Description")];
                    }
                    cve.save();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
