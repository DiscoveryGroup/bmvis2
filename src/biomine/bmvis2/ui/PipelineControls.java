package biomine.bmvis2.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import biomine.bmvis2.GraphArea;
import biomine.bmvis2.group.Grouper;
import biomine.bmvis2.group.GrouperList;
import biomine.bmvis2.pipeline.*;
import biomine.bmvis2.pipeline.sources.FileGraphSource;
import biomine.bmvis2.pipeline.sources.GraphSource;
import biomine.bmvis2.pipeline.sources.QueryGraphSource;
import biomine.bmvis2.utils.FileFilters;

/**
 * Component for controls.
 * <p/>
 * Settings in this component
 * <p/>
 * 1. Queries
 * <p/>
 * 2. Groupings
 * <p/>
 * 3. Visibility
 * <p/>
 * 4. Colors
 * <p/>
 * 5....
 */

public class PipelineControls extends GraphControls {
    private Pipeline pipeline;

    SummaryList dataList;
    SummaryList grouperList;
    SummaryList hiderList;
    NodeListView poiList;

    JButton newQueryButton;
    JButton newFileButton;
    JButton newGrouperButton;
    JButton newFastOpButton;

    // For detecting repeated groupers
    private Map<StructuralOperation, String> realGrouperTitles = new HashMap<StructuralOperation, String>();

    Action newQueryAction = new AbstractAction("New query") {
        public void actionPerformed(ActionEvent arg0) {
            String db = null;
            if (pipeline.getSourceDatabases().size() != 0)
                db = pipeline.getSourceDatabases().iterator().next();
            GraphSource src = QueryGraphSource.createFromDialog(Collections.EMPTY_SET, db);//,pipeline.getSourceDatabases());
            if (src != null)
                pipeline.addSource(src);
        }
    };

    Action newFileAction = new AbstractAction("New file") {
        public void actionPerformed(ActionEvent arg0) {
            JFileChooser choose = Menus.getInstance(pipeline).createFileChooser();
            choose.setFileFilter(FileFilters.FILTER_GRAPH);
            int ok = choose.showOpenDialog(PipelineControls.this);
            File f = choose.getSelectedFile();
            if (f != null) {
                GraphSource src = new FileGraphSource(f.getAbsolutePath());
                if (src != null)
                    pipeline.addSource(src);

            }
        }
    };

    Action newGrouperAction = new AbstractAction("New group") {
        public void actionPerformed(ActionEvent arg0) {
            Collection<Grouper> groupingAlgos = GrouperList.getGroupers();
            JMenuItem menuitem;
            JPopupMenu groupMenu = new JPopupMenu();

            for (final Grouper g : groupingAlgos) {
                // Grouping algorithms

                menuitem = new JMenuItem(new AbstractAction(
                        GrouperList.grouperName(g)) {
                    public void actionPerformed(ActionEvent e) {
                        Grouper rc = g;

                        PipelineControls.this.pipeline.addStructuralOp(new GrouperOperation(g));
                    }
                });
                groupMenu.add(menuitem);
            }
            groupMenu.setLocation(newGrouperButton.getLocation());
            groupMenu.setVisible(true);
            groupMenu.show(newGrouperButton, newGrouperButton.getX(),
                    newGrouperButton.getY());

        }
    };


    public void updateControls() {
        dataList.clearItems();
        for (final GraphSource op : pipeline.getCurrentSources()) {
            JComponent set = pipeline.getComponent(op);
            this.dataList.addItem(op.getTitle(), set, new AbstractAction(
                    removeText) {
                public void actionPerformed(ActionEvent arg0) {
                    pipeline.removeSource(op);
                }
            });
        }

        // update grouperList.
        Collection<String> grouperTitles = grouperList.itemTitles();
        for (final StructuralOperation op : pipeline.getCurrentStructuralOps()) {
            //Grouper g = op.getGrouper();
            String title = op.getTitle();
            if (realGrouperTitles.containsKey(op)) {
                title = realGrouperTitles.get(op);
            } else {
                while (realGrouperTitles.containsValue(title)) {
                    title = title + "*";
                }
                realGrouperTitles.put(op, title);
            }
            JComponent grouperSettings = pipeline.getComponent(op);

            if (grouperTitles.contains(title)) {
                CollapsePane cp = (CollapsePane) grouperList.getPane(title);
                if (cp != null)
                    cp.setComp(grouperSettings);
                grouperTitles.remove(title);
                if (op.getToolTip() != null)
                    cp.setToolTipText(op.getToolTip());
            } else {
                this.grouperList.addItem(title, grouperSettings,
                        new AbstractAction(removeText) {
                            public void actionPerformed(ActionEvent arg0) {
                                pipeline.removeStructuralOp(op);
                            }
                        });
            }
            //grouperTitles.add(op.getTitle());
        }

        for (String tit : grouperTitles)
            grouperList.removeItem(tit);

        Collection<String> hiderTitles = hiderList.itemTitles();

        for (String str : hiderList.itemTitles()) {
            boolean found = false;
            for (GraphOperation op : pipeline.getFastOps())
                if (op.getTitle().equals(str))
                    found = true;
            if (!found)
                hiderList.removeItem(str);
        }

        for (final GraphOperation op : pipeline.getFastOps()) {
            String title = op.getTitle();
            JComponent sets = pipeline.getComponent(op);

            if (hiderTitles.contains(title)) {
                CollapsePane cp = (CollapsePane) hiderList.getPane(title);
                if (cp == null)
                    continue;
                cp.setComp(sets);
            } else {
                hiderList.addItem(op.getTitle(), sets, new AbstractAction(
                        removeText) {
                    public void actionPerformed(ActionEvent e) {
                        pipeline.removeFastOp(op);
                    }
                });
            }
        }
        updatePOIList();
        dataList.revalidate();
        grouperList.revalidate();
        hiderList.revalidate();
        poiList.revalidate();
    }

    private void updatePOIList() {
        poiList.setNodes(pipeline.getCurrentGraph().getNodesOfInterest().keySet());
    }

    private JButton nodeHiderButton = new JButton(new AbstractAction() {
        public void actionPerformed(ActionEvent arg0) {
        }
    });


    public PipelineControls() {
        dataList = new SummaryList();
        grouperList = new SummaryList();
        hiderList = new SummaryList();

        poiList = new NodeListView(pipeline);

        newQueryButton = new JButton(newQueryAction);
        newFileButton = new JButton(newFileAction);
        newGrouperButton = new JButton(newGrouperAction);
        Action newFastOpAction = new AbstractAction("New view operation") {
            public void actionPerformed(ActionEvent arg0) {
                JPopupMenu pm = new JPopupMenu();
                for (final GraphOperation op : GraphOperationList
                        .getAvailableOperations()) {
                    pm.add(new AbstractAction(op.getTitle()) {
                        public void actionPerformed(ActionEvent e) {
                            pipeline.addFastOp(op);
                        }
                    });
                }
                pm.show(PipelineControls.this, newFastOpButton.getX(),
                        newFastOpButton.getY());
            }
        };
        newFastOpButton = new JButton(newFastOpAction);

        newGrouperButton.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
        newFastOpButton.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
        //Box queryBox = new Box(BoxLayout.Y_AXIS);
        JPanel queryBox = new JPanel();
        GridBagConstraints c = new GridBagConstraints();
        {
            GridBagLayout queryBoxLayout = new GridBagLayout();
            queryBox.setLayout(queryBoxLayout);

            c.fill = GridBagConstraints.HORIZONTAL;//HORIZONTAL;
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1;
            c.weighty = 0;
            c.anchor = GridBagConstraints.NORTH;
            c.gridwidth = 1;
            c.gridx = 0;
            c.gridwidth = 2;
            c.weighty = 1;
            queryBox.add(dataList, c);
            c.gridy++;
            c.fill = GridBagConstraints.NONE;
            c.anchor = GridBagConstraints.EAST;
            c.insets = new Insets(0, 10, 0, 10);
            queryBox.add(newQueryButton, c);
            c.gridy++;
            queryBox.add(newFileButton, c);
        }

        final SummaryList mainList = new SummaryList();
        mainList.addItem("Input", queryBox);

        //Box grouperBox = new Box(BoxLayout.Y_AXIS);
        JPanel grouperBox = new JPanel();
        {
            grouperBox.setLayout(new GridBagLayout());
            c.gridx = 0;
            c.gridy = 0;
            c.insets = new Insets(0, 0, 0, 0);
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.NORTH;

            grouperBox.add(grouperList, c);
            c.fill = GridBagConstraints.NONE;
            c.anchor = GridBagConstraints.EAST;
            c.gridy++;
            c.insets = new Insets(0, 10, 0, 10);
            grouperBox.add(newGrouperButton, c);
        }

        mainList.addItem("Structure", grouperBox);

        JPanel hiderBox = new JPanel();
        {
            hiderBox.setLayout(new GridBagLayout());
            c.gridx = 0;
            c.gridy = 0;
            c.insets = new Insets(0, 0, 0, 0);
            c.fill = c.HORIZONTAL;
            c.anchor = c.NORTH;

            hiderBox.add(hiderList, c);
            c.fill = c.NONE;
            c.anchor = c.EAST;
            c.gridy++;
            c.insets = new Insets(0, 10, 0, 10);
            hiderBox.add(newFastOpButton, c);
        }
        mainList.addItem("View", hiderBox);

        Box poiBox = new Box(BoxLayout.Y_AXIS);
        poiBox.add(poiList);
        mainList.addItem("Points of interest", poiBox);

        // This doesn't seem to work since at this point the pipeline and the visualizer aren't initialized.
        /* SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (pipeline != null && pipeline.getVisualizer() != null &&
                        pipeline.getVisualizer().getGraphAreaSettings() != null)
                    mainList.addItem("View settings", pipeline.getVisualizer().getGraphAreaSettings());
            }
        });*/

        this.setLayout(new GridLayout());
        JPanel b = new VerticalScrollablePanel();

        b.setLayout(new GridBagLayout());
        c = new GridBagConstraints();
        c.fill = c.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.anchor = c.NORTH;

        c.gridwidth = 1;

        c.gridx = 0;
        c.gridwidth = 2;

        c.weighty = 1;

        b.add(mainList, c);
        b.setBackground(Color.WHITE);

        JScrollPane scroller = new JScrollPane(b);

        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        this.add(scroller);

        this.setPreferredSize(new Dimension(150, Short.MAX_VALUE));
        this.setMinimumSize(new Dimension(150, 0));
    }

    public void setPipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
        this.poiList.setPipeline(pipeline);
    }

    /**
     * String shown in remove buttons of objects
     */
    private static final String removeText = "×";

    @Override
    public void initElements(GraphArea ga) {
    }
}