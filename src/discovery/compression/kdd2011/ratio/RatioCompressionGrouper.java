package discovery.compression.kdd2011.ratio;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import biomine.bmvis2.SimpleVisualGraph;
import biomine.bmvis2.VisualGroupNode;
import biomine.bmvis2.VisualGroupNodeAutoEdges;
import biomine.bmvis2.algoutils.DefaultGraph;
import biomine.bmvis2.group.Grouper;
import discovery.compression.kdd2011.ratio.RatioCompression.ResultGraph;
import biomine.bmvis2.pipeline.SettingsChangeCallback;

public class RatioCompressionGrouper extends Grouper {
    private double ratio = 0.7;
    private boolean paths = false;
    private double oldRatio = 0.7;
    private boolean oldPaths = false;

    @Override
    public String makeGroups(VisualGroupNode n) {
        RatioCompression comp = new GreedyRatioCompression(true);
        SimpleVisualGraph sg = new SimpleVisualGraph(n);
        DefaultGraph dg = new DefaultGraph(sg);
        ResultGraph res = comp.compressGraph(dg, ratio);
        for (ArrayList<Integer> a : res.partition) {
            if (a.size() < 2) continue;
            VisualGroupNodeAutoEdges ng = new VisualGroupNodeAutoEdges(n);
            for (int i : a) {
                sg.getVisualNode(i).setParent(ng);
            }
        }
        oldRatio = ratio;
        oldPaths = paths;
        return "";
    }

    private boolean changed() {
        return oldPaths != paths || oldRatio != ratio;
    }

    @Override
    public JComponent getSettingsComponent(final SettingsChangeCallback changeCallback, VisualGroupNode groupNode) {
        JPanel ret = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        ret.setLayout(layout);

        final JSpinner ratioSpin = new JSpinner(new SpinnerNumberModel(ratio, 0.1, 1.0, 0.05));
        final JCheckBox pathsBox = new JCheckBox();
        pathsBox.setSelected(paths);

        final JLabel messageLabel = new JLabel();
        final String message = "<html>Settings changed!<br>Click to see changes.</html>";
        final JButton recompute = new JButton();
        ratioSpin.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                ratio = (Double) ratioSpin.getValue();
                if (changed()) {
                    messageLabel.setText(message);
                    recompute.setEnabled(true);
                } else {
                    messageLabel.setText("");
                    recompute.setEnabled(false);
                }
            }
        });
        pathsBox.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                paths = pathsBox.isSelected();
                if (changed()) {
                    messageLabel.setText(message);
                    recompute.setEnabled(true);
                } else {
                    messageLabel.setText("");
                    recompute.setEnabled(false);
                }
            }
        });


        recompute.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeCallback.settingsChanged(true);
            }
        });
        recompute.setText("Recompute");
        recompute.setEnabled(changed());


        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.gridwidth = 1;
        c.gridx = 0;
        ret.add(new JLabel("Ratio"), c);
        c.gridx = 1;
        ret.add(ratioSpin, c);
        c.gridy++;
        c.gridx = 0;
        ret.add(new JLabel("Paths"), c);
        c.gridx = 1;
        ret.add(pathsBox, c);
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;

        ret.add(messageLabel, c);
        c.gridy++;
        ret.add(recompute, c);
        return ret;
    }

    public String getByName() {
        return "compression";
    }

}
