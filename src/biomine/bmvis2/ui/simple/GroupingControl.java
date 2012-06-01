package biomine.bmvis2.ui.simple;

import biomine.bmvis2.Logging;
import biomine.bmvis2.group.Grouper;
import biomine.bmvis2.group.GrouperList;
import biomine.bmvis2.pipeline.GraphOperation;
import biomine.bmvis2.pipeline.GrouperOperation;
import biomine.bmvis2.pipeline.Pipeline;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

public class GroupingControl extends TranslucentControl {
    private GroupingComboBox groupingComboBox;
    private Map<String, Grouper> visibleNameToGrouper = new HashMap<String, Grouper>();
    private JComponent groupingProperties = null;


    public GroupingControl(Pipeline pipeline) {
        super(pipeline);

        for (Grouper g : GrouperList.getGroupers())
            if (g.getByName() != null) {
                Logging.info("ui", "Adding grouper " + g);
                List<Grouper> groupers = new ArrayList<Grouper>();
                groupers.add(g);
                visibleNameToGrouper.put("by " + g.getByName(), g);
            }

        this.groupingComboBox = new GroupingComboBox();
        JLabel groupingLabel = new JLabel("Grouping");

        // EmptyBorder(int top, int left, int bottom, int right)
        //  .putClientProperty("JComponent.sizeVariant", "mini / small /large");
        groupingLabel.setBorder(new EmptyBorder(5, 5, 0, 5));
        this.groupingComboBox.setBorder(new EmptyBorder(0, 5, 5, 5));

        SpringLayout layout = new SpringLayout();
        this.setLayout(layout);
        layout.putConstraint(SpringLayout.NORTH, this.groupingComboBox, 0, SpringLayout.SOUTH, groupingLabel);

        this.add(groupingLabel);
        this.add(this.groupingComboBox);
    }

    public class GroupingComboBox extends JComboBox implements ActionListener {
        public GroupingComboBox() {
            super();

            this.addItem("No grouping");
            for (String name : GroupingControl.this.visibleNameToGrouper.keySet()) {
                this.addItem(name);
            }

            this.setEnabled(true);
            this.addActionListener(this);
        }

        private GrouperOperation getCurrentGrouperOperation() {
            Collection<GraphOperation> groupers = new ArrayList<GraphOperation>();
            for (GraphOperation op : GroupingControl.this.getPipeline().getCurrentStructuralOps()) {
                if (op instanceof GrouperOperation) {
                    groupers.add(op);
                }
            }

            if (groupers.size() == 1)
                return (GrouperOperation) groupers.iterator().next();
            return null;
        }

        private Grouper getSelectedGrouper() {
            try {
                return visibleNameToGrouper.get((String) this.getItemAt(this.getSelectedIndex()));
            } catch (NullPointerException npe) {
            }
            return null;
        }


        @Override
        public void actionPerformed(ActionEvent e) {
            Logging.info("ui", "Grouper changed!");

            if (this.getSelectedGrouper() == null && this.getCurrentGrouperOperation() != null) {
                GroupingControl.this.getPipeline().removeStructuralOp(this.getCurrentGrouperOperation());
            }

            Grouper selected = this.getSelectedGrouper();
            GrouperOperation op = this.getCurrentGrouperOperation();

            Grouper activeGrouper;
            if (op == null)
                activeGrouper = null;
            else
                activeGrouper = op.getGrouper();

            boolean update = false;
            if (activeGrouper == null)
                update = true;
            else if (selected == null)
                update = true;
            else if (activeGrouper.getByName().equals(selected.getByName())) {
            } else
                update = true;

            final boolean reallyUpdate = update;
            final Grouper newGrouper = selected;

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (reallyUpdate) {
                        GroupingControl.this.getPipeline().removeStructuralOp(GroupingComboBox.this.getCurrentGrouperOperation());
                        if (newGrouper != null)
                            GroupingControl.this.getPipeline().addStructuralOp(new GrouperOperation(newGrouper));
                        GroupingControl.this.getPipeline().settingsChanged(true);
                    }
                }
            });
        }
    }

    public void updateControl() {
        GrouperOperation currentOp = this.groupingComboBox.getCurrentGrouperOperation();
        Logging.info("ui", "currentGrouperOperation: " + currentOp);

        if (currentOp == null && this.groupingProperties == null) {
            // NOP
        } else if (currentOp == null && this.groupingProperties != null) {
            this.remove(this.groupingProperties);
            this.groupingProperties = null;
            this.revalidate();
        } else if (currentOp != null) {
            if (this.groupingProperties != null)
                this.remove(this.groupingProperties);

            this.groupingProperties = currentOp.getGrouper().getSettingsComponent(this.getPipeline(), this.getPipeline().getCurrentGraph().getRootNode());
            this.groupingProperties.setOpaque(false);
            // EmptyBorder(int top, int left, int bottom, int right)
            this.groupingProperties.setBorder(new EmptyBorder(0, 15, 5, 5));

            SpringLayout layout = (SpringLayout) this.getLayout();
            layout.putConstraint(SpringLayout.NORTH, this.groupingProperties, 0, SpringLayout.SOUTH, this.groupingComboBox);

            this.add(this.groupingProperties);
            this.revalidate();
            Logging.info("ui", "groupingProperties coords: " + this.groupingProperties.getX() + "," + this.groupingProperties.getY());
        }

        Logging.info("ui", "GroupingControl.getComponentCount(): " + this.getComponentCount());
    }
}
