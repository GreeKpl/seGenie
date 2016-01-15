import com.sun.org.apache.xpath.internal.SourceTree;
import smile.Network;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by alek on 14.01.16.
 */
public class MainFrame extends JFrame {

    public static void main(String[] args) {
        MainFrame mainFrame = new MainFrame();

    }

    private final List<String> symptoms = new ArrayList<>();
    private final Map<String, JCheckBox> checkboxes = new HashMap<>();

    public MainFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        Network n = new Network();
        n.readFile("choroby.xdsl");

        String[] nodeIds = n.getAllNodeIds();
        for (String nodeId : nodeIds) {
            System.out.println(n.getNode(nodeId) + " " + n.getNodeName(nodeId));

            if (n.getChildIds(nodeId).length > 0) {
                symptoms.add(nodeId);
                System.out.println(Arrays.toString(n.getChildIds(nodeId)));
            }



        }

        getContentPane().setLayout(new BorderLayout());

        JPanel choices = new JPanel();
        choices.setLayout(new BoxLayout(choices, BoxLayout.Y_AXIS));

        for (String symptom : symptoms) {
            if (!symptom.equals("temperatura")) {
                JCheckBox jCheckBox = new JCheckBox(symptom);

                choices.add(jCheckBox);
                checkboxes.put(symptom, jCheckBox);
            }
        }

        JPanel temperatura = new JPanel();
        JRadioButton brakTemperatury = new JRadioButton("<37.0");
        JRadioButton niskaTemperatura = new JRadioButton("37.0<x<39.0");
        JRadioButton wysokaTemperatura = new JRadioButton(">39.0");
        brakTemperatury.setSelected(true);
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(brakTemperatury);
        buttonGroup.add(niskaTemperatura);
        buttonGroup.add(wysokaTemperatura);

        temperatura.add(brakTemperatury);
        temperatura.add(niskaTemperatura);
        temperatura.add(wysokaTemperatura);

        choices.add(temperatura);

        JPanel results = new JPanel();
        results.setLayout(new BoxLayout(results, BoxLayout.Y_AXIS));

        JButton okButton = new JButton("OK");

        okButton.addActionListener(e -> {

            for (String symptom : checkboxes.keySet()) { // set everything but temperature
                n.setEvidence(symptom, checkboxes.get(symptom).isSelected() ? 0 : 1);
            }

            if (brakTemperatury.isSelected()) {
                n.setEvidence("temperatura", 0);
            } else if (niskaTemperatura.isSelected()) {
                n.setEvidence("temperatura", 1);
            } else if (wysokaTemperatura.isSelected()) {
                n.setEvidence("temperatura", 2);
            }

            n.updateBeliefs();

            results.removeAll();
            TreeMap<Integer, String> orderedIllnesses = new TreeMap<>();
            for (String nodeId : n.getAllNodeIds()) {
                if (!symptoms.contains(nodeId)) {
                    if (n.isValueValid(nodeId)) {
                        double[] allProbabilities = n.getNodeValue(nodeId);
                        double yesChance = allProbabilities[0]; // yes
                        orderedIllnesses.put((int)Math.round(yesChance * 100), n.getNodeName(nodeId));
                    } else {
                        System.out.println(nodeId + " is invalid");
                    }
                }
            }
            for (Map.Entry<Integer, String> a : orderedIllnesses.descendingMap().entrySet()) {
                results.add(new Label(a.getValue() + a.getKey() + "%"));
            }


            getContentPane().revalidate();
            getContentPane().repaint();


        });

        getContentPane().add(choices, BorderLayout.CENTER);
        getContentPane().add(results, BorderLayout.EAST);
        getContentPane().add(okButton, BorderLayout.SOUTH);

        setSize(500 ,400);
        setVisible(true);
    }
}
