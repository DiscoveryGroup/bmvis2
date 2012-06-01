package biomine.bmvis2.ui;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.*;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import biomine.bmvis2.*;
import biomine.bmvis2.color.ColorPalette;
import biomine.bmvis2.pipeline.Pipeline;

public class NodeBrowser extends JPanel implements GraphObserver,
        TreeSelectionListener, MouseListener, MouseMotionListener {
    private Pipeline pipeline = null;

    private VisualGraph visualGraph;
    private JScrollPane scrollPane;
    private JTree tree;
    private HashMap<VisualNode, DefaultMutableTreeNode> nodeToTreeNode;
    private JTextField filter = new JTextField();
    private boolean updatingSelection = false;
    private JCheckBox showHiddenCheckBox = new JCheckBox("List hidden nodes");

    WeakHashMap<JComponent, Void> renderers = new WeakHashMap<JComponent, Void>();

    public NodeBrowser(Pipeline pipeline) {
        this.pipeline = pipeline;
        this.pipeline.getCurrentGraph().addObserver(this);
        this.visualGraph = this.pipeline.getCurrentGraph();

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        Box b = new Box(BoxLayout.X_AXIS);
        this.showHiddenCheckBox.setSelected(true);

        b.add(filter);
        b.add(showHiddenCheckBox);

        this.add(b);

        filter.setMaximumSize(new Dimension(Short.MAX_VALUE, filter
                .getPreferredSize().height));
        filter.addCaretListener(new CaretListener() {
            public void caretUpdate(CaretEvent arg0) {
                // System.out.println("getText = " + filter.getText());
                buildTree();
            }
        });
        showHiddenCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent arg0) {
                buildTree();
            }
        });
        this.buildTree();
    }

    public void setShowHidden(boolean newState) {
        this.showHiddenCheckBox.setSelected(newState);
    }

    DefaultMutableTreeNode treeFromNode(VisualNode n) {
        DefaultMutableTreeNode ret = new DefaultMutableTreeNode(n.getName());
        ret.setUserObject(n);
        if (showHiddenCheckBox.isSelected() == false) {
            if (visualGraph.getHiddenNodes().contains(n))
                return null;
        }
        if (n.getClass().equals(VisualNode.class)) {
            String f = filter.getText();
            if (n.getName().toLowerCase().contains(f.toLowerCase()) == false) {
                return null;
            }
        }
        nodeToTreeNode.put(n, ret);

        if (n instanceof VisualGroupNode) {
            int count = 0;
            VisualGroupNode vgn = (VisualGroupNode) n;
            if (vgn.getChildren() == null)
                return ret;
            ArrayList<VisualNode> namesort = new ArrayList<VisualNode>();

            for (VisualNode child : vgn.getChildren()) {
                namesort.add(child);
            }
            Collections.sort(namesort, new Comparator<VisualNode>() {
                public int compare(VisualNode o1, VisualNode o2) {
                    if (o1 instanceof VisualGroupNode)
                        return -1;
                    if (o2 instanceof VisualGroupNode)
                        return 1;
                    return o1.getName().compareTo(o2.getName());
                }
            });
            for (VisualNode child : namesort) {
                DefaultMutableTreeNode cr = treeFromNode(child);
                if (cr != null) {
                    ret.add(cr);
                    count++;
                }
            }
            if (count == 0)
                return null;
        }

        return ret;
    }

    private boolean isPathOpen(VisualGroupNode n) {
        if (n.isOpen() == false)
            return false;
        if (n.getParent() == null)
            return true;
        return isPathOpen(n.getParent());
    }

    private void gatherNodes(VisualNode n, HashSet<VisualNode> v) {
        v.add(n);
        if (n instanceof VisualGroupNode) {
            VisualGroupNode vgn = (VisualGroupNode) n;
            if (vgn.isOpen())
                for (VisualNode c : vgn.getChildren()) {
                    gatherNodes(c, v);
                }
        }
    }

    public HashSet<VisualNode> getVisibleNodes() {
        HashSet<VisualNode> ret = new HashSet<VisualNode>();
        gatherNodes(visualGraph.getRootNode(), ret);
        if (showHiddenCheckBox.isSelected() == false)
            ret.removeAll(visualGraph.getHiddenNodes());
        return ret;
    }

    private HashSet<VisualNode> visibleNodes = new HashSet<VisualNode>();
    private String lastFilter = "";

    private void buildTree() {
        this.visualGraph = this.pipeline.getCurrentGraph();
        // Get original viewport to keep the scrolling state of scrollpane between updates.
        JViewport originalViewport = null;
        if (this.scrollPane != null)
            originalViewport = scrollPane.getViewport();

        HashSet<VisualNode> newVisible = getVisibleNodes();
        if (visibleNodes.equals(newVisible) && lastFilter.equals(filter.getText())) {
            repaintTree();
            return;
        }

        visibleNodes = newVisible;
        lastFilter = filter.getText();
        if (scrollPane != null)
            this.remove(scrollPane);

        nodeToTreeNode = new HashMap<VisualNode, DefaultMutableTreeNode>();
        DefaultMutableTreeNode root = treeFromNode(visualGraph.getRootNode());

        DefaultTreeModel treeModel = new DefaultTreeModel(root);
        VisualNodeTreeListener list = new VisualNodeTreeListener();

        this.tree = new JTree(treeModel);

        for (int r = 0; r < tree.getRowCount(); r++)
            tree.collapseRow(r);

        for (VisualNode n : visualGraph.getNodes()) {
            VisualGroupNode vgn = (VisualGroupNode) n.getParent();
            if (vgn == null)
                continue;
            DefaultMutableTreeNode tn = nodeToTreeNode.get(vgn);
            if (tn == null)
                continue;
            tree.expandPath(new TreePath(tn.getPath()));

        }

        treeModel.addTreeModelListener(list);
        tree.setCellRenderer(new VisualNodeCellRendererEditor());

        scrollPane = new JScrollPane(tree);
        tree.addTreeSelectionListener(this);

        this.add(scrollPane);
        this.updateSelected();

        tree.addMouseListener(this);
        tree.addMouseMotionListener(this);
        tree.addTreeExpansionListener(list);
        tree.setEditable(true);
        tree.setCellEditor(new VisualNodeCellRendererEditor());

        if (originalViewport != null)
            this.scrollPane.getViewport().setViewPosition(new Point(originalViewport.getViewPosition()));
        this.repaintTree();
        Logging.debug("ui", "NodeBrowser: built tree.");
    }

    private void updateSelected() {
        updatingSelection = true;
        tree.clearSelection();
        ArrayList<VisualNode> sel = new ArrayList<VisualNode>(visualGraph.getSelected());

        for (VisualNode n : sel) {
            DefaultMutableTreeNode treeNode = nodeToTreeNode.get(n);
            if (treeNode == null)
                continue;
            tree.addSelectionPath(new TreePath(treeNode.getPath()));

        }
        updatingSelection = false;
    }

    private boolean buildingTree = false;


    public void graphStructureChanged(VisualGraph g) {
        if (this.buildingTree)
            return;

        this.buildingTree = true;
        this.buildTree();
        this.buildingTree = false;
    }

    public void visibleNodesChanged(VisualGraph g) {
        this.buildTree();
    }

    public void pointsOfInterestsChanged(VisualGraph g) {
        this.buildTree();
    }

    public void selectionChanged(VisualGraph g) {
        this.updateSelected();
    }

    public void valueChanged(TreeSelectionEvent e) {
        if (this.updatingSelection) {
            return;
        }

        TreePath[] paths = tree.getSelectionPaths();
        updatingSelection = true;
        visualGraph.clearSelected();
        if (paths == null) {
            updatingSelection = false;
            return;
        }
        for (TreePath path : paths) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
                    .getLastPathComponent();
            VisualNode n = (VisualNode) node.getUserObject();
            visualGraph.addSelected(n);
        }
        visualGraph.selectionChanged();

        updatingSelection = false;
    }

    public void repaintTree() {
        this.revalidate();
        this.tree.setCellRenderer(new VisualNodeCellRendererEditor());
        this.repaint();
        this.tree.repaint();
    }

    class VisualNodeCellRendererEditor extends AbstractCellEditor implements
            TreeCellEditor, TreeCellRenderer {

        JLabel nameLabel;
        JPanel renderer;
        JToggleButton posPOI;
        JToggleButton negPOI;

        private DefaultMutableTreeNode getTreeNode(int x, int y) {
            TreePath path = tree.getPathForLocation(x, y);
            if (path != null) {
                Object node = path.getLastPathComponent();
                if ((node != null) && (node instanceof DefaultMutableTreeNode)) {
                    DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
                    return treeNode;
                }
            }
            return null;

        }

        private VisualNode getTreeNode(EventObject e) {
            if (e instanceof MouseEvent == false)
                return null;
            MouseEvent mouseEvent = (MouseEvent) e;

            DefaultMutableTreeNode node1 = getTreeNode(mouseEvent.getX(),
                    mouseEvent.getY());
            if (node1.getUserObject() instanceof VisualNode == false)
                return null;
            VisualNode vn = (VisualNode) node1.getUserObject();
            JLabel lab = new JLabel(vn.getName());
            int buttonw = posPOI.getPreferredSize().width
                    + negPOI.getPreferredSize().width;
            DefaultMutableTreeNode node2 = getTreeNode(mouseEvent.getX()
                    - buttonw, mouseEvent.getY());
            if (node1 == node2)
                return null;
            return vn;

        }

        ChangeEvent changeEvent = null;
        VisualNode currentNode = null;
        private DefaultTreeCellRenderer defaultRenderer = new DefaultTreeCellRenderer();

        public VisualNodeCellRendererEditor() {
            renderer = new JPanel();

            renderers.put(renderer, null);

            // renderer.setLayout(new BoxLayout(renderer, BoxLayout.X_AXIS));
            GridBagLayout layout = new GridBagLayout();

            renderer.setLayout(layout);

            nameLabel = new JLabel("zxc");
            nameLabel.setFont(new Font(Font.SANS_SERIF, 0, 12));
            posPOI = new JToggleButton("+");
            posPOI.putClientProperty("JComponent.sizeVariant", "mini"); // mini / small / large
            negPOI = new JToggleButton("-");
            negPOI.putClientProperty("JComponent.sizeVariant", "mini"); // mini / small / large
            posPOI.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));
            negPOI.setFont(posPOI.getFont());
            posPOI.setMaximumSize(new Dimension(20, nameLabel
                    .getPreferredSize().height));
            negPOI.setMaximumSize(new Dimension(20, nameLabel
                    .getPreferredSize().height));

            negPOI.setMargin(new Insets(0, 0, 0, 0));
            posPOI.setMargin(new Insets(0, 0, 0, 0));

            ChangeListener commonAction = new ChangeListener() {
                boolean changing = false;

                public void stateChanged(ChangeEvent e) {
                    if (currentNode == null) return;

                    if (changing)
                        return;
                    changing = true;

                    if (posPOI.isSelected() && negPOI.isSelected()) {
                        if (posPOI == e.getSource())
                            negPOI.setSelected(false);
                        else
                            posPOI.setSelected(false);
                    }
                    if (currentNode != null) {
                        if (negPOI.isSelected())
                            pipeline.addNodeOfInterest(currentNode, -1.0);
                        else if (posPOI.isSelected())
                            pipeline.addNodeOfInterest(currentNode, +1.0);
                        else
                            pipeline.addNodeOfInterest(currentNode, 0.0);
                        pipeline.settingsChanged(true);
                    }
                    posPOI.setForeground(posPOI.isSelected() ? Color.RED
                            : Color.black);

                    negPOI.setForeground(negPOI.isSelected() ? Color.BLUE
                            : Color.black);

                    changing = false;
                    renderer.revalidate();
                    renderer.repaint();
                    repaintTree();

                }
            };

            posPOI.addChangeListener(commonAction);
            negPOI.addChangeListener(commonAction);
            negPOI.setMinimumSize(negPOI.getPreferredSize());
            posPOI.setMinimumSize(posPOI.getPreferredSize());
            renderer.add(posPOI);
            renderer.add(negPOI);
            renderer.add(nameLabel);


            // renderer.add(islider);
            // posPOI.setAlignmentX(RIGHT_ALIGNMENT);
            // negPOI.setAlignmentX(RIGHT_ALIGNMENT);
        }

        public Object getCellEditorValue() {
            return "mo";
        }

        public boolean shouldSelectCell(EventObject e) {
            return false;
        }

        public boolean isCellEditable(EventObject event) {
            VisualNode vn = getTreeNode(event);

            if (vn == null)
                return false;

            return vn.getClass().equals(VisualNode.class);
        }

        public Component getTreeCellEditorComponent(JTree tree, Object value,
                                                    boolean selected, boolean expanded, boolean leaf, int row) {
            TreePath path = tree.getPathForRow(row);
            currentNode = null;
            if (path != null) {
                Object node = path.getLastPathComponent();
                if ((node != null) && (node instanceof DefaultMutableTreeNode)) {

                    DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;

                    VisualNode vn = (VisualNode) treeNode.getUserObject();
                    nameLabel.setText(vn.getName() + "   ");
                    nameLabel.setForeground(ColorPalette.getTextColorForBackground(vn.getColor()));

                    if (vn instanceof VisualGroupNode)
                        return nameLabel;

                    double poi = visualGraph.getInterestingness(vn);
                    posPOI.setSelected(poi > 0);
                    negPOI.setSelected(poi < 0);

                    if (poi != 0.0)
                        nameLabel.setText(vn.getName() + " "
                                + ((poi > 0) ? "+1" : "-1"));
                    currentNode = vn;
                    posPOI.setForeground(posPOI.isSelected() ? Color.RED
                            : Color.black);

                    negPOI.setForeground(negPOI.isSelected() ? Color.BLUE
                            : Color.black);
                    // renderer.setPreferredSize(new Dimension(400,
                    // (int) nameLabel.getPreferredSize().getHeight()));
                    // // renderer.setPreferredSize(renderer.getMaximumSize());
                    // renderer.updateUI();

                    return renderer;
                }
            }
            return new JLabel("fuu " + path);
        }

        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean selected, boolean expanded, boolean leaf, int row,
                                                      boolean hasFocus) {
            Component returnValue = null;
            if ((value != null) && (value instanceof DefaultMutableTreeNode)) {
                Object userObject = ((DefaultMutableTreeNode) value)
                        .getUserObject();

                if (userObject instanceof VisualNode) {
                    VisualNode vn = (VisualNode) userObject;

                    double interest = visualGraph.getInterestingness(vn);
                    nameLabel.setText(vn.getName() + "   ");
                    nameLabel.setForeground(ColorPalette.getTextColorForBackground(vn.getColor()));

                    if (vn instanceof VisualGroupNode) {
                        posPOI.setEnabled(false);
                        negPOI.setEnabled(false);
                    } else {

                        posPOI.setEnabled(true);
                        negPOI.setEnabled(true);
                    }
                    posPOI.setSelected(interest > 0);
                    negPOI.setSelected(interest < 0);
                    if (interest != 0.0)
                        nameLabel.setText(vn.getName() + " "
                                + ((interest > 0) ? "+1" : "-1"));
                    renderer.setBackground(vn.getColor());
                    renderer.setEnabled(tree.isEnabled());
                    posPOI.setForeground(posPOI.isSelected() ? Color.RED
                            : Color.black);

                    negPOI.setForeground(negPOI.isSelected() ? Color.BLUE
                            : Color.black);

                    returnValue = renderer;
                }
            }
            if (returnValue == null) {
                returnValue = defaultRenderer.getTreeCellRendererComponent(
                        tree, value, selected, expanded, leaf, row, hasFocus);
            }
            return returnValue;
        }
    }

    class VisualNodeCellRenderer implements TreeCellRenderer {
        JLabel nameLabel;
        JPanel renderer;
        JToggleButton posPOI;
        JToggleButton negPOI;
        DefaultTreeCellRenderer defaultRenderer = new DefaultTreeCellRenderer();

        public VisualNodeCellRenderer() {
            renderer = new JPanel();
            GridBagLayout layout = new GridBagLayout();

            renderer.setLayout(layout);
            // renderer.setLayout(new BoxLayout(renderer, BoxLayout.X_AXIS));
            nameLabel = new JLabel(" ");
            posPOI = new JToggleButton("+");
            posPOI.setMaximumSize(new Dimension(20, 20));
            negPOI = new JToggleButton("-");
            negPOI.setMaximumSize(new Dimension(20, 20));
            posPOI.setFont(new Font(posPOI.getFont().getName(), Font.BOLD, 8));
            negPOI.setFont(posPOI.getFont());
            //posPOI.setMaximumSize(new Dimension(Short.MAX_VALUE, nameLabel
            // 		.getPreferredSize().height));
            //negPOI.setMaximumSize(new Dimension(Short.MAX_VALUE, nameLabel
            //		.getPreferredSize().height));
            posPOI.setAlignmentX(RIGHT_ALIGNMENT);
            negPOI.setAlignmentX(RIGHT_ALIGNMENT);

            renderer.add(nameLabel);
            renderer.add(posPOI);
            renderer.add(negPOI);
            // renderer.setMaximumSize(new
            // Dimension(Short.MAX_VALUE,Short.MAX_VALUE));

            // renderer.setPreferredSize(new Dimension(
            // Short.MAX_VALUE,
            // renderer.getPreferredSize().height));
        }

        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean selected, boolean expanded, boolean leaf, int row,
                                                      boolean hasFocus) {
            Component returnValue = null;
            if ((value != null) && (value instanceof DefaultMutableTreeNode)) {
                Object userObject = ((DefaultMutableTreeNode) value)
                        .getUserObject();

                if (userObject instanceof VisualNode) {
                    VisualNode vn = (VisualNode) userObject;
                    double interest = visualGraph.getInterestingness(vn);
                    nameLabel.setText(vn.getName());
                    posPOI.setSelected(interest > 0);
                    negPOI.setSelected(interest < 0);

                    renderer.setBackground(vn.getColor());
                    renderer.setEnabled(tree.isEnabled());
                    posPOI.setForeground(posPOI.isSelected() ? Color.RED
                            : Color.black);

                    negPOI.setForeground(negPOI.isSelected() ? Color.BLUE
                            : Color.black);

                    returnValue = renderer;
                }
            }
            if (returnValue == null) {
                returnValue = defaultRenderer.getTreeCellRendererComponent(
                        tree, value, selected, expanded, leaf, row, hasFocus);
            }
            return returnValue;
        }
    }

    public void colorsChanged(VisualGraph g) {
        repaint();
    }

    public void zoomRequested(VisualGraph g, Collection<LayoutItem> items) {

    }

    public void mousePressed(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON3)
            return;

        int selRow = tree.getRowForLocation(e.getX(), e.getY());
        TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());

        if (selPath == null)
            return;

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath
                .getLastPathComponent();


        VisualNode vn = (VisualNode) node.getUserObject();

        if (pipeline != null) {
            JPopupMenu menu = Menus.getInstance(pipeline).get2ndButtonMenu(vn, pipeline.getVisualizer());
            int scroll = scrollPane.getViewport().getViewPosition().y;
            menu.show(this, e.getX(), e.getY() - scroll);
        }

    }

    public void mouseMoved(MouseEvent e) {
        this.visualGraph = this.pipeline.getCurrentGraph();
        TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());

        if (selPath == null)
            return;

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();

        VisualNode vn = (VisualNode) node.getUserObject();
        visualGraph.setHighlighted(vn);
    }

    class VisualNodeTreeListener implements TreeModelListener,
            TreeExpansionListener {

        public void treeNodesChanged(TreeModelEvent e) {
        }

        public void treeNodesInserted(TreeModelEvent e) {
        }

        public void treeNodesRemoved(TreeModelEvent e) {
        }

        public void treeStructureChanged(TreeModelEvent e) {
        }

        public void treeCollapsed(TreeExpansionEvent e) {
            TreePath selPath = e.getPath();

            if (selPath == null)
                return;

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath
                    .getLastPathComponent();

            VisualNode vn = (VisualNode) node.getUserObject();

            if (vn instanceof VisualGroupNode) {
                VisualGroupNode vgn = (VisualGroupNode) vn;
                vgn.setOpen(false);
            }
        }

        public void treeExpanded(TreeExpansionEvent e) {
            TreePath selPath = e.getPath();

            if (selPath == null)
                return;

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath
                    .getLastPathComponent();

            VisualNode vn = (VisualNode) node.getUserObject();

            if (vn instanceof VisualGroupNode) {
                VisualGroupNode vgn = (VisualGroupNode) vn;
                Logging.info("ui", "isOpen = " + vgn.isOpen());
                vgn.setOpen(true);
            }
        }
    }

    public void mouseDragged(MouseEvent arg0) {
    }

    private long lastClick = 0;

    public void mouseClicked(MouseEvent e) {
        long time = System.currentTimeMillis();

        TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
        if (selPath == null)
            return;

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath
                .getLastPathComponent();

        LayoutItem item = (LayoutItem) node.getUserObject();

        // Double click
        if (time - lastClick < 200) {
            // Logging.debug("ui", "NodeBrowser doubleclick!");

            // Automatic zoom
            Collection<LayoutItem> neighbors;
            if (item != null) {
                neighbors = visualGraph.getNeighbors(item);
                neighbors.add(item);
            } else
                neighbors = visualGraph.getZOrderItems();

            visualGraph.zoomTo(neighbors);
        }

        lastClick = System.currentTimeMillis();
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }
}
