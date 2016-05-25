/*
 * Copyright 2012 University of Helsinki.
 *
 * This file is part of BMVis².
 *
 * BMVis² is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * BMVis² is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with BMVis².  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package biomine.bmvis2.ui.simple;

import biomine.bmvis2.*;
import biomine.bmvis2.color.ColorPalette;
import biomine.bmvis2.color.DefaultNodeColoring;
import biomine.bmvis2.edgesimplification.KappaSimplifier;
import biomine.bmvis2.edgesimplification.SimplificationUtils;
import biomine.bmvis2.edgesimplification.XreosSimplifier;
import biomine.bmvis2.pipeline.*;
import biomine.bmvis2.pipeline.operations.structure.AllHider;
import biomine.bmvis2.pipeline.operations.structure.EdgeHiderOperation;
import biomine.bmvis2.pipeline.operations.structure.EdgeSimplificationOperation;
import biomine.bmvis2.pipeline.operations.structure.TextFilterShower;
import biomine.bmvis2.pipeline.operations.view.NodeColoringOperation;
import biomine.bmvis2.subgraph.SuggestionPanel;
import biomine.bmvis2.ui.GraphControls;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class GroupModalGraphControls extends GraphControls {
    private ToggleCommunityViewModeControl viewModeControl;

    private JLabel addMoreLabel;

    private JTextField dynamicShowerFilterField;
    private JButton addMoreButton;
    private DefaultListModel showerListModel;
    private JList showerList;
    private JScrollPane showerListScrollPane;

    private Set<TextFilterShower> nodeShowerOps = new HashSet<TextFilterShower>();
    private TextFilterShower dynamicFilterShower = new TextFilterShower("");

    private LabelChooserControl labelChooserControl;

    private JSlider edgeHiderSlider;
    private EdgeHiderOperation edgeSimplificationOperation = new EdgeSimplificationOperation(new XreosSimplifier());
    private int edgeSliderUpdateGraphNodeCount = 0;

    public class TranslucentPanel extends JPanel {
        public TranslucentPanel() {
            super();
            this.setOpaque(false);
        }

        public void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            Graphics2D g = (Graphics2D) graphics;
            Composite old = g.getComposite();

            g.setComposite(AlphaComposite.SrcOver.derive(0.3f));
            g.setColor(ColorPalette.GMAIL_BLUE);
            g.fill3DRect(0, 0, this.getSize().width, this.getSize().height, true);

            g.setComposite(old);
        }
    }

    private void ensureCorrectSimplifier() {
        EdgeSimplificationOperation operation = null;
        for (GraphOperation op : this.getPipeline().getFastOps())
            if (op instanceof EdgeSimplificationOperation)
                operation = (EdgeSimplificationOperation) op;

        int edgeCount = this.getPipeline().getCurrentGraph().getEdges().size();

        if (operation == null) {
            setSimplifier(edgeCount);
            return;
        }

        if (edgeCount < 500 && operation.simplifier instanceof KappaSimplifier)
            return;
        else if (edgeCount > 500 && operation.simplifier instanceof XreosSimplifier)
            return;

        this.getPipeline().removeFastOp(operation);
        setSimplifier(edgeCount);
    }

    private void setSimplifier(int currentEdgeCount) {
        if (currentEdgeCount < 500)
            this.edgeSimplificationOperation = new EdgeSimplificationOperation(new KappaSimplifier());
        else
            this.edgeSimplificationOperation = new EdgeSimplificationOperation(new XreosSimplifier());

        this.getPipeline().addFastOp(this.edgeSimplificationOperation);
    }

    @Override
    public void updateControls() {
        try {
            this.ensureCorrectSimplifier();
        } catch (NullPointerException npe) {
        }

        if (viewModeControl != null)
            viewModeControl.updateControl();

        if (labelChooserControl != null)
            this.labelChooserControl.updateControl();

        Set<String> showers = new HashSet<String>();
        for (GraphOperation op : this.getPipeline().getFastOps()) {
            if (op instanceof EdgeHiderOperation) {
                int edgeCount = SimplificationUtils.countNormalEdges(this.getPipeline().getCurrentGraph());
                Logging.debug("graph_operation", "Normal edge count in graph: " + edgeCount);
                EdgeHiderOperation hiderOp = (EdgeHiderOperation) op;

                if (edgeCount > this.edgeSliderUpdateGraphNodeCount) {
                    hiderOp.setTargetHiddenEdges(edgeCount);
                    this.edgeSliderUpdateGraphNodeCount = edgeCount;
                }

                this.edgeHiderSlider.setMaximum(edgeCount);
                this.edgeHiderSlider.setValue(edgeCount);
            } else if (op instanceof TextFilterShower) {
                showers.add(((TextFilterShower) op).getFilter());
            }
        }
    }


    @Override
    public void initElements(GraphArea area) {
        try {
            this.ensureCorrectSimplifier();
        } catch (NullPointerException npe) {
        }

        TranslucentPanel panel = new TranslucentPanel();
        area.addLeftReachingComponent(panel);

        // Suggestion launch button
        this.addMoreButton = new JButton("Search for choices");
        this.addMoreButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                // Set<VisualNode> nodesToSuggest = new HashSet<VisualNode>(GroupModalGraphControls.this.getPipeline().getKnownNodes());
                Set<VisualNode> nodesToSuggest = new HashSet<VisualNode>(GroupModalGraphControls.this.getPipeline().getCurrentGraph().getAllNodes());
                nodesToSuggest.remove(getPipeline().getCurrentGraph().getRootNode());
                nodesToSuggest.removeAll(getPipeline().getCurrentGraph().getNodes());
                SuggestionPanel panel = new SuggestionPanel(GroupModalGraphControls.this, nodesToSuggest);

                final JDialog dialog = new JDialog();
                dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
                dialog.setContentPane(panel);
                dialog.setSize(600, 500);
                panel.update();
                dialog.setVisible(true);
            }
        });

        this.dynamicShowerFilterField = new JTextField();

        this.dynamicShowerFilterField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent documentEvent) {
                changedUpdate(documentEvent);
            }

            public void removeUpdate(DocumentEvent documentEvent) {
                changedUpdate(documentEvent);
            }

            public void changedUpdate(DocumentEvent documentEvent) {
                Logging.debug("ui", "Dynamic filter changed to " + dynamicShowerFilterField.getText() + "!");
                dynamicFilterShower.setFilter(dynamicShowerFilterField.getText());
                getPipeline().settingsChanged(false);
            }
        });
        this.dynamicShowerFilterField.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent keyEvent) {
            }

            public void keyPressed(KeyEvent keyEvent) {
            }

            public void keyReleased(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
                    String text = dynamicShowerFilterField.getText();
                    if (text.equals(""))
                        text = "*";

                    addInterestNode(text);
                    dynamicShowerFilterField.setText("");
                }
            }
        });

        // this.dynamicShowerFilterField.setText("*");

        this.addMoreLabel = new JLabel("Add more nodes");
        this.showerListModel = new DefaultListModel();
        this.showerList = new JList(this.showerListModel);
        this.showerListScrollPane = new JScrollPane(this.showerList);

        this.showerList.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent mouseEvent) {
            }

            public void mousePressed(MouseEvent mouseEvent) {
            }

            public void mouseReleased(MouseEvent mouseEvent) {
                JPopupMenu menu = new JPopupMenu();
                final Object[] selected = GroupModalGraphControls.this.showerList.getSelectedValues();

                if (selected.length == 0)
                    return;

                menu.add(new JMenuItem(new AbstractAction("Remove selected") {
                    public void actionPerformed(ActionEvent e) {
                        for (Object o : selected)
                            GroupModalGraphControls.this.removeInterestNode(o.toString());
                    }
                }));

                menu.show(GroupModalGraphControls.this.showerList, mouseEvent.getX(), mouseEvent.getY());
            }

            public void mouseEntered(MouseEvent mouseEvent) {
            }

            public void mouseExited(MouseEvent mouseEvent) {
            }
        });

        this.labelChooserControl = new LabelChooserControl(getPipeline());

        JLabel edgeHiderLabel = new JLabel("Hide relations");
        this.edgeHiderSlider = new JSlider(0,
                this.getPipeline().getCurrentGraph().getAllEdges().size(),
                this.getPipeline().getCurrentGraph().getAllEdges().size());
        this.edgeHiderSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                GroupModalGraphControls.this.edgeSimplificationOperation.setTargetHiddenEdges(GroupModalGraphControls.this.edgeHiderSlider.getValue());
                /* try {
                    GroupModalGraphControls.this.edgeSimplificationOperation.doOperation(GroupModalGraphControls.this.getPipeline().getCurrentGraph());
                } catch (Exception e) {
                } */
                GroupModalGraphControls.this.getPipeline().settingsChanged(false);
            }
        });

        // this.getPipeline().addStructuralOp(new DijkstraExtractor());
        this.getPipeline().addFastOp(new NodeColoringOperation(new DefaultNodeColoring()));
        this.getPipeline().addFastOp(this.labelChooserControl.nodeLabelOperation);
        this.getPipeline().addFastOp(this.labelChooserControl.edgeLabelOperation);

        this.getPipeline().addFastOp(new AllHider());
        // this.getPipeline().addFastOp(this.edgeSimplificationOperation);
        this.getPipeline().addFastOp(this.dynamicFilterShower);
        this.getPipeline().addFastOp(this.edgeSimplificationOperation);

        // View mode
        JLabel viewModeLabel = new JLabel("Color communities");
        this.viewModeControl = new ToggleCommunityViewModeControl(this.getPipeline());

        JLabel toggleAttributeLabel = new JLabel("Choose visible facts");

        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(edgeHiderLabel)
                                .addComponent(this.edgeHiderSlider))
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(this.addMoreLabel)
                                .addComponent(this.addMoreButton)
                                .addComponent(this.dynamicShowerFilterField)
                                .addComponent(this.showerListScrollPane))
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(toggleAttributeLabel)
                                .addComponent(this.labelChooserControl))
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(viewModeLabel)
                                .addComponent(this.viewModeControl))
        );
        layout.setHorizontalGroup(
                layout.createParallelGroup()
                        .addComponent(this.addMoreLabel)
                        .addComponent(this.addMoreButton)
                        .addComponent(this.dynamicShowerFilterField)
                        .addComponent(this.showerListScrollPane)
                        .addComponent(this.labelChooserControl)
                        .addComponent(this.viewModeControl)
                        .addComponent(this.edgeHiderSlider)
                        .addComponent(edgeHiderLabel)
                        .addComponent(toggleAttributeLabel)
                        .addComponent(viewModeLabel)
        );
    }

    public void addInterestNode(String s) {
        Logging.debug("ui", "Attempting to add interest node by name or id " + s);
        showerListModel.addElement(s);

        boolean exists = false;
        for (TextFilterShower tfs : nodeShowerOps) {
            if (exists)
                break;
            if (tfs.getFilter().equals(s))
                exists = true;
        }

        if (exists)
            return;

        TextFilterShower n = new TextFilterShower(s);
        nodeShowerOps.add(n);

        getPipeline().addFastOp(n);

        getPipeline().settingsChanged(false);

        delayedZoomToVisible();
    }

    public void removeInterestNode(String s) {
        this.showerListModel.removeElement(s);
        Set<TextFilterShower> removables = new HashSet<TextFilterShower>();

        for (TextFilterShower op : nodeShowerOps)
            if (op.getFilter().equals(s))
                removables.add(op);

        nodeShowerOps.removeAll(removables);
        for (TextFilterShower op : removables)
            getPipeline().removeFastOp(op);

        delayedZoomToVisible();
    }

    public void setInterestNodes(Set<String> names) {
        this.showerListModel.clear();
        for (TextFilterShower op : nodeShowerOps)
            getPipeline().removeFastOp(op);

        for (String name : names) {
            this.showerListModel.addElement(name);
            TextFilterShower op = new TextFilterShower(name);
            this.nodeShowerOps.add(op);
            getPipeline().addFastOp(op);
        }

        delayedZoomToVisible();
    }

    public Set<String> getInterestNodes() {
        Set<String> ret = new HashSet<String>();

        for (TextFilterShower op : nodeShowerOps)
            ret.add(op.getFilter());

        return ret;
    }

    private void delayedZoomToVisible() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                }
                getPipeline().getVisualizer().getGraphArea().zoomTo(new LinkedList<LayoutItem>(getPipeline().getCurrentGraph().getNodes()));
            }
        });
    }
}
