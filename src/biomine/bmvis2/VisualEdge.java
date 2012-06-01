package biomine.bmvis2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.*;

import javax.swing.plaf.basic.BasicLabelUI;

import biomine.bmvis2.color.ColorPalette;
import biomine.bmvis2.color.EdgeColoring;

/**
 * Edge used in VisualGraph. Contains methods.
 *
 * @author alhartik
 */
public class VisualEdge extends LabeledItem {
    public enum WeightType {PROBABILISTIC, FLOW, WEIGHT}

    public class Weight {
        public WeightType type;
        public double value;

        public Weight(WeightType type, double value) {
            this.type = type;
            this.value = value;
        }

        public String toString() {
            return this.type + ": " + this.value;
        }
    }

    private Collection<Weight> weights = new HashSet<Weight>();

    private VisualNode from;
    private VisualNode to;
    private String type;
    private boolean symmetric;

    private void init(VisualNode fr, VisualNode t, double goodness,
                      boolean symmetric, String type) {
        assert (fr != null && t != null);

        this.setType(type);
        this.from = fr;
        this.to = t;
        this.setSymmetric(symmetric);
        this.setGoodness(goodness);
        this.assignPositionAccordingToEndpoints();
        super.setHLColor(ColorPalette.NODE_HOVER);
        super.setNeighborHLColor(ColorPalette.NEIGHBOR_HOVER);
    }

    public VisualEdge(VisualGraph graph, VisualNode from, VisualNode to,
                      double goodness, boolean symmetric, String type) {
        super(graph);
        init(from, to, goodness, symmetric, type);
    }

    public VisualEdge(VisualNode from, VisualNode to, double goodness,
                      boolean symmetric, String type) {
        super(from.getGraph());
        init(from, to, goodness, symmetric, type);
    }

    public void assignPositionAccordingToEndpoints() {
        Vec2 fp = from.getPos();
        Vec2 tp = to.getPos();
        setPos(fp.plus(tp).scaled(0.5));
    }

    public final void setFrom(VisualNode from) {
        this.from = from;
    }

    public final VisualNode getFrom() {
        return from;
    }

    public final void setTo(VisualNode to) {
        this.to = to;
    }

    public final VisualNode getTo() {
        return to;
    }

    public void paint(Graphics2D g) {

        if (this.isHighlighted()) {
            g.setColor(Color.green);
        }
        edgeRenderer.drawEdge(g, this);

        super.paint(g);
    }


    /*
      * public boolean equals(Object o){ if(!(o instanceof VisualEdge))return
      * false; VisualEdge e = (VisualEdge)o; return from.equals(e.from)&&
      * to.equals(e.to);
      *
      * } public int hashCode(){ int x = to.hashCode(); return
      * from.hashCode()^(x<<16+x>>16); }
      */

    public final VisualNode getOther(VisualNode n) {
        if (n == from)
            return to;
        return from;
    }

    public final void setGoodness(double goodness) {
        this.weights.add(new Weight(WeightType.PROBABILISTIC, goodness));
        updateLabels();
    }

    public double getGoodness() {
        if (this.weights.size() == 0)
            this.getGraph().assignWeights(WeightType.PROBABILISTIC);
        if (this.weights.size() == 0)
            return 1.0;
        return this.getWeight(WeightType.PROBABILISTIC).value;
    }

    public void setWeight(WeightType type, double value) {
        Weight removable = null;
        for (Weight w : this.weights)
            if (w.type == type)
                removable = w;
        if (removable != null)
            this.weights.remove(removable);

        this.weights.add(new Weight(type, value));
    }

    public Weight getWeight(WeightType type) {
        for (Weight w : this.weights) {
            if (w.type == type)
                return w;
        }

        return null;
    }

    public final void setSymmetric(boolean symmetric) {
        this.symmetric = symmetric;
    }

    public final boolean isSymmetric() {
        return symmetric;
    }

    public final String getType() {
        return type;
    }

    public void reverse() {
        VisualNode tmp = to;
        to = from;
        from = tmp;
    }

    public static void setEdgeRenderer(EdgeRenderer edgeRenderer) {
        VisualEdge.edgeRenderer = edgeRenderer;
    }

    public static EdgeRenderer getEdgeRenderer() {
        return edgeRenderer;
    }

    /*
      * public void setLabels(String[] s){ String[] s2 = new String[s.length+1];
      * s2[0]=getType(); for(int i=0;i<s.length;i++) s2[i+1]=s[i];
      * super.setLabels(s2); }
      */

    public void setType(String type) {
        this.type = type;
        updateLabels();
    }

    @Override
    public boolean equals(Object o) {
        if (o.getClass().equals(this.getClass())) {
            VisualEdge e = (VisualEdge) o;

            if (!e.getType().equals(getType())
                    || e.getGoodness() != getGoodness())
                return false;

            if (e.getFrom() == getFrom() && e.getTo() == getTo())
                return true;
            else if (e.getFrom() == getTo() && e.getTo() == getFrom()
                    && e.isSymmetric())
                return true;
            return false;
        }
        return false;
    }

    public int hashCode() {
        return (getFrom().hashCode() + 1) * (getTo().hashCode() + 1);
    }

    public String toString() {
        return this.getFrom().toString() + " " + this.getType() + " "
                + this.getTo().toString();
    }

    public Color getBaseColor() {
        if (edgeColoring == null)
            return null;
        return edgeColoring.getColor(this);
    }

    private static EdgeRenderer edgeRenderer = new BezierEdgeRenderer();
    private static EdgeColoring edgeColoring = null;

    public static EdgeColoring getColoring() {
        return edgeColoring;
    }

    public static void setColoring(EdgeColoring edgeColoring) {
        VisualEdge.edgeColoring = edgeColoring;
    }

    /* LABELS */
    private ArrayList<String> extraLabels = new ArrayList<String>();

    private void updateLabels() {
        ArrayList<String> labels = new ArrayList<String>();
        try {
            labels.add(this.getType().replace("_", " "));
        } catch (Exception e) {
            Logging.warning("graph_drawing", "Error while adding type label for edge " + this);
            e.printStackTrace();
        }

        labels.addAll(extraLabels);
        super.setLabels(labels);
    }

    public void setExtraLabels(Collection<String> el) {
        extraLabels = new ArrayList<String>(el);
        updateLabels();
    }

    public List<String> getExtraLabels() {
        return Collections.unmodifiableList(extraLabels);
    }
}
