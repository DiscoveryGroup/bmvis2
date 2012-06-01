package biomine.bmvis2;

import biomine.bmgraph.BMGraph;
import biomine.bmgraph.BMGraphUtils;
import biomine.bmvis2.subgraph.DijkstraExtractor;
import biomine.bmvis2.subgraph.Path;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DijkstraExtractorTest {
    private VisualGraph graph;
    private VisualNode foo;
    private VisualNode bar;

    public static final String graphString = "Node_foo\n" +
            "Node_bar\n" +

            "Node_foo Node_bar is_related_to weight=1\n" +
            "Node_baz Node_bar is_related_to weight=10\n" +
            "Node_baz Node_foo is_related_to weight=10\n";

    public static BMGraph stringToBMGraph(String in) {
        ByteArrayInputStream bs = new ByteArrayInputStream(in.getBytes());
        return BMGraphUtils.readBMGraph(bs);
    }

    @BeforeMethod
    public void setUp() throws Exception {
        BMGraph bmg = GraphReadingTest.stringToBMGraph(graphString);
        this.graph = new VisualGraph(bmg);

        this.foo = null;
        this.bar = null;

        for (VisualNode node : this.graph.getNodes()) {
            if (node.getId().equals("foo")) {
                foo = node;
                this.graph.addNodeOfInterest(node, 1.0);
            } else if (node.getId().equals("bar")) {
                bar = node;
                this.graph.addNodeOfInterest(node, 1.0);
            }
        }
    }

    @AfterMethod
    public void tearDown() throws Exception {
        this.graph = null;
    }

    @Test
    public void testReadToVisualGraph() throws Exception {
        Assert.assertEquals(this.graph.getAllNodes().size(), 4); // Root node is the node number 4!
    }

    @Test
    public void testSetNodeOfInterests() throws Exception {
        Assert.assertEquals(this.graph.getNodesOfInterest().keySet().size(), 2);
    }

    @Test
    public void testProbabilisticWeightAssignments() throws Exception {
        VisualEdge fooBar = null;
        VisualEdge fooBaz = null;

        for (VisualEdge edge : foo.getEdges())
            if (edge.getOther(foo).equals(this.bar))
                fooBar = edge;
            else if (edge.getOther(foo).getId().equals("baz"))
                fooBaz = edge;

        Assert.assertNotNull(fooBar);
        Assert.assertNotNull(fooBaz);
        Assert.assertTrue(fooBar.getWeight(VisualEdge.WeightType.PROBABILISTIC).value == 0.1);
        Assert.assertTrue(fooBar.getWeight(VisualEdge.WeightType.WEIGHT).value == 1);
        Assert.assertTrue(fooBaz.getWeight(VisualEdge.WeightType.WEIGHT).value == 10);
    }

    @Test
    public void testExtractBestPaths() throws Exception {
        DijkstraExtractor x = new DijkstraExtractor();

        Map<VisualNode, List<Path>> paths = x.calculateBestPaths(this.graph, this.foo, Collections.<VisualNode>emptySet());


        for (VisualEdge edge : this.graph.getEdges())
            d(" - " + edge + ": " + edge.getWeight(VisualEdge.WeightType.PROBABILISTIC));
        for (VisualNode node : paths.keySet())
            d(node + ": " + paths.get(node).toString());

        Assert.assertEquals(paths.keySet().size(), 2);
        Assert.assertEquals(paths.get(this.bar).get(0).edges().size(), 2);
    }

    private void d(String s) {
        System.err.println(s);
    }

}
