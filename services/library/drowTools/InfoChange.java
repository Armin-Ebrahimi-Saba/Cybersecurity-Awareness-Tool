package services.library.drowTools;

import org.bson.Document;

import javax.swing.*;
import java.util.ArrayList;

public class InfoChange extends JFrame {

    public InfoChange(ArrayList<Document> temp) {

        JTextArea textArea= new JTextArea();
        for(Document document: temp) {
            if (document.get("document_type").equals("node")) {
                if (document.get("operation").equals("add")) {
                    textArea.append(" Add Node :\n");
                    textArea.append("\tName : " + document.get("name") + "\tCollection : " + document.get("collection") + "\n");
                    Document relation = (Document) document.get("relation");
                    textArea.append("\tRelation Name : " + relation.get("relationName") + "\tStart Node : " + relation.get("startNodeName") + "\tEnd Node : " + relation.get("endNodeName") + "\n");
                }
                if (document.get("operation").equals("delete")) {
                    textArea.append(" Delete Node :\n");
                    textArea.append("\tName : " + document.get("name") + "\tCollection : " + document.get("collection") + "\n");
                    ArrayList<Document> relations = (ArrayList<Document>) document.get("relations");
                    if(!relations.isEmpty())
                        textArea.append("\tDelete Relations :\n");
                    for (Document relation: relations) {
                        textArea.append("\t\tRelation Name : " + relation.get("relationName") + "\tStart Node : " + relation.get("startNodeName") + "\tEnd Node : " + relation.get("endNodeName") + "\n");
                    }
                }
            }
            if (document.get("document_type").equals("relation")) {
                if (document.get("operation").equals("add")) {
                    textArea.append(" Add Relation :\n");
                    textArea.append("\tRelation Name : " + document.get("relationName") + "\tStart Node : " + document.get("startNodeName") + "\tEnd Node : " + document.get("endNodeName") + "\n");
                }
                if (document.get("operation").equals("delete")) {
                    textArea.append(" Delete Relation :\n");
                    textArea.append("\tRelation Name : " + document.get("relationName") + "\tStart Node : " + document.get("startNodeName") + "\tEnd Node : " + document.get("endNodeName") + "\n");
                }
            }
        }

        textArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(textArea);
        add(scroll);

        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setVisible(true);
        setLocation(200,300);
        setSize(800,200);
    }

}
