package biomine.bmvis2;

import java.awt.*;
import java.util.*;
import java.util.List;

import biomine.bmgraph.BMNode;
import biomine.bmvis2.color.ColorPalette;
import biomine.bmvis2.color.NodeColoring;

public class VisualNode extends LabeledItem {
    private static long nextId = 0;

    private static String generateNewId() {
        return "N" + (nextId++);
    }

    private VisualGroupNode parent;
    private String name;
    private String type;
    private boolean selected;
    private HashMap<VisualNode, HashSet<VisualEdge>> edges;
    private String id;

    /**
     * You must remember to call graph.selectionChanged() on any changes to selections!
     *
     * @param selected
     */
    public void setSelected(boolean selected) {
        boolean oldSelect = this.selected;
        this.selected = selected;

        if (oldSelect != selected) {
            if (selected)
                this.getGraph().addSelected(this);
            else
                this.getGraph().removeSelected(this);
            this.getGraph().colorsChanged();
        }

    }

    public boolean isSelected() {
        return selected;
    }

    public static String resolveVisibleName(BMNode node) {
        String label = node.get("label");

        if (label == null)
            label = node.get("Label");
        if (label == null)
            label = node.get("ShortName");
        if (label == null)
            label = node.getId();

        return label;
    }


    private void init(VisualGraph graph, BMNode node, VisualGroupNode parent,
                      String name) {
        edges = new HashMap<VisualNode, HashSet<VisualEdge>>();
        Color c = null;
        id = "" + generateNewId();
        if (node != null) {
            id = node.getId();
            String posValue = node.get(BMGraphAttributes.POS_KEY);
            if (posValue != null) {
                String posStr[] = posValue.split(",");

                double x = Double.parseDouble(posStr[0]);
                double y = Double.parseDouble(posStr[1]);
                setPos(new Vec2(x, y));
            } else {
                setPos(new Vec2(0, 0));
            }

            setType(node.getType());
            String pinnedStr = node.get(BMGraphAttributes.PINNED_KEY);
            if (pinnedStr == null || pinnedStr.equals("0"))
                setPositionFixed(false);
            else
                setPositionFixed(true);

            this.setName(resolveVisibleName(node));

            c = ColorPalette.NODE_COLORS.get(node.getType());
            String qs = node.get(BMGraphAttributes.QUERYSET_KEY);

            if (qs != null) {
                this.getGraph().addNodeOfInterest(this, 1.0);
            }

        } else {
            setName(name);
            setType("Unknown type");
        }
        bmNode = node;

        if (c == null)
            c = ColorPalette.DEFAULT_FILL;

        super.setBaseColor(c);
        super.setNeighborHLColor(ColorPalette.NEIGHBOR_HOVER);

        super.setHLColor(ColorPalette.NODE_HOVER);

        setParent(parent);

    }

    public VisualNode(VisualGraph graph, VisualGroupNode group, String name) {
        super(graph);
        init(graph, null, group, name);
    }

    public VisualNode(VisualGroupNode group, String name) {
        super(group.getGraph());
        init(group.getGraph(), null, group, name);
    }

    public VisualNode(VisualGraph graph, BMNode node, VisualGroupNode group) {
        super(graph);
        init(graph, node, group, "");
    }

    /**
     * Creates copy of node in other visualgraph.
     *
     * @param graph
     * @param on
     * @param parent2
     */
    public VisualNode(VisualGraph graph, VisualNode on, VisualGroupNode parent2) {
        super(graph);
        init(graph, on.getBMNode(), parent2, "");
        id = on.getId();
        setPositionFixed(on.isPositionFixed());
        setPos(on.getPos());
    }

    public String getId() {
        return id;
    }

    public String getBMPos() {
        Vec2 p = this.getPos();
        return p.x + "," + p.y;
    }


    private BMNode bmNode;

    public BMNode getBMNode() {
        return bmNode;
    }

    public void paint(Graphics2D g) {
        super.setStrokeWidth(0.0);
        super.setStrokeColor(Color.BLACK);
        super.setBaseColor(this.getBaseColor());

        double poiScore = 0.0;
        boolean selected = false;

        if (this.getGraph() != null) {
            Double interestingness = this.getGraph().getNodesOfInterest().get(this);
            if (interestingness != null)
                poiScore = interestingness;
        }

        if (this.isSelected())
            selected = true;

        if (selected)
            super.setStrokeWidth(2);

        // These variables are for storing the original colors of negative POIs
        Color originalStrokeColor = null;
        Color originalBaseColor = null;

        if (poiScore > 0) {
            String qs = this.bmNode.get(BMGraphAttributes.QUERYSET_KEY);
            Color strokeColor = ColorPalette.QUERYSET_COLORS.get(qs);
            if (strokeColor == null)
                strokeColor = ColorPalette.QUERYSET_COLORS.get("start");
            if (strokeColor != null)
                super.setStrokeColor(strokeColor);
            else
                super.setStrokeColor(Color.RED);
            this.setStrokeWidth(3);
            if (selected)
                super.setStrokeColor(Color.BLACK);
        } else if (poiScore < 0) {
            originalBaseColor = this.getBaseColor();
            originalStrokeColor = this.getStrokeColor();
            super.setStrokeColor(Color.GRAY);
            super.setBaseColor(Color.GRAY);
        }

        super.paint(g);

        if (originalBaseColor != null)
            this.setBaseColor(originalBaseColor);
        if (originalStrokeColor != null)
            this.setStrokeColor(originalStrokeColor);


/*        if (this.getGraph() != null) {
            Double interestingness = getGraph().getNodesOfInterest().get(this);

            if (interestingness != null) {
                if (interestingness > 0)
                    this

                Double baseLineY = this.getShape().getY() + 5;
                Double baseLineX = this.getShape().getX() - 5;

                try {
                    SVGDiagram diagram;

                    if (interestingness > 0)
                        diagram = ResourceUtils.getSVGDiagramByPath("/resources/posPOI.svg");
                    else
                        diagram = ResourceUtils.getSVGDiagramByPath("/resources/negPOI.svg");

                    int x = new Double(baseLineX).intValue() - 8;
                    int y = new Double(baseLineY).intValue() - 18;
                    Graphics2D signGraphics = (Graphics2D) g.create(x, y, 22, 22);
                    diagram.render(signGraphics);
                } catch (Exception e) {
                    Logging.error("graph_drawing", "Exception while loading SVG file, resorting to old methods...");

                    Font currentFont = g.getFont();
                    Font signFont = new Font(Font.MONOSPACED, Font.BOLD, 18);
                    g.setFont(signFont);

                    String sign = (interestingness < 0) ? "-" : "+";
                    Rectangle2D sr = g.getFontMetrics().getStringBounds(sign, g);
                    double w = Math.max(sr.getWidth(), sr.getHeight());

                    w -= 4;
                    sr.setRect(baseLineX - (w - sr.getWidth()) / 2,
                            baseLineY + sr.getY() - (w - sr.getHeight()) / 2, w, w);
                    Arc2D.Double circle = new Arc2D.Double(sr, 0, 360, Arc2D.CHORD);

                    Color color;

                    if (interestingness < 0)
                        color = Color.blue;
                    else
                        color = Color.red;
                    g.setColor(color);

                    g.fill(circle);
                    g.setColor(Color.black);
                    g.setStroke(new BasicStroke());
                    g.draw(circle);

                    g.setColor(Color.WHITE);
                    g.drawString(sign, baseLineX.floatValue(),
                            baseLineY.floatValue());
                    g.setFont(currentFont);

                }

            }
        } */
    }

    public void setParent(VisualGroupNode par) {
        if (this.parent != null)
            this.parent.removeChild(this);
        this.parent = par;
        if (par != null) {
            par.addChild(this);
        } else
            this.setVisible(true);
    }

    public VisualGroupNode getParent() {
        return parent;
    }

    private boolean visible;

    protected void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }

    public VisualNode getVisibleAncestor() {
        if (visible)
            return this;
        if (parent == null)
            return this;
        return parent.getVisibleAncestor();
    }

    /**
     * returns all ancestors up to root. Collection will also include this object.
     *
     * @return Collection of ancestors
     */
    public Collection<VisualNode> getAncestors() {
        ArrayList<VisualNode> ret = new ArrayList<VisualNode>();
        VisualNode vn = this;
        while (vn != null) {
            ret.add(vn);
            vn = vn.getParent();
        }
        return ret;
    }

    public void setName(String name) {
        this.name = name;
        this.refreshLabels();
    }

    /**
     * Update name and type labels for node.
     */
    public void refreshLabels() {
        if (this.getGraph().getShowNodeTypes())
            this.setBasicLabels(Arrays.asList(this.getName(), this.getType()));
        else
            this.setBasicLabels(Arrays.asList(this.getName()));
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return getName();
    }

    public Color getColor() {
        if (this.isVisible() == false) {
            return Color.WHITE;
        }
        if (this.isSelected())
            return ColorPalette.NODE_SELECT;
        return super.getColor();
    }

    public Color getBaseColor() {
        if (coloring != null)
            return coloring.getFillColor(this);
        return super.getBaseColor();
    }

    public VisualEdge getEdge(VisualNode n) {
        try {
            Set<VisualEdge> visualEdges = edges.get(n);
            if (visualEdges.size() > 1)
                Logging.error("graph_reading", "Multiple edges for getEdge! Returning one.");
            return visualEdges.iterator().next();
        } catch (NullPointerException e) {
            return null;
        }
    }

    protected final void addEdge(VisualEdge e) {
        if (e.getFrom() == e.getTo())
            return;

        if (e.getTo() != this && e.getFrom() != this)
            return;
        // if (edges.containsKey(e.getOther(this)))
        // return;

        if (edges.get(e.getOther(this)) == null)
            edges.put(e.getOther(this), new HashSet<VisualEdge>());

        edges.get(e.getOther(this)).add(e);
        if (e.getOther(this).getEdges().contains(e) == false)
            e.getOther(this).addEdge(e);
    }

    protected void removeEdge(VisualEdge e) {

        HashSet<VisualEdge> ed = edges.get(e.getOther(this));

        if (ed == null)
            return;
        if (!ed.contains(e))
            return;

        ed.remove(e);
        if (ed.size() == 0)
            edges.remove(e.getOther(this));

        e.getOther(this).removeEdge(e);
    }

    public Collection<VisualEdge> getEdges() {
        HashSet<VisualEdge> ret = new HashSet<VisualEdge>();
        for (HashSet<VisualEdge> r : edges.values())
            ret.addAll(r);
        return ret;
    }


    public Collection<VisualNode> getNeighbors() {
        HashSet<VisualNode> ret = new HashSet<VisualNode>();
        for (VisualEdge ve : this.getEdges())
            ret.add(ve.getOther(this));
        return ret;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
        this.refreshLabels();
    }

    public int getDegree() {
        return this.edges.keySet().size();
    }

    private static NodeColoring coloring = null;

    public static NodeColoring getColoring() {
        return coloring;
    }

    public static void setColoring(NodeColoring coloring) {
        VisualNode.coloring = coloring;
    }

    public int getVisibleNeighborCount() {
        int count = 0;

        for (VisualNode n : this.edges.keySet())
            if (!n.equals(this) && n.isVisible())
                count++;

        return count;
    }


    /* LABELS */

    private ArrayList<String> extraLabels = new ArrayList<String>();
    private ArrayList<String> basicLabels = new ArrayList<String>();

    private void updateLabels() {
        ArrayList<String> lbl = new ArrayList<String>();
        lbl.addAll(basicLabels);
        lbl.addAll(extraLabels);
        super.setLabels(lbl);
    }

    public boolean getShowNodeType() {
        return this.getGraph().getShowNodeTypes();
    }

    /**
     * Extra labels are additional labels that are added below name and type.
     * You should not call setLabels for visualnodes, as this might break stuff.
     *
     * @param arr
     */
    public void setExtraLabels(Collection<String> arr) {
        extraLabels = new ArrayList<String>(arr);
        updateLabels();
    }

    private void setBasicLabels(Collection<String> arr) {
        basicLabels = new ArrayList<String>(arr);
        this.updateLabels();
    }

    public List<String> getExtraLabels() {
        return Collections.unmodifiableList(extraLabels);
    }

    public void clearExtraLabels() {
        setExtraLabels(Collections.EMPTY_LIST);
    }

    public void addExtraLabel(String str) {
        extraLabels.add(str);
        updateLabels();
    }

    protected void setId(String id) {
        this.id = id;
    }

}
