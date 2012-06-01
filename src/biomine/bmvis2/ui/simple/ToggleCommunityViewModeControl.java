package biomine.bmvis2.ui.simple;

import biomine.bmvis2.Logging;
import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.color.DefaultNodeColoring;
import biomine.bmvis2.color.GroupColoring;
import biomine.bmvis2.group.GirvanNewmanClustering;
import biomine.bmvis2.group.Grouper;
import biomine.bmvis2.pipeline.GrouperOperation;
import biomine.bmvis2.pipeline.NodeColoringOperation;
import biomine.bmvis2.pipeline.Pipeline;
import biomine.bmvis2.pipeline.StructuralOperation;
import discovery.compression.kdd2011.ratio.RatioCompressionGrouper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

public class ToggleCommunityViewModeControl extends TranslucentControl {
    private SpringLayout layout;
    private JCheckBox communityEnabledCheckBox;
    private JComponent settingsComponent;

    private GirvanNewmanClustering grouper = new GirvanNewmanClustering();

    public boolean communityEnabled = false;

    public ToggleCommunityViewModeControl(Pipeline pipeline) {
        super(pipeline);

        layout = new SpringLayout();
        this.setLayout(layout);

        this.communityEnabledCheckBox = new ToggleCommunityCheckBox();
        // top, left, bottom, right
        // this.modeComboBox.setBorder(new EmptyBorder(5, 5, 5, 5));
        this.add(this.communityEnabledCheckBox);
    }

    private void modeChangeCallback(boolean communityEnabled) {
        if (this.communityEnabled == communityEnabled)
            return;

        Logging.info("ui", "Community view: " + communityEnabled);

        if (communityEnabled) {
            NodeColoringOperation coloringOp = new NodeColoringOperation(new GroupColoring());

            this.getPipeline().removeColoringOperations();
            this.getPipeline().addFastOp(coloringOp);

            boolean grouperSet = false;
            List<StructuralOperation> removableOps = new LinkedList<StructuralOperation>();
            for (StructuralOperation op : this.getPipeline().getCurrentStructuralOps()) {
                if (op instanceof GrouperOperation) {
                    GrouperOperation grouperOp = (GrouperOperation) op;
                    Grouper grouper = grouperOp.getGrouper();
                    if (grouper instanceof GirvanNewmanClustering) {
                        grouperSet = true;
                    } else {
                        removableOps.add(op);
                    }
                }
            }
            for (StructuralOperation op : removableOps)
                this.getPipeline().removeStructuralOp(op);

            this.getPipeline().getCurrentGraph().setHighlightMode(VisualGraph.HighlightMode.GROUP);

            if (!grouperSet) {
                this.getPipeline().addStructuralOp(new GrouperOperation(this.grouper));
                this.getPipeline().settingsChanged(true);
            } else
                this.getPipeline().settingsChanged(false);

            this.getPipeline().getCurrentGraph().setHighlightMode(VisualGraph.HighlightMode.GROUP);
        } else {
            NodeColoringOperation coloringOp = new NodeColoringOperation(new DefaultNodeColoring());

            this.getPipeline().removeColoringOperations();
            this.getPipeline().addFastOp(coloringOp);
            this.getPipeline().settingsChanged(true);
            this.getPipeline().getCurrentGraph().setHighlightMode(VisualGraph.HighlightMode.NEIGHBORS);
        }

        if (this.settingsComponent != null)
            this.remove(this.settingsComponent);
        this.settingsComponent = null;

        if (communityEnabled) {
            this.settingsComponent = this.getPipeline().getCurrentStructuralOps().iterator().next().getSettingsComponent(this.getPipeline(), this.getPipeline().getCurrentGraph());
            this.settingsComponent.setBorder(new EmptyBorder(5, 5, 5, 5));
            this.settingsComponent.setBounds(new Rectangle(200, 200));
            this.settingsComponent.setOpaque(false);

            this.layout.putConstraint(SpringLayout.NORTH, this.settingsComponent, 0, SpringLayout.SOUTH, this.communityEnabledCheckBox);
            this.add(this.settingsComponent);
        }
        this.revalidate();

        this.communityEnabled = communityEnabled;
    }

    public class ToggleCommunityCheckBox extends JCheckBox implements ActionListener {
        public ToggleCommunityCheckBox() {
            super();

            this.setEnabled(true);
            this.addActionListener(this);
        }

        public void actionPerformed(ActionEvent e) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ToggleCommunityViewModeControl.this.modeChangeCallback(ToggleCommunityCheckBox.this.isSelected());
                }
            });

        }
    }
}