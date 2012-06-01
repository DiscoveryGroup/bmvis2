package biomine.bmvis2.subgraph;

import biomine.bmvis2.Logging;
import biomine.bmvis2.VisualNode;
import biomine.bmvis2.ui.GraphControls;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;


public class SuggestionPanel extends JPanel {
    private JLabel searchTermLabel = new JLabel("Search term");
    private JTextField searchTerm = new JTextField();

    private JLabel suggestionListLabel = new JLabel("Suggestions");
    private DefaultListModel suggestListModel = new DefaultListModel();
    private JList suggestList = new JList(this.suggestListModel);
    private JScrollPane suggestListScrollPane = new JScrollPane(this.suggestList);
    private Map<String, VisualNode> nameToNode = new HashMap<String, VisualNode>();

    private JButton addButton = new JButton("Add suggestion");

    private GraphControls controls;

    private SuggestionDocumentListener documentListener;

    abstract class SuggestionDocumentListener implements DocumentListener {
        public abstract void update();
    }

    private void init() {
        this.add(this.searchTermLabel);
        this.add(this.searchTerm);
        this.add(this.suggestionListLabel);
        this.add(this.suggestListScrollPane);
        this.add(this.addButton);

        this.addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                for (Object o : SuggestionPanel.this.suggestList.getSelectedValues()) {
                    Logging.debug("ui", "Adding " + o.toString() + " to selected in panel.");
                    SuggestionPanel.this.controls.addInterestNode(o.toString());
                }

                // Close containing JDialog instance
                Container parent = SuggestionPanel.this.getParent();
                while (!(parent instanceof JDialog)) {
                    parent = parent.getParent();
                }
                parent.setVisible(false);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(this.searchTermLabel)
                                .addComponent(this.searchTerm)
                                .addComponent(this.suggestionListLabel)
                                .addComponent(this.suggestListScrollPane)
                                .addComponent(this.addButton))
        );
        layout.setHorizontalGroup(
                layout.createParallelGroup()
                        .addComponent(this.searchTermLabel)
                        .addComponent(this.searchTerm)
                        .addComponent(this.suggestionListLabel)
                        .addComponent(this.suggestListScrollPane)
                        .addComponent(this.addButton)
        );
    }

    private SuggestionDocumentListener getNodeSetDocumentListener(final Collection<VisualNode> nodes) {
        return new SuggestionDocumentListener() {
            public void update() {
                Logging.debug("ui", "Suggestion panel filter updated to: " + searchTerm.getText());
                SuggestionPanel.this.suggestListModel.clear();

                String filter = SuggestionPanel.this.searchTerm.getText();
                DefaultListModel model = SuggestionPanel.this.suggestListModel;

                for (VisualNode node : nodes) {
                    if (model.contains(node.toString()))
                        continue;

                    if (filter.equals("")) {
                        model.addElement(node);
                        continue;
                    }

                    for (String key : node.getBMNode().getAttributes().keySet()) {
                        if (new String(node.getBMNode().getAttributes().get(key)).toLowerCase().contains(filter.toLowerCase())) {
                            model.addElement(node);
                            break;
                        }
                    }
                }

            }

            public void insertUpdate(DocumentEvent documentEvent) {
                update();
            }

            public void removeUpdate(DocumentEvent documentEvent) {
                update();
            }

            public void changedUpdate(DocumentEvent documentEvent) {
                update();
            }
        };
    }

    public SuggestionPanel(GraphControls controls, final Collection<VisualNode> nodes) {
        this.controls = controls;
        init();

        this.documentListener = getNodeSetDocumentListener(nodes);
        this.searchTerm.getDocument().addDocumentListener(this.documentListener);
    }

    public void update() {
        this.documentListener.update();
    }
}
