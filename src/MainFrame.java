import smile.Network;
import smile.UserProperty;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;


public class MainFrame extends JFrame {

    private final Network network;

    public static void main(String[] args) {
        MainFrame mainFrame = new MainFrame();

    }

    private final List<String> symptoms = new ArrayList<>();
    private final List<String> illnesses = new ArrayList<>();
    private final Map<String, JCheckBox> checkboxes = new HashMap<>();

    public MainFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        network = new Network();
        network.readFile("choroby.xdsl");

        String[] nodeIds = network.getAllNodeIds();
        for (String nodeId : nodeIds) {
            System.out.println(network.getNode(nodeId) + " " + network.getNodeName(nodeId));

            if (isInstanceOf(nodeId, "objaw")) {
                symptoms.add(nodeId);
            } else if (isInstanceOf(nodeId, "choroba")) {
                illnesses.add(nodeId);
            }
        }

        getContentPane().setLayout(new GridLayout(2, 1));

        JPanel choices = new JPanel();
        choices.setLayout(new BoxLayout(choices, BoxLayout.Y_AXIS));

        for (String symptom : symptoms) {
            JCheckBox checkBox = new JCheckBox(network.getNodeName(symptom));

            choices.add(checkBox);
            checkboxes.put(symptom, checkBox);
        }

        JPanel temperatura = new JPanel();
        JRadioButton brakTemperatury = new JRadioButton("< 37.5");
        JRadioButton niskaTemperatura = new JRadioButton("37.5 < x < 39.5");
        JRadioButton wysokaTemperatura = new JRadioButton("> 39.5");

        createTemperaturePanel(temperatura, brakTemperatury, niskaTemperatura, wysokaTemperatura);

        choices.add(temperatura);

        JPanel results = new JPanel();
        results.setLayout(new BoxLayout(results, BoxLayout.Y_AXIS));

        JButton okButton = new JButton("OK");

        okButton.addActionListener(e -> {
            onButtonClick(network, brakTemperatury, niskaTemperatura, wysokaTemperatura, results);
        });

        JPanel okButtonPanel = new JPanel();
        okButtonPanel.setLayout(new BorderLayout());
        okButtonPanel.add(okButton, BorderLayout.SOUTH);

        getContentPane().add(choices);
        getContentPane().add(results);
        getContentPane().add(okButtonPanel);

        setSize(800, 500);
        setVisible(true);
    }

    private boolean isInstanceOf(String nodeId, String typeName) {
        for (UserProperty prop : network.getNodeUserProperties(nodeId)) {
            if (prop.name.equals("type") && prop.value.equals(typeName)) {
                return true;
            }
        }
        return false;
    }

    private void createTemperaturePanel(JPanel temperatura, JRadioButton brakTemperatury,
                                        JRadioButton niskaTemperatura, JRadioButton wysokaTemperatura) {
        brakTemperatury.setSelected(true);
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(brakTemperatury);
        buttonGroup.add(niskaTemperatura);
        buttonGroup.add(wysokaTemperatura);

        temperatura.add(brakTemperatury);
        temperatura.add(niskaTemperatura);
        temperatura.add(wysokaTemperatura);
    }

    private void onButtonClick(Network network, JRadioButton brakTemperatury, JRadioButton niskaTemperatura,
                               JRadioButton wysokaTemperatura, JPanel results) {
        for (String symptom : checkboxes.keySet()) { // set everything but temperature
            network.setEvidence(symptom, checkboxes.get(symptom).isSelected() ? 0 : 1);
        }

        if (brakTemperatury.isSelected()) {
            network.setEvidence("temperatura", 0);
        } else if (niskaTemperatura.isSelected()) {
            network.setEvidence("temperatura", 1);
        } else if (wysokaTemperatura.isSelected()) {
            network.setEvidence("temperatura", 2);
        }

        network.updateBeliefs();

        results.removeAll();
        TreeMap<Integer, String> orderedIllnesses = new TreeMap<>();
        for (String nodeId : network.getAllNodeIds()) {
            if (illnesses.contains(nodeId)) {
                if (network.isValueValid(nodeId)) {
                    double[] allProbabilities = network.getNodeValue(nodeId);
                    double yesChance = allProbabilities[0]; // yes
                    orderedIllnesses.put((int) Math.round(yesChance * 100), network.getNodeName(nodeId));
                } else {
                    System.out.println(nodeId + " is invalid");
                }
            }
        }

        // pokaz prawdopodobienstwo poszczegolnych chorob
        for (Map.Entry<Integer, String> a : orderedIllnesses.descendingMap().entrySet()) {
            results.add(new Label(a.getValue() + " - " + a.getKey() + "%"));
        }

        getContentPane().revalidate();
        getContentPane().repaint();
    }
}
