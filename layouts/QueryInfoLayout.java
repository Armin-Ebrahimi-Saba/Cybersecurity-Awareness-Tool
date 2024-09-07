package layouts;

import models.*;
import models.missions.*;
import models.vulnerabilities.*;
import services.adapters.GetNodeLabel;
import services.library.drowTools.FilterPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

public class QueryInfoLayout extends JFrame {

    public QueryInfoLayout(int numberOfRows, int numberOfColumns, int numberOfNodes, int numberOfRelations, int largestNodeDegree, HashMap<String, Color> colors)
    {
        setTitle("Visualize Query Result");
        Container cp = getContentPane();
        cp.setLayout(new FlowLayout());
        JTextArea textArea = new JTextArea(
                "\nQuery result has " + numberOfRows + " rows and "+ numberOfColumns + " columns :\n"
                + "\t" + numberOfNodes + " unique nodes\n"
                + "\t" + numberOfRelations + " unique relationships\n"
                +"Visualized Graph :\n"
                +"\t" + "Number of nodes : " + "  " + numberOfNodes +"\n"
                +"\t" + "Number of edges : " + "  " + numberOfRelations +"\n"
                +"\t" + "Average node degree : " + "  " + String.format("%.02f",((float)numberOfRelations/(numberOfNodes/2))) +"\n"
                +"\t" + "Largest node degree : " + "  " + largestNodeDegree
        );
        textArea.setEditable(false);
        textArea.setPreferredSize(new Dimension(430,150));
        cp.add(textArea);

        Label col_labels = new Label(".: Node Labels :.");
        cp.add(col_labels,BorderLayout.WEST);

        JPanel collectionsPanel = new JPanel();
        collectionsPanel.setLayout(new GridLayout(7,0));
        collectionsPanel.setPreferredSize(new Dimension(430,200));

        Label cpe_label = new Label(GetNodeLabel.getLabel(Vulnerability.COLLECTION)+" | "+ CPE.TYPE.toUpperCase());
        cpe_label.setForeground(colors.get("CPE"));
        collectionsPanel.add(cpe_label,BorderLayout.WEST);

        Label cvss_label = new Label(GetNodeLabel.getLabel(Vulnerability.COLLECTION)+" | "+ CVSS.TYPE.toUpperCase());
        cvss_label.setForeground(colors.get("CVSS"));
        collectionsPanel.add(cvss_label,BorderLayout.WEST);

        Label metric_label = new Label(GetNodeLabel.getLabel(Vulnerability.COLLECTION)+" | "+ Metric.TYPE.toUpperCase());
        metric_label.setForeground(colors.get("Metric"));
        collectionsPanel.add(metric_label,BorderLayout.WEST);

        Label Severity_label = new Label(GetNodeLabel.getLabel(Vulnerability.COLLECTION)+" | "+ Severity.TYPE.toUpperCase());
        Severity_label.setForeground(colors.get("Severity"));
        collectionsPanel.add(Severity_label,BorderLayout.WEST);

        Label CVE_label = new Label(GetNodeLabel.getLabel(Vulnerability.COLLECTION)+" | "+ CVE.TYPE.toUpperCase());
        CVE_label.setForeground(colors.get("CVE"));
        collectionsPanel.add(CVE_label,BorderLayout.WEST);

        Label Reference_label = new Label(GetNodeLabel.getLabel(Vulnerability.COLLECTION)+" | "+ Reference.TYPE.toUpperCase());
        Reference_label.setForeground(colors.get("Reference"));
        collectionsPanel.add(Reference_label,BorderLayout.WEST);

        Label CWE_label = new Label(GetNodeLabel.getLabel(Vulnerability.COLLECTION)+" | "+ CWE.TYPE.toUpperCase());
        CWE_label.setForeground(colors.get("CWE"));
        collectionsPanel.add(CWE_label,BorderLayout.WEST);

        Label AttackPattern_label = new Label(GetNodeLabel.getLabel(AttackPattern.COLLECTION));
        AttackPattern_label.setForeground(colors.get("AttackPattern"));
        collectionsPanel.add(AttackPattern_label,BorderLayout.WEST);

        Label Machine_label = new Label(GetNodeLabel.getLabel(Machine.COLLECTION));
        Machine_label.setForeground(colors.get("Machine"));
        collectionsPanel.add(Machine_label,BorderLayout.WEST);

        Label Firewall_label = new Label(GetNodeLabel.getLabel(Firewall.COLLECTION));
        Firewall_label.setForeground(colors.get("Firewall"));
        collectionsPanel.add(Firewall_label,BorderLayout.WEST);

        Label Subnet_label = new Label(GetNodeLabel.getLabel(Subnet.COLLECTION));
        Subnet_label.setForeground(colors.get("Subnet"));
        collectionsPanel.add(Subnet_label,BorderLayout.WEST);

        Label Snort_label = new Label(GetNodeLabel.getLabel(Snort.COLLECTION));
        Snort_label.setForeground(colors.get("Snort"));
        collectionsPanel.add(Snort_label,BorderLayout.WEST);

        Label MissionTask_label = new Label(GetNodeLabel.getLabel(MissionTask.COLLECTION));
        MissionTask_label.setForeground(colors.get("MissionTask"));
        collectionsPanel.add(MissionTask_label,BorderLayout.WEST);

        Label MissionInformation_label = new Label(GetNodeLabel.getLabel(MissionInformation.COLLECTION));
        MissionInformation_label.setForeground(colors.get("MissionInformation"));
        collectionsPanel.add(MissionInformation_label,BorderLayout.WEST);

        Label CyberAsset_label = new Label(GetNodeLabel.getLabel(CyberAsset.COLLECTION));
        CyberAsset_label.setForeground(colors.get("CyberAsset"));
        collectionsPanel.add(CyberAsset_label,BorderLayout.WEST);

        Label MissionObj_label = new Label(GetNodeLabel.getLabel(MissionObjective.COLLECTION));
        MissionObj_label.setForeground(colors.get("MissionObjective"));
        collectionsPanel.add(MissionObj_label,BorderLayout.WEST);

        cp.add(collectionsPanel,BorderLayout.WEST);

        Button cancel = new Button("Cancel");
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        cp.add(cancel,BorderLayout.CENTER);

        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setVisible(true);
        setSize(445,450);
        setLocation(1000,276);
    }

    public QueryInfoLayout(HashMap<String, FilterPanel> filterPanels, int numberOfRows, int numberOfColumns, int numberOfNodes, int numberOfRelations, int largestNodeDegree)
    {
        setTitle("Visualize Query Result");
        Container cp = getContentPane();
        cp.setLayout(new FlowLayout());
        JTextArea textArea = new JTextArea(
                "\nQuery result has " + numberOfRows + " rows and "+ numberOfColumns + " columns :\n"
                        + "\t" + numberOfNodes + " unique nodes\n"
                        + "\t" + numberOfRelations + " unique relationships\n"
                        +"Visualized Graph :\n"
                        +"\t" + "Number of nodes : " + "  " + numberOfNodes +"\n"
                        +"\t" + "Number of edges : " + "  " + numberOfRelations +"\n"
                        +"\t" + "Average node degree : " + "  " + String.format("%.02f",((float)numberOfRelations/(numberOfNodes/2))) +"\n"
                        +"\t" + "Largest node degree : " + "  " + largestNodeDegree
        );
        textArea.setEditable(false);
        textArea.setPreferredSize(new Dimension(430,150));
        cp.add(textArea);

        Label col_labels = new Label(".: Node Labels :.");
        cp.add(col_labels,BorderLayout.WEST);

        JPanel collectionsPanel = new JPanel();
        collectionsPanel.setLayout(new GridLayout(7,0));
        collectionsPanel.setPreferredSize(new Dimension(430,200));

        Label cpe_label = new Label(GetNodeLabel.getLabel(Vulnerability.COLLECTION)+" | "+ CPE.TYPE.toUpperCase());
        cpe_label.setForeground(filterPanels.get(GetNodeLabel.getLabel(Vulnerability.COLLECTION) + " | " + CPE.TYPE.toUpperCase()).getCanvas().getColor());
        collectionsPanel.add(cpe_label,BorderLayout.WEST);

        Label cvss_label = new Label(GetNodeLabel.getLabel(Vulnerability.COLLECTION)+" | "+ CVSS.TYPE.toUpperCase());
        cvss_label.setForeground(filterPanels.get(GetNodeLabel.getLabel(Vulnerability.COLLECTION) + " | " + CVSS.TYPE.toUpperCase()).getCanvas().getColor());
        collectionsPanel.add(cvss_label,BorderLayout.WEST);

        Label metric_label = new Label(GetNodeLabel.getLabel(Vulnerability.COLLECTION)+" | "+ Metric.TYPE.toUpperCase());
        metric_label.setForeground(filterPanels.get(GetNodeLabel.getLabel(Vulnerability.COLLECTION) + " | " + Metric.TYPE.toUpperCase()).getCanvas().getColor());
        collectionsPanel.add(metric_label,BorderLayout.WEST);

        Label Severity_label = new Label(GetNodeLabel.getLabel(Vulnerability.COLLECTION)+" | "+ Severity.TYPE.toUpperCase());
        Severity_label.setForeground(filterPanels.get(GetNodeLabel.getLabel(Vulnerability.COLLECTION) + " | " + Severity.TYPE.toUpperCase()).getCanvas().getColor());
        collectionsPanel.add(Severity_label,BorderLayout.WEST);

        Label CVE_label = new Label(GetNodeLabel.getLabel(Vulnerability.COLLECTION)+" | "+ CVE.TYPE.toUpperCase());
        CVE_label.setForeground(filterPanels.get(GetNodeLabel.getLabel(Vulnerability.COLLECTION) + " | " + CVE.TYPE.toUpperCase()).getCanvas().getColor());
        collectionsPanel.add(CVE_label,BorderLayout.WEST);

        Label Reference_label = new Label(GetNodeLabel.getLabel(Vulnerability.COLLECTION)+" | "+ Reference.TYPE.toUpperCase());
        Reference_label.setForeground(filterPanels.get(GetNodeLabel.getLabel(Vulnerability.COLLECTION) + " | " + Reference.TYPE.toUpperCase()).getCanvas().getColor());
        collectionsPanel.add(Reference_label,BorderLayout.WEST);

        Label CWE_label = new Label(GetNodeLabel.getLabel(Vulnerability.COLLECTION)+" | "+ CWE.TYPE.toUpperCase());
        CWE_label.setForeground(filterPanels.get(GetNodeLabel.getLabel(Vulnerability.COLLECTION) + " | " + CWE.TYPE.toUpperCase()).getCanvas().getColor());
        collectionsPanel.add(CWE_label,BorderLayout.WEST);

        Label AttackPattern_label = new Label(GetNodeLabel.getLabel(AttackPattern.COLLECTION));
        AttackPattern_label.setForeground(filterPanels.get(GetNodeLabel.getLabel(AttackPattern.COLLECTION)).getCanvas().getColor());
        collectionsPanel.add(AttackPattern_label,BorderLayout.WEST);

        Label Machine_label = new Label(GetNodeLabel.getLabel(Machine.COLLECTION));
        Machine_label.setForeground(filterPanels.get(GetNodeLabel.getLabel(Machine.COLLECTION)).getCanvas().getColor());
        collectionsPanel.add(Machine_label,BorderLayout.WEST);

        Label Firewall_label = new Label(GetNodeLabel.getLabel(Firewall.COLLECTION));
        Firewall_label.setForeground(filterPanels.get(GetNodeLabel.getLabel(Firewall.COLLECTION)).getCanvas().getColor());
        collectionsPanel.add(Firewall_label,BorderLayout.WEST);

        Label Subnet_label = new Label(GetNodeLabel.getLabel(Subnet.COLLECTION));
        Subnet_label.setForeground(filterPanels.get(GetNodeLabel.getLabel(Subnet.COLLECTION)).getCanvas().getColor());
        collectionsPanel.add(Subnet_label,BorderLayout.WEST);

        Label Snort_label = new Label(GetNodeLabel.getLabel(Snort.COLLECTION));
        Snort_label.setForeground(filterPanels.get(GetNodeLabel.getLabel(Snort.COLLECTION)).getCanvas().getColor());
        collectionsPanel.add(Snort_label,BorderLayout.WEST);

        Label MissionTask_label = new Label(GetNodeLabel.getLabel(MissionTask.COLLECTION));
        MissionTask_label.setForeground(filterPanels.get(GetNodeLabel.getLabel(MissionTask.COLLECTION)).getCanvas().getColor());
        collectionsPanel.add(MissionTask_label,BorderLayout.WEST);

        Label MissionInformation_label = new Label(GetNodeLabel.getLabel(MissionInformation.COLLECTION));
        MissionInformation_label.setForeground(filterPanels.get(GetNodeLabel.getLabel(MissionInformation.COLLECTION)).getCanvas().getColor());
        collectionsPanel.add(MissionInformation_label,BorderLayout.WEST);

        Label CyberAsset_label = new Label(GetNodeLabel.getLabel(CyberAsset.COLLECTION));
        CyberAsset_label.setForeground(filterPanels.get(GetNodeLabel.getLabel(CyberAsset.COLLECTION)).getCanvas().getColor());
        collectionsPanel.add(CyberAsset_label,BorderLayout.WEST);

        Label MissionObj_label = new Label(GetNodeLabel.getLabel(MissionObjective.COLLECTION));
        MissionObj_label.setForeground(filterPanels.get(GetNodeLabel.getLabel(MissionObjective.COLLECTION)).getCanvas().getColor());
        collectionsPanel.add(MissionObj_label,BorderLayout.WEST);

        cp.add(collectionsPanel,BorderLayout.WEST);

        Button cancel = new Button("Cancel");
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        cp.add(cancel,BorderLayout.CENTER);

        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setVisible(true);
        setSize(445,450);
        setLocation(1000,276);
    }
}
