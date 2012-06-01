package biomine.bmvis2.color;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IllegalFormatFlagsException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Color palette for the BMVis visualization.
 *1
 * @author Kimmo Kulovesi
 * @author Atte Hinkka
 */
public class ColorPalette {
    /**
     * Pastel shades.
     */
    public static Color RED_TO_PURPLE_1 = new Color(255, 168, 168);
    public static Color RED_TO_PURPLE_2 = new Color(254, 169, 243);
    public static Color RED_TO_PURPLE_3 = new Color(196, 171, 254);
    public static Color PURPLE_TO_BLUE_1 = new Color(255, 134, 255);
    public static Color PURPLE_TO_BLUE_2 = new Color(199, 155, 242);
    public static Color PURPLE_TO_BLUE_3 = new Color(123, 202, 225);
    public static Color BLUE_TO_GREEN_1 = new Color(153, 133, 255);
    public static Color BLUE_TO_GREEN_2 = new Color(117, 236, 253);
    public static Color BLUE_TO_GREEN_3 = new Color(139, 254, 168);
    public static Color GREEN_TO_YELLOW_1 = new Color(147, 238, 170);
    public static Color GREEN_TO_YELLOW_2 = new Color(111, 255, 68);
    public static Color GREEN_TO_YELLOW_3 = new Color(238, 240, 147);

    public static final Color[] pastelShades = {
            ColorPalette.BLUE_TO_GREEN_1,
            ColorPalette.BLUE_TO_GREEN_2,
            ColorPalette.BLUE_TO_GREEN_3,
            ColorPalette.GREEN_TO_YELLOW_1,
            ColorPalette.GREEN_TO_YELLOW_2,
            ColorPalette.GREEN_TO_YELLOW_3,
            ColorPalette.RED_TO_PURPLE_1,
            ColorPalette.RED_TO_PURPLE_2,
            ColorPalette.RED_TO_PURPLE_3,
            ColorPalette.PURPLE_TO_BLUE_1,
            ColorPalette.PURPLE_TO_BLUE_2,
            ColorPalette.PURPLE_TO_BLUE_3
    };

    /**
     * "Web 2.0" colors
     */
    public static Color SHINY_SILVER = new Color(238, 238, 238);
    public static Color MAGNOLIA = new Color(249, 247, 237);
    public static Color INTERACTIVE_YELLOW = new Color(255, 255, 136);
    public static Color QOOP_MINT = new Color(205, 235, 139);
    public static Color GMAIL_BLUE = new Color(195, 217, 255);

    public static final Color web20[] = {
            SHINY_SILVER, MAGNOLIA, INTERACTIVE_YELLOW, QOOP_MINT, GMAIL_BLUE
    };

    /**
     * Predefined node type colors.
     */
    public static final HashMap<String, Color> NODE_COLORS = new HashMap<String, Color>();

    /**
     * Predefined queryset colors.
     */
    public static final Color QUERYSET_START = new Color(0, 175, 0);
    public static final Color QUERYSET_SOURCE = new Color(0, 175, 0);
    public static final Color QUERYSET_END = new Color(220, 0, 0);
    public static final Color QUERYSET_TARGET = new Color(220, 0, 0);
    public static final Color QUERYSET_START_END = new Color(220, 0, 220);
    public static final Color QUERYSET_SOURCE_TARGET = new Color(220, 0, 220);
    public static final Color QUERYSET_RANK = new Color(0, 175, 220);

    public static final HashMap<String, Color> QUERYSET_COLORS = new HashMap<String, Color>();

    static {
        QUERYSET_COLORS.put("start", QUERYSET_START);
        QUERYSET_COLORS.put("source", QUERYSET_SOURCE);
        QUERYSET_COLORS.put("end", QUERYSET_END);
        QUERYSET_COLORS.put("target", QUERYSET_TARGET);
        QUERYSET_COLORS.put("start,end", QUERYSET_START_END);
        QUERYSET_COLORS.put("source,target", QUERYSET_SOURCE_TARGET);
        QUERYSET_COLORS.put("rank", QUERYSET_RANK);

        NODE_COLORS.put("Node", new Color(240, 240, 240));

        // Hush.
        NODE_COLORS.put("Organization", new Color(239, 231, 176));
        NODE_COLORS.put("Person", new Color(198, 202, 248));
        NODE_COLORS.put("Project", new Color(174, 214, 234));
        NODE_COLORS.put("ResearchArea", new Color(190, 229, 201));
        NODE_COLORS.put("Patent", new Color(255, 215, 253));
        NODE_COLORS.put("Class", new Color(194, 237, 218));

        // Saturated greens
        NODE_COLORS.put("Sequence", new Color(211, 247, 163));
        NODE_COLORS.put("Gene", new Color(211, 247, 163));
        NODE_COLORS.put("Protein", new Color(199, 240, 178));
        NODE_COLORS.put("Enzyme", new Color(193, 234, 174));
        // NODE_COLORS.put("ProteinGroup", new Color(199, 240, 178));
        NODE_COLORS.put("AllelicVariant", new Color(199, 240, 178));

        // Beige
        NODE_COLORS.put("Article", new Color(250, 230, 160));

        // Cyans (to do, make greener)
        NODE_COLORS.put("HomologGroup", new Color(187, 237, 215));
        NODE_COLORS.put("OrthologGroup", new Color(194, 237, 218));

        // Desaturated blues
        NODE_COLORS.put("GO", new Color(185, 218, 234));
        NODE_COLORS.put("BiologicalProcess", new Color(185, 218, 234));
        NODE_COLORS.put("MolecularFunction", new Color(189, 223, 239));
        NODE_COLORS.put("CellularComponent", new Color(174, 214, 234));

        // Light purples
        NODE_COLORS.put("Ligand", new Color(210, 203, 240));
        NODE_COLORS.put("Substance", new Color(210, 203, 240));
        NODE_COLORS.put("Compound", new Color(210, 203, 240));
        NODE_COLORS.put("Drug", new Color(203, 196, 233));
        NODE_COLORS.put("Glycan", new Color(198, 200, 242));

        // Pink
        NODE_COLORS.put("GenomicContext", new Color(255, 215, 253));

        // Blues
        NODE_COLORS.put("Locus", new Color(180, 196, 239)); // unused
        NODE_COLORS.put("Phenotype", new Color(193, 209, 255));

        // Greens
        NODE_COLORS.put("Family", new Color(179, 226, 192));
        NODE_COLORS.put("Region", new Color(195, 229, 204));
        NODE_COLORS.put("Domain", new Color(195, 229, 204));
        NODE_COLORS.put("Repeat", new Color(195, 229, 204));
        NODE_COLORS.put("Site", new Color(190, 229, 201));
        NODE_COLORS.put("ActiveSite", new Color(190, 229, 201));
        NODE_COLORS.put("BindingSite", new Color(190, 229, 201));
        NODE_COLORS.put("PostTranslationalModification", new Color(190, 229, 201));

        // Purple
        NODE_COLORS.put("Pathway", new Color(208, 185, 231));

        // Browns
        NODE_COLORS.put("Tissue", new Color(229, 218, 189));
        // NODE_COLORS.put("Organism", new Color(229, 215, 177));
        NODE_COLORS.put("MeSHHeading", new Color(239, 231, 176));
        NODE_COLORS.put("OMIM", new Color(239, 231, 176));
    }

    /**
     * Node text color.
     */
    public static final Color NODE_TEXT = Color.GRAY;

    /**
     * Groupnode border stroke color.
     */
    public static final Color GROUPNODE_STROKE = Color.GRAY;

    /**
     * Edgenode fill color.
     */
    public static final Color EDGENODE_FILL = Color.BLACK;

    /**
     * Default node fill color. This is generally unused, due to the use of
     * type-specific colors set in NODE_COLORS.
     */
    public static final Color DEFAULT_FILL = new Color(200, 215, 255);

    /**
     * Selected node color.
     */
    public static final Color NODE_SELECT = new Color(255, 100, 100);

    /**
     * Node hover highlight color.
     */
    public static final Color NODE_HOVER = new Color(255, 155, 140);

    /**
     * Node hover neighbor highlight color.
     */
    public static final Color NEIGHBOR_HOVER = new Color(255, 175, 0);

    /**
     * Selected node hover highlight color.
     */
    public static final Color SELECT_HOVER = new Color(255, 120, 120);

    /**
     * Node hover neighbor highlight color for selected nodes.
     */
    public static final Color SELECT_NEIGHBOR = new Color(255, 100, 35);


    /**
     * Parses color expressions in the traditional BMVis format R/G/B.
     *
     * @param expression
     * @return Color
     */
    public static Color parseSlashLimitedColorString(String expression) {
        String[] parts = expression.split("/");
        if (parts.length >= 3) {
            int r = Integer.parseInt(parts[0], 10);
            int g = Integer.parseInt(parts[1], 10);
            int b = Integer.parseInt(parts[2], 10);
            Color c = new Color(r, g, b);
            return c;
        }
        return null;
    }

    /**
     * Parses color expressions in the web color format #RRGGBB.
     *
     * @param expression
     * @return Color
     */
    public static Color parseWebColorString(String expression) {
        Pattern pat = Pattern.compile("^.(..)(..)(..)$");
        Matcher m = pat.matcher(expression);
        if (m.matches() && m.groupCount() == 3) {
            int r = Integer.parseInt(m.group(1), 16);
            int g = Integer.parseInt(m.group(2), 16);
            int b = Integer.parseInt(m.group(3), 16);
            Color c = new Color(r, g, b);
            return c;
        }
        return null;
    }

    /**
     * Utility method to parse either R/G/B or #RRGGBB colors.
     *
     * @param String expression
     * @return Color
     */
    public static Color parseColor(String expression) {
        if (expression.charAt(0) == '#')
            return parseWebColorString(expression);
        else
            return parseSlashLimitedColorString(expression);
    }

    /**
     * Utility method to parse a comma separated list of colors to a List&lt;Color&gt;.
     *
     * @param String expression
     * @return List&ltColor&gt
     */
    public static List<Color> parseCommaSeparatedColors(String expression) {
        ArrayList<Color> ret = new ArrayList<Color>();

        String[] parts = expression.split(",");
        for (String part : parts)
            ret.add(parseColor(part));

        return ret;
    }

    public static final double getLuminosity(Color color) {
        if (color == null)
            return 1.0;
        double cr = color.getRed() / 256.0;
        double cg = color.getGreen() / 256.0;
        double cb = color.getBlue() / 256.0;
        double luminosity = (cr * .2 + cg * .7 + cb * .1);
        return luminosity;
    }

    public static final Color getTextColorForBackground(Color backgroundColor) {
        if (ColorPalette.getLuminosity(backgroundColor) < 0.5)
            return Color.WHITE;
        return Color.BLACK;
    }
}
