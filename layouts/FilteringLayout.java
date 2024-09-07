package layouts;

import models.*;
import models.missions.*;
import models.vulnerabilities.*;
import services.adapters.GetNodeLabel;
import services.adapters.QueryBuilder;
import services.library.drowTools.FilterPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

public class FilteringLayout extends JFrame {

    public FilteringLayout(String query, HashMap<String, Color> colors) {

        HashMap<String, FilterPanel> filterPanels = new HashMap<>();

        setTitle("Set Filters");
        Container container = getContentPane();
        container.setLayout(new FlowLayout());

        JPanel cp = new JPanel();
        cp.setLayout(new GridLayout(10,2,10,10));
        cp.setPreferredSize(new Dimension(1000,400));

        FilterPanel FVCPE = new FilterPanel(GetNodeLabel.getLabel(Vulnerability.COLLECTION) + " | "
                + CPE.TYPE.toUpperCase(), colors.get("CPE"));
        cp.add(FVCPE);
        filterPanels.put(FVCPE.getPanel_name(), FVCPE);

        FilterPanel FVCVSS = new FilterPanel(GetNodeLabel.getLabel(Vulnerability.COLLECTION) + " | "
                + CVSS.TYPE.toUpperCase(), colors.get("CVSS"));
        cp.add(FVCVSS);
        filterPanels.put(FVCVSS.getPanel_name(), FVCVSS);

        FilterPanel FVMetric = new FilterPanel(GetNodeLabel.getLabel(Vulnerability.COLLECTION) + " | "
                + Metric.TYPE.toUpperCase(), colors.get("Metric"));
        cp.add(FVMetric);
        filterPanels.put(FVMetric.getPanel_name(), FVMetric);

        FilterPanel FVSeverity = new FilterPanel(GetNodeLabel.getLabel(Vulnerability.COLLECTION) + " | "
                + Severity.TYPE.toUpperCase(), colors.get("Severity"));
        cp.add(FVSeverity);
        filterPanels.put(FVSeverity.getPanel_name(), FVSeverity);

        FilterPanel FVCVE = new FilterPanel(GetNodeLabel.getLabel(Vulnerability.COLLECTION) + " | "
                + CVE.TYPE.toUpperCase(), colors.get("CVE"));
        cp.add(FVCVE);
        filterPanels.put(FVCVE.getPanel_name(), FVCVE);

        FilterPanel FVRef = new FilterPanel(GetNodeLabel.getLabel(Vulnerability.COLLECTION) + " | "
                + Reference.TYPE.toUpperCase(), colors.get("Reference"));
        cp.add(FVRef);
        filterPanels.put(FVRef.getPanel_name(), FVRef);

        FilterPanel FVCWE = new FilterPanel(GetNodeLabel.getLabel(Vulnerability.COLLECTION) + " | "
                + CWE.TYPE.toUpperCase(), colors.get("CWE"));
        cp.add(FVCWE);
        filterPanels.put(FVCWE.getPanel_name(), FVCWE);

        FilterPanel FVOther = new FilterPanel("VULNERABILITY | OTHER", colors.get("DefaultVulnerability"));
        cp.add(FVOther);
        filterPanels.put(FVOther.getPanel_name(), FVOther);

        FilterPanel FAttPatt = new FilterPanel(GetNodeLabel.getLabel(AttackPattern.COLLECTION), colors.get("AttackPattern"));
        cp.add(FAttPatt);
        filterPanels.put(FAttPatt.getPanel_name(), FAttPatt);

        FilterPanel FMachine = new FilterPanel(GetNodeLabel.getLabel(Machine.COLLECTION), colors.get("Machine"));
        cp.add(FMachine);
        filterPanels.put(FMachine.getPanel_name(), FMachine);

        FilterPanel FFirewall = new FilterPanel(GetNodeLabel.getLabel(Firewall.COLLECTION), colors.get("Firewall"));
        cp.add(FFirewall);
        filterPanels.put(FFirewall.getPanel_name(), FFirewall);

        FilterPanel FSubnet = new FilterPanel(GetNodeLabel.getLabel(Subnet.COLLECTION), colors.get("Subnet"));
        cp.add(FSubnet);
        filterPanels.put(FSubnet.getPanel_name(), FSubnet);

        FilterPanel FSnort = new FilterPanel(GetNodeLabel.getLabel(Snort.COLLECTION), colors.get("Snort"));
        cp.add(FSnort);
        filterPanels.put(FSnort.getPanel_name(), FSnort);

        FilterPanel FMTask = new FilterPanel(GetNodeLabel.getLabel(MissionTask.COLLECTION), colors.get("MissionTask"));
        cp.add(FMTask);
        filterPanels.put(FMTask.getPanel_name(), FMTask);

        FilterPanel FMInfo = new FilterPanel(GetNodeLabel.getLabel(MissionInformation.COLLECTION), colors.get("MissionInformation"));
        cp.add(FMInfo);
        filterPanels.put(FMInfo.getPanel_name(), FMInfo);

        FilterPanel FCyberAss = new FilterPanel(GetNodeLabel.getLabel(CyberAsset.COLLECTION), colors.get("CyberAsset"));
        cp.add(FCyberAss);
        filterPanels.put(FCyberAss.getPanel_name(), FCyberAss);

        FilterPanel FMObj = new FilterPanel(GetNodeLabel.getLabel(MissionObjective.COLLECTION), colors.get("MissionObjective"));
        cp.add(FMObj);
        filterPanels.put(FMObj.getPanel_name(), FMObj);

        FilterPanel FOther = new FilterPanel("OTHER", colors.get("Default"));
        cp.add(FOther);
        filterPanels.put(FOther.getPanel_name(), FOther);

        FilterPanel FEdge = new FilterPanel("Edge", colors.get("Edge"));
        cp.add(FEdge);
        filterPanels.put(FEdge.getPanel_name(), FEdge);

        FilterPanel FBack = new FilterPanel("Background", colors.get("Background"));
        cp.add(FBack);
        filterPanels.put(FBack.getPanel_name(), FBack);

        container.add(cp);

        container.add(new JSeparator());

        JPanel bp = new JPanel();

        JButton apply = new JButton("Apply");
        apply.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HashMap<String, Color> newColors = new HashMap<>();
                newColors.put("CPE", filterPanels.get(GetNodeLabel.getLabel(Vulnerability.COLLECTION) + " | " + CPE.TYPE.toUpperCase()).getCanvas().getColor());
                newColors.put("CVSS", filterPanels.get(GetNodeLabel.getLabel(Vulnerability.COLLECTION) + " | " + CVSS.TYPE.toUpperCase()).getCanvas().getColor());
                newColors.put("Metric", filterPanels.get(GetNodeLabel.getLabel(Vulnerability.COLLECTION) + " | " + Metric.TYPE.toUpperCase()).getCanvas().getColor());
                newColors.put("Severity", filterPanels.get(GetNodeLabel.getLabel(Vulnerability.COLLECTION) + " | " + Severity.TYPE.toUpperCase()).getCanvas().getColor());
                newColors.put("CVE", filterPanels.get(GetNodeLabel.getLabel(Vulnerability.COLLECTION) + " | " + CVE.TYPE.toUpperCase()).getCanvas().getColor());
                newColors.put("Reference", filterPanels.get(GetNodeLabel.getLabel(Vulnerability.COLLECTION) + " | " + Reference.TYPE.toUpperCase()).getCanvas().getColor());
                newColors.put("CWE", filterPanels.get(GetNodeLabel.getLabel(Vulnerability.COLLECTION) + " | " + CWE.TYPE.toUpperCase()).getCanvas().getColor());
                newColors.put("DefaultVulnerability", filterPanels.get("VULNERABILITY | OTHER").getCanvas().getColor());
                newColors.put("AttackPattern", filterPanels.get(GetNodeLabel.getLabel(AttackPattern.COLLECTION)).getCanvas().getColor());
                newColors.put("Machine", filterPanels.get(GetNodeLabel.getLabel(Machine.COLLECTION)).getCanvas().getColor());
                newColors.put("Firewall", filterPanels.get(GetNodeLabel.getLabel(Firewall.COLLECTION)).getCanvas().getColor());
                newColors.put("Subnet", filterPanels.get(GetNodeLabel.getLabel(Subnet.COLLECTION)).getCanvas().getColor());
                newColors.put("Snort", filterPanels.get(GetNodeLabel.getLabel(Snort.COLLECTION)).getCanvas().getColor());
                newColors.put("MissionTask", filterPanels.get(GetNodeLabel.getLabel(MissionTask.COLLECTION)).getCanvas().getColor());
                newColors.put("MissionInformation", filterPanels.get(GetNodeLabel.getLabel(MissionInformation.COLLECTION)).getCanvas().getColor());
                newColors.put("MissionObjective", filterPanels.get(GetNodeLabel.getLabel(MissionObjective.COLLECTION)).getCanvas().getColor());
                newColors.put("CyberAsset", filterPanels.get(GetNodeLabel.getLabel(CyberAsset.COLLECTION)).getCanvas().getColor());
                newColors.put("Default", filterPanels.get("OTHER").getCanvas().getColor());
                newColors.put("Edge", filterPanels.get("Edge").getCanvas().getColor());
                newColors.put("Background", filterPanels.get("Background").getCanvas().getColor());

                new QueryBuilder(query, "Filtering", newColors);
            }
        });
        bp.add(apply);

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        bp.add(cancel);
        bp.setLayout(new FlowLayout(FlowLayout.LEFT));

        container.add(bp);

        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setVisible(true);
        setSize(1000, 500);
    }
}
