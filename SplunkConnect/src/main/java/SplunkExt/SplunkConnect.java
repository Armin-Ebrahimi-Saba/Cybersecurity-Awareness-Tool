package SplunkExt;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.splunk.Event;
import com.splunk.JobExportArgs;
import com.splunk.MultiResultsReaderXml;
import com.splunk.SearchResults;
import com.splunk.Service;
import com.splunk.ServiceArgs;

public class SplunkConnect {
    public static void main(String[] args) {
        // 1- connect to splunk
        Service service = ConnectSplunk();
        /* these lines should replace the above line in the case someone wants to connect to remote host 
        ServiceArgs loginArgs = new ServiceArgs();
        loginArgs.setUsername("YourUsername");
        loginArgs.setPassword("YourPassword");
        loginArgs.setHost("Remote hosts IP address");
        loginArgs.setPort(0); // replace your port here
        Service service = Service.connect(loginArgs);
        */
        // 2- read features from file
        ArrayList<String> usefulFeatures = getUsefulFeatures();
        // 3- connect to mongodb
        Subject logHolder = new Subject();
        Observer observer = new Observer(logHolder);
        Thread DBHandler = new Thread(() -> {
            ConnectMongoDB(observer);
        });
        Thread jobHandler = new Thread(() -> {
            // 4- create query 
            String query = 
                "search source=\"fortiweb.csv\" host=\"LAPTOP-NAJJ6E2C\" sourcetype=\"csv\" index = \"fortiweb\" | table ";
            for (String feature : usefulFeatures)
                query += feature + ", ";
            query = query.substring(0, query.length() - 2) + " | where mvcount(attack_type) > 0";
            System.out.println(query);
            // 5- create job (splunk api)
            while (true) {
                createJob(service, query, logHolder);
                break;
            }
        });
        try {
            jobHandler.start();
            jobHandler.join();
            DBHandler.start();
            DBHandler.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    /*
     * this function uses splunk's JobAPI to send queries and get results 
     * @service the connection between the application and splunk
     * @query the query in splunks query language to be executed for collecting data 
     * @logHolder object to be observed for changes so that an observer can notify a MongoDB's agent for collecting data
     */
    static int start = 10;
    private static void createJob(Service service, String query, Subject logHolder) {
        JobExportArgs jobargs = new JobExportArgs();
        InputStream exportSearch = service.export(query, jobargs);
        jobargs.setSearchMode(JobExportArgs.SearchMode.NORMAL);
        System.out.println("Job finished");
        // Get the search results and use the built-in XML parser to display them
        MultiResultsReaderXml multiResultsReader;
        int count = 0;
        try {
            multiResultsReader = new MultiResultsReaderXml(exportSearch);
            Set<ConcurrentHashMap<String, Object>> events = ConcurrentHashMap.newKeySet();
            for (SearchResults searchResults: multiResultsReader) {
                for (Event event : searchResults) {
                    count++;
                    events.add(new ConcurrentHashMap<>(event));
                    // System.out.println(event.get("attack_type"));
                }
            }
            logHolder.updateSet(events);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(count);
    }

    /*
     * connects to splunk using username and password
     */
    static Service ConnectSplunk() {
        ServiceArgs loginArgs = new ServiceArgs();
        loginArgs.setUsername("");
        loginArgs.setPassword("");
        loginArgs.setHost("");
        loginArgs.setPort();
        // Create a Service instance and log in with the argument map
        Service service = Service.connect(loginArgs);
        return service;
    }
    
    /*
     * connects to mongoDB and normalizes and collects data collected from splunk  
     * @observe ran observer that watches for the data collected from the splunk enterperise
     */
    static void ConnectMongoDB(Observer observer) {
        // String username = "";
        // String password = "";
        // try {
        //     password = URLEncoder.encode(password, "UTF-8");
        //     username = URLEncoder.encode(username, "UTF-8");
        // } catch (UnsupportedEncodingException e) {
        //     e.printStackTrace();
        // }
        String connectionString = "mongodb://localhost:27017";
        MongoClientSettings settings = MongoClientSettings.builder()
        .applyConnectionString(new ConnectionString(connectionString))
        .build();
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            try {
                MongoDatabase database = mongoClient.getDatabase("cygraph");
                MongoCollection<Document> logsCollection = database.getCollection("Logs");
                logsCollection.deleteMany(new BasicDBObject());
                Set<ConcurrentHashMap<String, Object>> newLogs;
                while (true) {
                    if ((newLogs = observer.getUpdate()) != null) {
                        for (ConcurrentHashMap<String, Object> map : newLogs)
                            logsCollection.insertOne(new Document(map));
                        break;
                    } 
                    Thread.sleep(1000);
                }
                // finding attacks and building a collection for them
                MongoCollection<Document> attacksCollection = database.getCollection("attacks");
                attacksCollection.deleteMany(new BasicDBObject());
                logsCollection.aggregate(Arrays.asList(
                    Aggregates.lookup("Machines", "dst", "ip", "matchedDocuments"),
                    Aggregates.match(Filters.ne("matchedDocuments", Arrays.asList()))
                )).into(new ArrayList<>()).forEach(document -> attacksCollection.insertOne(document));
                // add attack_types with their severity_level to monogodb to be exported later
                // Your aggregation query as a string
                BasicDBObject groupStage = new BasicDBObject("$group", new Document("_id", new Document("attack_type", "$attack_type")
                .append("severity_level", "$severity_level")));
                BasicDBObject projectStage = new BasicDBObject("$project", new Document("_id", 0)
                .append("ID", "$_id.attack_type")
                .append("Name", "$_id.attack_type")
                .append("Description", "N/A")
                .append("Likelihood Of Attack", "N/A")
                .append("Typical Severity", "$_id.severity_level")
                .append("Taxonomy Mappings", "N/A")
                .append("Status", "N/A"));
                BasicDBObject[] pipeline = { groupStage, projectStage };
                // Execute the aggregation query
                List<Document> aggregationList = new ArrayList<>();
                attacksCollection.aggregate(Arrays.asList(pipeline)).into(aggregationList);
                // Create a new collection for the aggregation result
                MongoCollection<Document> attackPatternCollection =
                    database.getCollection("attack_patterns");
                attackPatternCollection.insertOne(new Document());
                attackPatternCollection.deleteMany(new BasicDBObject());
                attackPatternCollection.insertMany(aggregationList);

                // creating collection for logs that mathces the format that of cygraph (network.csv)
                projectStage = new BasicDBObject("$project", new Document("_id", 0)
                .append("ID", new Document("$concat", Arrays.asList("$dst", "-", "$src", "-", "$dst_port", "-", "$src_port", "-", "N/A")))
                .append("Source_IP", "$src")
                .append("Source_Port", "$src_port")
                .append("Destination_IP", "$dst")
                .append("Destination_Port", "$dst_port")
                .append("Protocol", "$service")
                .append("Timestamp", "$_time")
                .append("Attack_Type", "$attack_type")
                .append("Stage", "N/A"));
                aggregationList.clear();
                attacksCollection.aggregate(Arrays.asList(projectStage)).into(aggregationList);
                MongoCollection<Document> networkCollection = 
                    database.getCollection("network");
                networkCollection.deleteMany(new BasicDBObject());
                networkCollection.insertMany(aggregationList);
                /* If you want to create the attack patterns collection, uncomment the lines below
                MongoCollection<Document> attackPatternCollection =
                    database.getCollection("AttackPattern");
                attackPatternCollection.deleteMany(new BasicDBObject());
                FindIterable<Document> documents = attacksCollection.find(new Document());
                MongoCursor<Document> cursor = documents.iterator();
                HashSet<String> attackSeverity = new HashSet<>();
                while (cursor.hasNext()) {
                    Document document = cursor.next();
                    String pair = document.get("attack_type").toString() +
                                    document.get("severity_level").toString();
                    if (!attackSeverity.contains(pair)) {
                        attackSeverity.add(pair);
                        Document newDocument = new Document();
                        newDocument.append("ID", document.get("N/A"));
                        newDocument.append("Name", document.get("attack_type"));
                        newDocument.append("Description", "N/A");
                        newDocument.append("Likelihood Of Attack", "N/A");
                        newDocument.append("Typical Severity", document.get("severity_level"));
                        newDocument.append("Taxonomy Mappings", "N/A");
                        newDocument.append("Status", "N/A");
                        attackPatternCollection.insertOne(newDocument);
                    }
                }
                */

            } catch (MongoException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("DB closed");
    }

    /*
     * extract recorded useful data according to the useful features in features.xlsx
     */
    static ArrayList<String> getUsefulFeatures() {
        ArrayList<String> features = new ArrayList<>();
            FileInputStream file;
            try {
                file = new FileInputStream(new File("src\\main\\java\\SplunkExt\\features.xlsx"));
                //Create Workbook instance holding reference to .xlsx file
                XSSFWorkbook workbook = new XSSFWorkbook(file);
                //Get first/desired sheet from the workbook
                XSSFSheet sheet = workbook.getSheetAt(0);
                //Iterate through each rows one by one
                Row row = sheet.getRow(0);
                Iterator<Cell> cellIterator = row.cellIterator();
                while (cellIterator.hasNext()) 
                {
                    features.add(cellIterator.next().getStringCellValue());
                    // System.out.println(cellIterator.next().getStringCellValue());
                }
                file.close();
                workbook.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        return features;
    }
}

/* just for simulating real-time logging, if you are using it to connect to an active logging source, this piece of code should be used
private static void createJob(Service service, String query, Subject logHolder) {
    String range = "| Head " + (start + 10) + "| tail " + start;
    start += 10;
    JobArgs jobargs = new JobArgs();
    jobargs.setExecutionMode(JobArgs.ExecutionMode.NORMAL);
    Job job = service.getJobs().create(query + range, jobargs);
    while (!job.isDone()) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    System.out.println("Job finished");
    // Get the search results and use the built-in XML parser to display them
    InputStream resultsNormalSearch =  job.getResults();
    ResultsReaderXml resultsReaderNormalSearch;
    int count = 0;
    try {
        resultsReaderNormalSearch = new ResultsReaderXml(resultsNormalSearch);
        Set<ConcurrentHashMap<String, Object>> events = ConcurrentHashMap.newKeySet();
        HashMap<String, String> event;
        while ((event = resultsReaderNormalSearch.getNextEvent()) != null) {
            events.add(new ConcurrentHashMap<String, Object>(event));
            count++;
            // System.out.println("\n****************EVENT****************\n");
            // for (String key: event.keySet())
            // System.out.println("   " + key + ":  " + event.get(key));
            }
        logHolder.updateSet(events);
        // just for simulating real-time logging
        Thread.sleep(500);
    } catch (Exception e) {
        e.printStackTrace();
    }
    System.out.println(count);
    // Get properties of the completed job (for debugging)
    //System.out.println("\nSearch job properties\n---------------------");
    //System.out.println("Search job ID:         " + job.getSid());
    //System.out.println("The number of events:  " + job.getEventCount());
    //System.out.println("The number of results: " + job.getResultCount());
    //System.out.println("Search duration:       " + job.getRunDuration() + " seconds");
    //System.out.println("This job expires in:   " + job.getTtl() + " seconds");
    // System.out.println(query);
}
*/
