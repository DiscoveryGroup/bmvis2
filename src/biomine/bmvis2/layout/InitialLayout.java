package biomine.bmvis2.layout;

import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;

import biomine.bmgraph.BMEdge;
import biomine.bmgraph.BMGraph;
import biomine.bmgraph.BMGraphUtils;
import biomine.bmgraph.BMNode;
import biomine.bmvis2.BMGraphAttributes;
import biomine.bmvis2.Logging;
import biomine.bmvis2.Vec2;

/**
 * Selects how to position components of graph. Larger components are assigned
 * larger squares.
 *
 * @author alhartik
 */
class SquarePacker {
    ArrayList<boolean[]> arr;
    int w;

    SquarePacker(int w) {
        this.w = w;
        //System.out.println("w = "+w);
        arr = new ArrayList<boolean[]>();

    }

    Point pack(int s) {
        if (s > w)
            return null;
        Point ret = new Point(0, 0);
        for (int y = 0; y < arr.size() - s + 1; y++) {
            for (int x = 0; x < w - s + 1; x++) {

                boolean fits = true;
                packLoop:
                for (int i = 0; i < s; i++) {
                    for (int j = 0; j < s; j++) {
                        if (arr.get(y + i)[x + j]) {
                            fits = false;
                            break packLoop;
                        }
                    }
                }
                if (fits) {
                    for (int i = 0; i < s; i++) {
                        for (int j = 0; j < s; j++) {
                            arr.get(y + i)[x + j] = true;
                        }
                    }
                    ret.x = x;
                    ret.y = y;
                    return ret;
                }
            }
        }
        int asize = arr.size() + 2;
        for (int i = 0; i < asize; i++)
            arr.add(new boolean[w]);

        return pack(s);
    }
}

public class InitialLayout {

    private static final int SCALE = 533;

    private static void scaleGraph(BMGraph g) {
        /*
          if(g.getNodes().size()<2)return;
          double total = 0;
          int n = g.getNodes().size();
          int pairs = 0 ;

      //	/*
          for(BMNode ni:g.getNodes()){

              for(BMNode nj:g.getNodes()){
          //
          for(BMEdge ee:g.getEdges()){
              BMNode ni = ee.getTo();
              BMNode nj = ee.getFrom();
                  String[] posstr = ni.get(BMGraphAttributes.POS_KEY).split(",");
                  double x = Double.parseDouble(posstr[0]);
                  double y = Double.parseDouble(posstr[1]);
                  posstr = nj.get(BMGraphAttributes.POS_KEY).split(",");
                  double xj = Double.parseDouble(posstr[0]);
                  double yj = Double.parseDouble(posstr[1]);
                  double dist = Math.hypot(xj-x, yj-y);
              //	dist = (dist);
                  pairs++;
                  total+=dist;
      //		}
          }
          total=total/pairs;
          final double TARGET = 2000;

          System.out.println("total = "+total);

          double scale = TARGET/total;
          for(BMNode ni:g.getNodes()){
              String[] posstr = ni.get(BMGraphAttributes.POS_KEY).split(",");
              double x = Double.parseDouble(posstr[0]);
              double y = Double.parseDouble(posstr[1]);
              x*=scale;
              y*=scale;
              ni.put(BMGraphAttributes.POS_KEY,x+","+y);
          }
          */

    }

    private static int graphWidth(BMGraph g) {
        if (g.getNodes().size() < 2) return 1;
        double minx, miny, maxx, maxy;
        minx = miny = Double.MAX_VALUE;
        maxy = maxx = -Double.MAX_VALUE;
        for (BMNode n : g.getNodes()) {
            String[] posstr = n.get(BMGraphAttributes.POS_KEY).split(",");
            double x = Double.parseDouble(posstr[0]);
            double y = Double.parseDouble(posstr[1]);
            minx = Math.min(x, minx);
            miny = Math.min(y, miny);
            maxx = Math.max(x, minx);
            maxy = Math.max(y, maxx);

        }
        //System.out.println("minx = "+minx+" maxx = "+maxx+" miny = "+miny+" maxy = "+maxy);
        return 5*(int) (Math.max(1, (Math.ceil(Math.max(maxx - minx, maxy - miny)) / SCALE)));
    }

    private static void ccr(BMNode n, BMGraph newGraph, BMGraph graph) {
        if (newGraph.hasNode(n)) {
            return;
        }
        newGraph.ensureHasNode(n);
        // System.out.println("Node:"+n.getId());
        // System.out.println(newGraph.getNodes().size()+" nodes");
        for (BMEdge e : graph.getNodeEdges(n)) {

            // System.out.println("edge "+e.getFrom().getId()+" -> "
            // +e.getTo());
            ccr(e.getTo(), newGraph, graph);
            ccr(e.getFrom(), newGraph, graph);
            newGraph.ensureHasEdge(e);
        }
    }

    private static BMGraph[] connectedComponents(BMGraph b) {
        HashSet<BMNode> handled = new HashSet<BMNode>();
        ArrayList<BMGraph> al = new ArrayList<BMGraph>();
        for (BMNode node : b.getNodes()) {
            if (handled.contains(node))
                continue;
            BMGraph newGraph = new BMGraph();
            ccr(node, newGraph, b);
            for (BMNode in : newGraph.getNodes())
                handled.add(in);
            al.add(newGraph);

        }
        BMGraph[] ret = new BMGraph[al.size()];
        al.toArray(ret);
        return ret;
    }

    /**
     * Uses distance scaling to solve initial layout.
     *
     * @param b
     */
    public static void solvePositions(BMGraph b) {
        if (b.getNodes().size() < 1) {
            Logging.info("layout", "Not doing initial layout on an empty graph.");
            return;
        }
        Logging.info("layout", "Solving initial layout positions for a graph of " + b.getNodes().size() + " nodes");

        BMGraph[] components = connectedComponents(b);
        for (int i = 0; i < components.length; i++) {
            solveComponentPositions(components[i]);
        }

        Comparator<BMGraph> graphCmp = new Comparator<BMGraph>() {
            public int compare(BMGraph arg0, BMGraph arg1) {
                return graphWidth(arg1) - graphWidth(arg0);
            }
        };
        Arrays.sort(components, graphCmp);

        SquarePacker pack = new SquarePacker(2 * graphWidth(components[0]));
        for (int i = 0; i < components.length; i++) {
            int wh = graphWidth(components[i]);
            Point p = pack.pack(wh);
            //System.out.println("p = "+p);
            for (BMNode cnode : components[i].getNodes()) {
                String[] posstr = cnode.get(BMGraphAttributes.POS_KEY).split(",");
                double x = Double.parseDouble(posstr[0]);
                double y = Double.parseDouble(posstr[1]);
                double whd = wh/5.0;
                BMNode node = b.getNode(cnode);
                x = (p.x/5.0 * SCALE + SCALE * whd / 2 + 0.5 * (x * 0.9));
                y = (p.y/5.0 * SCALE + SCALE * whd / 2 + 0.5 * (y * 0.9));

                node.put(BMGraphAttributes.POS_KEY, x + "," + y);
            }

        }
        Logging.info("layout", "Solved initial layout positions.");
    }

    private static void solveComponentPositions(BMGraph b) {
        int i = 0;

        if (b.getNodes().size() < 2) {
            for (BMNode n : b.getNodes())
                n.put(BMGraphAttributes.POS_KEY, "0.0,0.0");

            return;
        } else if (b.getNodes().size() > 500) {
            Logging.warning("enduser", "Not doing initial layout due to large graph size (over 500 nodes).");
            for (BMNode n : b.getNodes()) {
                n.put(BMGraphAttributes.POS_KEY, 500 * (Math.random() - 0.5) + "," + 500 * (Math.random() - 0.5));
                n.put(BMGraphAttributes.PINNED_KEY, "0");
            }

            return;
        }
        if (b.getNodes().size() == 2) {
            double x = -0.75;
            for (BMNode n : b.getNodes()) {
                n.put(BMGraphAttributes.POS_KEY, x + ",0.0");
                n.put(BMGraphAttributes.PINNED_KEY, "0");
                x += 1.5;
            }
            return;
        }
        Matrix m = LayoutUtils.bmToMatrix(b);

        BMNode[] idToNode = new BMNode[m.cols()];
        for (BMNode node : b.getNodes()) {
            idToNode[i] = node;
            i++;
        }

        int n = m.cols();
        Vec2[] startPositions = new Vec2[n];
        Random random = new Random();
        for (i = 0; i < n; i++) {
            int hash = idToNode[i].getId().hashCode();
            random.setSeed(hash);
            double x = random.nextDouble(), y = random.nextDouble();
            startPositions[i] = new Vec2(x, y);
        }

        StressMinimizer sm = new StressMinimizer(m, startPositions);
        sm.iterate();
        Vec2[] pos = sm.getPositions();

        for (i = 0; i < n; i++) {
            double scale = SCALE;
            Vec2 p = pos[i].scaled(scale);
            idToNode[i].put(BMGraphAttributes.POS_KEY, p.x + "," + p.y);
            idToNode[i].put(BMGraphAttributes.PINNED_KEY, "0");
        }

        scaleGraph(b);
    }

    public static void main(String[] args) {
        BMGraph b = null;
        try {
            if (args.length > 0) {
                b = BMGraphUtils.readBMGraph(args[0]);
            } else {
                b = BMGraphUtils.readBMGraph(System.in);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (b == null) {
            System.exit(1);
        }

        solvePositions(b);

        try {
            if (args.length > 0) {
                BMGraphUtils.writeBMGraph(b, args[0]);
            } else {
                BMGraphUtils.writeBMGraph(b, System.out);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
