package biomine.bmvis2;

import java.io.ByteArrayInputStream;

import biomine.bmgraph.BMGraph;
import biomine.bmgraph.BMGraphUtils;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class GraphReadingTest {
    private VisualGraph oldGroups;
    private VisualGraph newGroups;

    public static final String oldStyleGroupString = "Island_Java\n" +
            "Island_Sumatra\n" +

            "# _group Island_grp1x2 2 Island Borneo,Kalimantan\n" +

            "Island_grp1x2 Country_Indonesia is_part_of\n" +

            "Island_Java Country_Indonesia is_part_of\n" +
            "Island_Sumatra Country_Indonesia is_part_of";

    public static final String newStyleGroupString = "Island_Java\n" +
            "Island_Sumatra\n" +
            "Island_Borneo Group_IndonesianIsles belongs_to\n" +

            "Island_Kalimantan Group_IndonesianIsles belongs_to\n" +
            "Group_IndonesianIsles Country_Indonesia is_part_of\n" +

            "Island_Java Country_Indonesia is_part_of\n" +
            "Island_Sumatra Country_Indonesia is_part_of\n" +
            "Island_Borneo Country_Indonesia is_part_of\n" +
            "Island_Kalimantan Country_Indonesia is_part_of";

    public static BMGraph stringToBMGraph(String in) {
        ByteArrayInputStream bs = new ByteArrayInputStream(in.getBytes());
        return BMGraphUtils.readBMGraph(bs);
    }

    @BeforeMethod
    public void setUp() throws Exception {
        BMGraph oldStyleGroups = GraphReadingTest.stringToBMGraph(oldStyleGroupString);
        BMGraph newStyleGroups = GraphReadingTest.stringToBMGraph(newStyleGroupString);
        oldGroups = new VisualGraph(oldStyleGroups);
        newGroups = new VisualGraph(newStyleGroups);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        this.oldGroups = null;
        this.newGroups = null;
    }

    @Test
    public void testReadToVisualGraph() throws Exception {
        Assert.assertEquals(this.oldGroups.getAllNodes().size(), 7);
        Assert.assertEquals(this.newGroups.getAllNodes().size(), 7);
    }
}
