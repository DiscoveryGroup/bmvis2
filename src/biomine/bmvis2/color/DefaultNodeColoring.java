package biomine.bmvis2.color;

import java.awt.Color;
import java.util.*;

import javax.swing.JComponent;

import biomine.bmgraph.BMNode;
import biomine.bmvis2.BMGraphAttributes;
import biomine.bmvis2.VisualGraph;
import biomine.bmvis2.VisualGroupNode;
import biomine.bmvis2.VisualNode;

/**
 * "NodeColoring" for default colors.
 *
 * @author alhartik
 */
public class DefaultNodeColoring implements NodeColoring {
    private Map colorMap = new HashMap<String, Color>();
    private long colorMapGraphVersion = 0;

    private void determineColors(VisualGraph graph) {
        if (colorMapGraphVersion == graph.getCombinedVersion())
            return;

        if (colorMap.size() > 0)
            colorMap = new HashMap<String, Color>();

        Set<String> types = new HashSet<String>();
        for (VisualNode node : graph.getAllNodes()) {
            if (node instanceof VisualGroupNode)
                continue;
            types.add(node.getType());
        }

        List<String> typesOrdered = new ArrayList<String>(types);
        Collections.sort(typesOrdered);

        int i = 0;
        for (String type : typesOrdered) {
            if (ColorPalette.NODE_COLORS.containsKey(type)) {
                colorMap.put(type, ColorPalette.NODE_COLORS.get(type));
                continue;
            }
            try {
                colorMap.put(type, ColorPalette.pastelShades[i]);
            } catch (IndexOutOfBoundsException iob) {
                colorMap.put(type, ColorPalette.DEFAULT_FILL);
            }
            i++;
        }

        this.colorMapGraphVersion = graph.getCombinedVersion();
    }

    public JComponent colorLegendComponent() {
        return null;
    }


    public Color getFillColor(VisualNode n) {
        List<Color> manualFills = Collections.EMPTY_LIST;

        BMNode bmNode = n.getBMNode();
        if (bmNode != null) {
            String fill = bmNode.get(BMGraphAttributes.FILL_KEY);
            if (fill == null)
                fill = bmNode.get(BMGraphAttributes.FILLS_KEY);
            if (fill != null)
                manualFills = ColorPalette.parseCommaSeparatedColors(fill);
        }

        if (manualFills.size() > 0)
            return manualFills.iterator().next();
        else if (n instanceof VisualGroupNode)
            return Color.GREEN;

        this.determineColors(n.getGraph());

        return (Color) this.colorMap.get(n.getType());
    }

    public String getByName() {
        return "by node type";
    }
}
