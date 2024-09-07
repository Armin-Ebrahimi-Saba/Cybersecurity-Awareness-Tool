package services.middlewares.vulnerabilities;

import org.json.JSONObject;
import services.http.Get;

public class VulnerabilitiesAPI {

    private static final String route = "https://services.nvd.nist.gov/rest/json/cves/1.0/";
    private static final int resultsPerPage = 100;

    public static JSONObject get(int startIndex){
        return Get.sendRequest(route + "?startIndex=" + startIndex + "&resultsPerPage=" + resultsPerPage);
    }
}
